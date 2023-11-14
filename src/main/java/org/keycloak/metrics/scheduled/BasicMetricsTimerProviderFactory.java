package org.keycloak.metrics.scheduled;

import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.timer.TimerProvider;
import org.keycloak.timer.TimerProviderFactory;

public class BasicMetricsTimerProviderFactory implements MetricsTimerProviderFactory {

    private Timer timer;

    private int transactionTimeout;

    public static final String TRANSACTION_TIMEOUT = "transactionTimeout";

    private ConcurrentMap<String, MetricsTimerTaskContextImpl> scheduledTasks = new ConcurrentHashMap<>();

    @Override
    public MetricsTimerProvider create(KeycloakSession session) {
        return new BasicMetricsTimerProvider(session, timer, transactionTimeout, this);
    }

    @Override
    public void init(Config.Scope config) {
        transactionTimeout = config.getInt(TRANSACTION_TIMEOUT, 0);
        timer = new Timer();
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
        timer.cancel();
        timer = null;
    }

    @Override
    public String getId() {
        return "basic";
    }

    protected MetricsTimerTaskContextImpl putTask(String taskName, MetricsTimerTaskContextImpl task) {
        return scheduledTasks.put(taskName, task);
    }

    protected MetricsTimerTaskContextImpl removeTask(String taskName) {
        return scheduledTasks.remove(taskName);
    }

}
