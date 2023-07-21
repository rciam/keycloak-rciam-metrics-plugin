package org.keycloak.metrics.events;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.metrics.utils.MetricsUtils;
import org.keycloak.metrics.scheduled.MetricsTimerProvider;
import org.keycloak.metrics.scheduled.MetricsCommunicationTask;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.services.scheduled.ClusterAwareScheduledTaskRunner;
import org.keycloak.timer.TimerProvider;

public class MetricsCommunicationProvider implements EventListenerProvider {

    private static final Logger logger = Logger.getLogger(MetricsCommunicationProvider.class);

    private KeycloakSession session;
    private RealmProvider realm;
    private Set<EventType> includedEvents;
    private static final List<EventType> acceptedEventsType = Stream.of(EventType.LOGIN, EventType.LOGIN_ERROR, EventType.IDENTITY_PROVIDER_FIRST_LOGIN).collect(Collectors.toList());

    private EventListenerTransaction tx = new EventListenerTransaction(null, this::metricsCommunication);

    public MetricsCommunicationProvider(KeycloakSession session, Set<EventType> includedEvents) {
        this.session = session;
        this.realm = session.realms();
        this.session.getTransactionManager().enlistAfterCompletion(tx);
        this.includedEvents = includedEvents;
    }

    @Override
    public void onEvent(Event event) {
        if (includedEvents.contains(event.getType()))
            tx.addEvent(event);
    }

    private void metricsCommunication(Event event) {
        RealmModel realm = session.realms().getRealm(event.getRealmId());
        if (realm.getAttribute(MetricsUtils.AMS_URL) != null && acceptedEventsType.contains(event.getType())) {
            MetricsTimerProvider timer = (MetricsTimerProvider) session.getProvider(TimerProvider.class, "metrics");
            timer.scheduleOnce(new ClusterAwareScheduledTaskRunner(session.getKeycloakSessionFactory(), new MetricsCommunicationTask(event, realm.getId()), 60 * 1000), 30 * 1000, "MetricsCommunicationTask"+event.getId());
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
    }

    @Override
    public void close() {
    }
}
