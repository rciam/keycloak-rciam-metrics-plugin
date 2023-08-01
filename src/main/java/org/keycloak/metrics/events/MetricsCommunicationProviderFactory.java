package org.keycloak.metrics.events;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventType;
import org.keycloak.metrics.utils.MetricsUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

public class MetricsCommunicationProviderFactory implements EventListenerProviderFactory {

    public static final String ID = "metrics-communication";

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
