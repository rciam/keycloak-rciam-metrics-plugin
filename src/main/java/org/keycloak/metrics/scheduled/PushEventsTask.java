package org.keycloak.metrics.scheduled;


import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.persistence.Tuple;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.metrics.utils.AmsCommunication;
import org.keycloak.metrics.utils.MetricsUtils;
import org.keycloak.metrics.jpa.EventNotSendRepository;
import org.keycloak.models.KeycloakSession;
import org.keycloak.timer.ScheduledTask;

public class PushEventsTask implements ScheduledTask {

    private static final Logger logger = Logger.getLogger(PushEventsTask.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final TypeReference<Map<String, String>> mapType = new TypeReference<Map<String, String>>() {
    };

    @Override
    public void run(KeycloakSession session) {
        EventNotSendRepository repository = new EventNotSendRepository(session);
        session.realms().getRealmsStream().forEach(realm -> {
            String metricsUrl = realm.getAttribute(MetricsUtils.AMS_URL);
            if (metricsUrl != null) {
                Stream<Tuple> events = repository.eventsNotSendByRealm(realm.getId());
                events.forEach(eventNotSend -> {
                    try {
                        AmsCommunication.communicate(realm, convertEventEntity(eventNotSend));
                        repository.deleteEntity((String) eventNotSend.get("id"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

        });
    }

    private Event convertEventEntity(Tuple tuple) {
        Event event = new Event();
        event.setId((String) tuple.get("id"));
        event.setTime(((BigInteger) tuple.get("event_time")).longValue());
        event.setType(EventType.valueOf((String) tuple.get("type")));
        event.setClientId((String) tuple.get("client_id"));
        event.setUserId((String) tuple.get("user_id"));
        event.setSessionId((String) tuple.get("session_id"));
        event.setIpAddress((String) tuple.get("ip_address"));
        event.setError((String) tuple.get("error"));
        try {
            Map<String, String> details = mapper.readValue((String) tuple.get("details_json"), mapType);
            event.setDetails(details);
        } catch (IOException ex) {
            logger.error("Failed to read log details", ex);
        }
        return event;
    }
}
