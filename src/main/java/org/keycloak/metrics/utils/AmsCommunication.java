package org.keycloak.metrics.utils;

import java.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.metrics.representations.AmsDto;
import org.keycloak.metrics.representations.MessagesDto;
import org.keycloak.metrics.representations.MetricsDto;
import org.keycloak.models.RealmModel;

public class AmsCommunication {

    private static final Logger logger = Logger.getLogger(AmsCommunication.class);
    private static final OkHttpClient client = new OkHttpClient.Builder().build();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static void communicate(RealmModel realm, Event event) throws Exception {
        MetricsDto metricsDto = new MetricsDto(event, realm);
        String encodedData = Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(metricsDto));
        AmsDto amsDto = new AmsDto(new MessagesDto(encodedData));
        String amsJson = objectMapper.writeValueAsString(amsDto);

        Request request = new Request.Builder()
                .url(realm.getAttribute(MetricsUtils.AMS_URL) + MetricsUtils.PUBLISH)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader(MetricsUtils.API_KEY, realm.getAttribute(MetricsUtils.API_KEY))
                .post(RequestBody.create(amsJson, JSON))
                .build();



        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            int statusCode=  response.code();
            logger.error("ams response error with status: " + statusCode);
            logger.error("message : " + response.message());
            response.close();
            throw new Exception("ams response error with status: " + statusCode);
        } else {
            response.close();
        }

    }
}
