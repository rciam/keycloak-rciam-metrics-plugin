package org.keycloak.metrics.events;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventType;
import org.keycloak.metrics.scheduled.MetricsTimerProvider;
import org.keycloak.metrics.scheduled.PushEventsTask;
import org.keycloak.metrics.utils.MetricsUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.services.scheduled.ClusterAwareScheduledTaskRunner;

public class MetricsCommunicationProviderFactory implements EventListenerProviderFactory {

    private static final Logger logger = Logger.getLogger(MetricsCommunicationProviderFactory.class);
    public static final String ID = "metrics-communication";
    private boolean executeStartupTasks = true;

    private static final Set<EventType> SUPPORTED_EVENTS = new HashSet<>();
    static {
        Collections.addAll(SUPPORTED_EVENTS, EventType.LOGIN, EventType.LOGIN_ERROR, EventType.REGISTER, EventType.valueOf(MetricsUtils.GROUP_MEMBERSHIP_CREATE), EventType.valueOf(MetricsUtils.GROUP_MEMBERSHIP_SUSPEND), EventType.valueOf(MetricsUtils.GROUP_MEMBERSHIP_DELETE));
    }

    private Set<EventType> includedEvents = new HashSet<>();


    @Override
    public MetricsCommunicationProvider create(KeycloakSession session) {
        return new MetricsCommunicationProvider(session, includedEvents);
    }

    @Override
    public void init(Config.Scope config) {
        String[] include = config.getArray("include-events");
        if (include != null) {
            for (String i : include) {
                includedEvents.add(EventType.valueOf(i.toUpperCase()));
            }
        } else {
            includedEvents.addAll(SUPPORTED_EVENTS);
        }

        String[] exclude = config.getArray("exclude-events");
        if (exclude != null) {
            for (String e : exclude) {
                includedEvents.remove(EventType.valueOf(e.toUpperCase()));
            }
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

        if(executeStartupTasks) {
            logger.info("Keycloak metrics plugin event listener is starting...");
            try (KeycloakSession session = factory.create()) {
                MetricsTimerProvider timer = session.getProvider(MetricsTimerProvider.class);
                //schedule task every 4 hours
                long interval = 4 * 3600 * 1000;
                timer.scheduleOnce(new ClusterAwareScheduledTaskRunner(session.getKeycloakSessionFactory(), new PushEventsTask(), interval), interval, "PushEventsTaskOnce");
                timer.schedule(new ClusterAwareScheduledTaskRunner(session.getKeycloakSessionFactory(), new PushEventsTask(), interval), interval, interval, "PushEventsTaskDaily");
            }
            executeStartupTasks = false;
        }
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        String[] supportedEvents = Arrays.stream(EventType.values())
                .map(EventType::name)
                .map(String::toLowerCase)
                .sorted(Comparator.naturalOrder())
                .toArray(String[]::new);
        return ProviderConfigurationBuilder.create()
                .property()
                .name("include-events")
                .type("string")
                .helpText("A comma-separated list of events that are supported.")
                .options(supportedEvents)
                .defaultValue("All events")
                .add()
                .property()
                .name("exclude-events")
                .type("string")
                .helpText("A comma-separated list of events that are excluded.")
                .options(supportedEvents)
                .add()
                .build();
    }
}
