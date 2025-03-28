package org.keycloak.metrics.scheduled;

import jakarta.persistence.Tuple;
import org.jboss.logging.Logger;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.metrics.jpa.EventNotSendRepository;
import org.keycloak.metrics.utils.AmsCommunication;
import org.keycloak.metrics.utils.MetricsUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.scheduled.ClusterAwareScheduledTaskRunner;
import org.keycloak.timer.ScheduledTask;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PushAdminEventsTask implements ScheduledTask {

    private static final Logger logger = Logger.getLogger(PushAdminEventsTask.class);

    @Override
    public void run(KeycloakSession session) {
        EventNotSendRepository repository = new EventNotSendRepository(session);
        boolean reExecute = false;
        for (RealmModel realm : session.realms().getRealmsStream().collect(Collectors.toList()))
        {
            String metricsUrl = realm.getAttribute(MetricsUtils.AMS_URL);
            if (metricsUrl != null) {
                logger.infof("PushAdminEventsTask is running for realm %s", realm.getName());
                Stream<Tuple> adminEvents = repository.adminEventsNotSendByRealm(realm.getId());
                adminEvents.forEach(eventNotSend -> {
                    try {
                        AmsCommunication ams = new AmsCommunication();
                        ams.communicate(realm, null, convertAdminEventEntity(eventNotSend));
                        repository.deleteAdminEntity((String) eventNotSend.get("id"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                if (repository.countAdminEventsNotSendByRealm(realm.getId()) > 0)
                    reExecute = true;
            }

        }
        if (reExecute) {
            logger.infof("Reexecute PushAdminEventsTask");
            MetricsTimerProvider timer = session.getProvider(MetricsTimerProvider.class);
            long interval = 300 * 1000;
            timer.scheduleOnce(new ClusterAwareScheduledTaskRunner(session.getKeycloakSessionFactory(), new PushAdminEventsTask(), interval), interval, "PushAdminEventsTaskOnce");
        }
    }

    private AdminEvent convertAdminEventEntity(Tuple tuple) {
        AdminEvent event = new AdminEvent();
        event.setId((String) tuple.get("id"));
        event.setTime(((Long) tuple.get("event_time")));
        event.setOperationType(OperationType.valueOf((String) tuple.get("operation_type")));
        event.setResourcePath((String) tuple.get("resource_type"));
        event.setError((String) tuple.get("error"));
        return event;
    }
}
