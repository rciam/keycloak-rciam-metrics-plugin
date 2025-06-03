package org.keycloak.metrics.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.BadRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.metrics.representations.AmsDto;
import org.keycloak.metrics.representations.MessagesDto;
import org.keycloak.metrics.representations.MetricsDto;
import org.keycloak.models.GroupModel;
import org.keycloak.models.RealmModel;

public class AmsCommunication {

    private static final Logger logger = Logger.getLogger(AmsCommunication.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<OperationType> ALLOWED_OPERATION_TYPES = Stream.of(OperationType.CREATE, OperationType.UPDATE).collect(Collectors.toList());

    private static final List<EventType> groupEvents = Stream.of(EventType.valueOf(MetricsUtils.GROUP_MEMBERSHIP_CREATE), EventType.valueOf(MetricsUtils.GROUP_MEMBERSHIP_SUSPEND), EventType.valueOf(MetricsUtils.GROUP_MEMBERSHIP_DELETE)).collect(Collectors.toList());
    private static final List<EventType> loginEvents = Stream.of(EventType.valueOf(MetricsUtils.LOGIN), EventType.valueOf(MetricsUtils.LOGIN_ERROR), EventType.valueOf(MetricsUtils.REGISTER)).collect(Collectors.toList());

   public void communicate(RealmModel realm, Event event, AdminEvent adminEvent) throws Exception {
        if (event != null && isAllowedEvent(realm, event)) {
            communicateWithAms(new MetricsDto(event, realm), realm);
        } else if (adminEvent != null && ALLOWED_OPERATION_TYPES.contains(adminEvent.getOperationType())) {
            GroupModel group = realm.getGroupById(adminEvent.getResourcePath());
            if (group == null) {
                logger.errorf("Metrics plugin could not find group with %s id", adminEvent.getResourcePath());
            } else if (group.getParent() == null) {
                communicateWithAms(new MetricsDto(adminEvent, group, realm), realm);
            }
        }

    }

    private void communicateWithAms(MetricsDto metricsDto, RealmModel realm) throws Exception {
        String encodedData = Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(metricsDto));
        AmsDto amsDto = new AmsDto(new MessagesDto(encodedData));
        String amsJson = objectMapper.writeValueAsString(amsDto);
        logger.info("try to write message with body : " + amsJson);

        try (CloseableHttpClient client = HttpClientBuilder.create()
                .setConnectionTimeToLive(60, java.util.concurrent.TimeUnit.SECONDS)
                .setDefaultRequestConfig(org.apache.http.client.config.RequestConfig.custom()
                        .setConnectTimeout(60000)
                        .setSocketTimeout(60000)
                        .build())
                .build()) {
            HttpPost request = new HttpPost(realm.getAttribute(MetricsUtils.AMS_URL) + MetricsUtils.PUBLISH);
            request.addHeader("Accept", "application/json");
            request.addHeader("Content-Type", "application/json");
            request.addHeader(MetricsUtils.API_KEY, realm.getAttribute(MetricsUtils.API_KEY));
            StringEntity se = new StringEntity(amsJson);
            request.setEntity(se);

            try {
                CloseableHttpResponse response = client.execute(request);
                if (response.getStatusLine().getStatusCode() >= 400) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    logger.error("ams response error with status: " + statusCode);
                    response.close();
                    throw new BadRequestException("ams response error with status: " + statusCode);
                }
            } catch (ConnectTimeoutException e) {
                logger.error("No response from ams after a minute", e);
                throw new BadRequestException("No response from ams after a minute");
            } catch (Exception e) {
                e.printStackTrace();
                throw new BadRequestException("General problem during communication with ams"+ e.getMessage());
            }
        }
    }

    private boolean isAllowedEvent(RealmModel realm, Event event) {
        //groupEvents are only allowed for top level groups
        String excludedClientsConfigurationValue = realm.getAttribute(MetricsUtils.EXCLUDED_CLIENTS_CONFIGURATION);
        List<String> excludedClients = excludedClientsConfigurationValue == null ? new ArrayList<>() : Arrays.asList(excludedClientsConfigurationValue.split(MetricsUtils.COMMA));
        return (loginEvents.contains(event.getType()) && !excludedClients.contains(event.getClientId())) || (groupEvents.contains(event.getType()) && StringUtils.countMatches(event.getDetails().get(MetricsUtils.EVENT_GROUP), "/") == 1);
    }
}
