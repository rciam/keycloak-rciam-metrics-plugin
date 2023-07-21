package org.keycloak.metrics;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;
import org.keycloak.metrics.scheduled.MetricsTimerProvider;
import org.keycloak.metrics.scheduled.PushEventsTask;
import org.keycloak.models.KeycloakSession;
import org.keycloak.quarkus.runtime.integration.QuarkusKeycloakSessionFactory;
import org.keycloak.services.scheduled.ClusterAwareScheduledTaskRunner;
import org.keycloak.timer.TimerProvider;

@ApplicationScoped
public class MetricsEvent {

    private static final Logger logger = Logger.getLogger(MetricsEvent.class);

    void onStart(@Observes StartupEvent ev) {
        logger.info("Metrics plugin is starting...");
        //work only for quarkus
        QuarkusKeycloakSessionFactory instance = QuarkusKeycloakSessionFactory.getInstance();
        instance.init();
        KeycloakSession session = instance.create();
        MetricsTimerProvider timer = (MetricsTimerProvider) session.getProvider(TimerProvider.class, "metrics");
        //schedule task every 4 hours
        //long interval = 4 * 3600 * 1000;
        long interval = 3 * 60 * 1000;
        timer.scheduleOnce(new ClusterAwareScheduledTaskRunner(session.getKeycloakSessionFactory(), new PushEventsTask(), interval), interval, "PushEventsTaskOnce");
        timer.schedule(new ClusterAwareScheduledTaskRunner(session.getKeycloakSessionFactory(), new PushEventsTask(), interval), interval, interval, "PushEventsTaskDaily");
    }

}
