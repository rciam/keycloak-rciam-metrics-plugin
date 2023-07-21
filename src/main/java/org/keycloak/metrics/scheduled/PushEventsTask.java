package org.keycloak.metrics.scheduled;


import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.jpa.EventEntity;
import org.keycloak.metrics.utils.AmsCommunication;
import org.keycloak.metrics.utils.MetricsUtils;
import org.keycloak.metrics.jpa.EventNotSendEntity;
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
                Stream<EventNotSendEntity> events = repository.eventsNotSendByRealm(realm.getId());
                events.forEach(eventNotSend -> {
                    try {
                        AmsCommunication.communicate(realm, convertEventEntity(eventNotSend.getId().getEvent()));
                        repository.deleteEntity(eventNotSend);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

        });
    }

    private Event convertEventEntity(EventEntity eventEntity) {
        Event event = new Event();
        event.setId(eventEntity.getId() == null ? UUID.randomUUID().toString() : eventEntity.getId());
        event.setTime(eventEntity.getTime());
        event.setType(EventType.valueOf(eventEntity.getType()));
        event.setRealmId(eventEntity.getRealmId());
        event.setClientId(eventEntity.getClientId());
        event.setUserId(eventEntity.getUserId());
        event.setSessionId(eventEntity.getSessionId());
        event.setIpAddress(eventEntity.getIpAddress());
        event.setError(eventEntity.getError());
        try {
            Map<String, String> details = mapper.readValue(eventEntity.getDetailsJson(), mapType);
            event.setDetails(details);
        } catch (IOException ex) {
            logger.error("Failed to read log details", ex);
        }
        return event;
    }
}
