package org.keycloak.metrics.events;

import java.util.Set;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.metrics.scheduled.MetricsTimerProvider;
import org.keycloak.metrics.utils.MetricsUtils;
import org.keycloak.metrics.scheduled.MetricsCommunicationTask;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.scheduled.ClusterAwareScheduledTaskRunner;

public class MetricsCommunicationProvider implements EventListenerProvider {

    private KeycloakSession session;
    private Set<EventType> includedEvents;
    private Set<ResourceType> includedAdminEvents;
    private EventListenerTransaction tx = new EventListenerTransaction(this::metricsCommunication, this::metricsCommunication);

    public MetricsCommunicationProvider(KeycloakSession session, Set<EventType> includedEvents, Set<ResourceType> includedAdminEvents) {
        this.session = session;
        this.session.getTransactionManager().enlistAfterCompletion(tx);
        this.includedEvents = includedEvents;
        this.includedAdminEvents = includedAdminEvents;
    }

    @Override
    public void onEvent(Event event) {
        RealmModel realm = session.realms().getRealm(event.getRealmId());
        if (realm.getAttribute(MetricsUtils.AMS_URL) != null && includedEvents.contains(event.getType()))
            tx.addEvent(event);
    }

    private void metricsCommunication(Event event) {
        RealmModel realm = session.realms().getRealm(event.getRealmId());
        MetricsTimerProvider timer = session.getProvider(MetricsTimerProvider.class);
        timer.scheduleOnce(new ClusterAwareScheduledTaskRunner(session.getKeycloakSessionFactory(), new MetricsCommunicationTask(event, null, realm.getId()), 60 * 1000), 30 * 1000, "MetricsCommunicationTask" + event.getId());

    }

    private void metricsCommunication(AdminEvent adminEvent, boolean includeRepresentation) {
        RealmModel realm = session.realms().getRealm(adminEvent.getRealmId());
        MetricsTimerProvider timer = session.getProvider(MetricsTimerProvider.class);
        timer.scheduleOnce(new ClusterAwareScheduledTaskRunner(session.getKeycloakSessionFactory(), new MetricsCommunicationTask(null, adminEvent, realm.getId()), 60 * 1000), 30 * 1000, "MetricsCommunicationTask" + adminEvent.getId());

    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        RealmModel realm = session.realms().getRealm(event.getRealmId());
        if (realm.getAttribute(MetricsUtils.AMS_URL) != null && includedAdminEvents.contains(event.getResourceType()))
            tx.addAdminEvent(event, false);
    }

    @Override
    public void close() {
    }
}
