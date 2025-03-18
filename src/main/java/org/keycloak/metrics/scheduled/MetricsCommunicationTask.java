package org.keycloak.metrics.scheduled;

import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.metrics.utils.AmsCommunication;
import org.keycloak.metrics.jpa.EventNotSendRepository;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.timer.ScheduledTask;

public class MetricsCommunicationTask implements ScheduledTask {

    private final Event event;
    private final AdminEvent adminEvent;
    private final String realmId;

    public MetricsCommunicationTask(Event event, AdminEvent adminEvent, String realmId) {
        this.event = event;
        this.adminEvent = adminEvent;
        this.realmId = realmId;
    }

    @Override
    public void run(KeycloakSession session) {
        RealmModel realm = session.realms().getRealm(realmId);

        try {
            AmsCommunication ams = new AmsCommunication();
            ams.communicate(realm, event, adminEvent);
        } catch (Exception e) {
            e.printStackTrace();
            EventNotSendRepository eventNotSendRepository = new EventNotSendRepository(session);
            if (event != null) {
                eventNotSendRepository.create(event.getId(), realmId);
            } else {
                eventNotSendRepository.createAdmin(adminEvent.getId(), realmId);
            }
        }

    }
}
