package org.keycloak.metrics.events;

import java.util.Set;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.metrics.utils.MetricsUtils;
import org.keycloak.metrics.scheduled.BasicMetricsTimerProvider;
import org.keycloak.metrics.scheduled.MetricsCommunicationTask;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.scheduled.ClusterAwareScheduledTaskRunner;
import org.keycloak.timer.TimerProvider;

public class MetricsCommunicationProvider implements EventListenerProvider {

    private KeycloakSession session;
    private Set<EventType> includedEvents;
    private EventListenerTransaction tx = new EventListenerTransaction(null, this::metricsCommunication);

    public MetricsCommunicationProvider(KeycloakSession session, Set<EventType> includedEvents) {
        this.session = session;
        this.session.getTransactionManager().enlistAfterCompletion(tx);
        this.includedEvents = includedEvents;
    }

    @Override
    public void onEvent(Event event) {
        RealmModel realm = session.realms().getRealm(event.getRealmId());
        if (realm.getAttribute(MetricsUtils.AMS_URL) != null && includedEvents.contains(event.getType()))
            tx.addEvent(event);
    }

    private void metricsCommunication(Event event) {
        RealmModel realm = session.realms().getRealm(event.getRealmId());
        BasicMetricsTimerProvider timer = (BasicMetricsTimerProvider) session.getProvider(TimerProvider.class, "metrics");
        timer.scheduleOnce(new ClusterAwareScheduledTaskRunner(session.getKeycloakSessionFactory(), new MetricsCommunicationTask(event, realm.getId()), 60 * 1000), 30 * 1000, "MetricsCommunicationTask" + event.getId());

    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
    }

    @Override
    public void close() {
    }
}
