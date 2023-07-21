package org.keycloak.metrics.scheduled;

import org.keycloak.events.Event;
import org.keycloak.metrics.utils.AmsCommunication;
import org.keycloak.metrics.jpa.EventNotSendRepository;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.timer.ScheduledTask;

public class MetricsCommunicationTask implements ScheduledTask {

    private final Event event;
    private final String realmId;

    public MetricsCommunicationTask(Event event, String realmId) {
        this.event = event;
        this.realmId = realmId;
    }

    @Override
    public void run(KeycloakSession session) {
        RealmModel realm = session.realms().getRealm(realmId);

        try {
            AmsCommunication.communicate(realm, event);
        } catch (Exception e) {
            e.printStackTrace();
            EventNotSendRepository eventNotSendRepository = new EventNotSendRepository(session);
            eventNotSendRepository.create(event.getId(), realmId);
        }

    }
}
