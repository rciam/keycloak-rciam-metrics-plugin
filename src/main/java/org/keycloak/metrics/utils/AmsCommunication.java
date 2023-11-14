package org.keycloak.metrics.utils;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.BadRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.metrics.representations.AmsDto;
import org.keycloak.metrics.representations.MessagesDto;
import org.keycloak.metrics.representations.MetricsDto;
import org.keycloak.models.RealmModel;

public class AmsCommunication {

    private static final Logger logger = Logger.getLogger(AmsCommunication.class);
    private static final OkHttpClient client = new OkHttpClient.Builder().build();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final List<EventType> groupEvents = Stream.of(EventType.valueOf(MetricsUtils.GROUP_MEMBERSHIP_CREATE), EventType.valueOf(MetricsUtils.GROUP_MEMBERSHIP_SUSPEND), EventType.valueOf(MetricsUtils.GROUP_MEMBERSHIP_DELETE)).collect(Collectors.toList());

    public static void communicate(RealmModel realm, Event event) throws BadRequestException, IOException {
        //groupEvents are only allowed for top level groups
        if ((!groupEvents.contains(event.getType())) || StringUtils.countMatches(event.getDetails().get(MetricsUtils.EVENT_GROUP), "/") == 1) {
            MetricsDto metricsDto = new MetricsDto(event, realm);
            String encodedData = Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(metricsDto));
            AmsDto amsDto = new AmsDto(new MessagesDto(encodedData));
            String amsJson = objectMapper.writeValueAsString(amsDto);
            logger.info("try to write message with body : " + amsJson);

            Request request = new Request.Builder()
                    .url(realm.getAttribute(MetricsUtils.AMS_URL) + MetricsUtils.PUBLISH)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader(MetricsUtils.API_KEY, realm.getAttribute(MetricsUtils.API_KEY))
                    .post(RequestBody.create(amsJson, JSON))
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                int statusCode = response.code();
                logger.error("ams response error with status: " + statusCode);
                logger.error("message : " + response.message());
                response.close();
                throw new BadRequestException("ams response error with status: " + statusCode);
            } else {
                response.close();
            }
        }

    }
}
