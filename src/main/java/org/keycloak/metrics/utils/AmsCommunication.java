package org.keycloak.metrics.utils;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.BadRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.metrics.representations.AmsDto;
import org.keycloak.metrics.representations.MessagesDto;
import org.keycloak.metrics.representations.MetricsDto;
import org.keycloak.models.RealmModel;

public class AmsCommunication {

    private static final Logger logger = Logger.getLogger(AmsCommunication.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<EventType> groupEvents = Stream.of(EventType.valueOf(MetricsUtils.GROUP_MEMBERSHIP_CREATE), EventType.valueOf(MetricsUtils.GROUP_MEMBERSHIP_SUSPEND), EventType.valueOf(MetricsUtils.GROUP_MEMBERSHIP_DELETE)).collect(Collectors.toList());

    public static void communicate(RealmModel realm, Event event) throws Exception {
        //groupEvents are only allowed for top level groups
        if ((!groupEvents.contains(event.getType())) || StringUtils.countMatches(event.getDetails().get(MetricsUtils.EVENT_GROUP), "/") == 1) {
            MetricsDto metricsDto = new MetricsDto(event, realm);
            String encodedData = Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(metricsDto));
            AmsDto amsDto = new AmsDto(new MessagesDto(encodedData));
            String amsJson = objectMapper.writeValueAsString(amsDto);
            logger.info("try to write message with body : " + amsJson);

            try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
                HttpPost request = new HttpPost(realm.getAttribute(MetricsUtils.AMS_URL) + MetricsUtils.PUBLISH);
                request.addHeader("Accept", "application/json");
                request.addHeader("Content-Type", "application/json");
                request.addHeader(MetricsUtils.API_KEY, realm.getAttribute(MetricsUtils.API_KEY));
                StringEntity se = new StringEntity(amsJson);
                request.setEntity(se);

                CloseableHttpResponse response = client.execute(request);
                if (response.getStatusLine().getStatusCode() >= 400) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    logger.error("ams response error with status: " + statusCode);
                    response.close();
                    throw new BadRequestException("ams response error with status: " + statusCode);
                }
            }
        }

    }
}
