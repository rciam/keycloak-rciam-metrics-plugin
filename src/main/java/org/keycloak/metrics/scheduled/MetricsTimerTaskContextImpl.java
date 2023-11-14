package org.keycloak.metrics.scheduled;

import java.util.TimerTask;

import org.keycloak.timer.TimerProvider;

public class MetricsTimerTaskContextImpl implements MetricsTimerProvider.MetricsTimerTaskContext {

    private final Runnable runnable;
    final TimerTask timerTask;
    private final long intervalMillis;

    public MetricsTimerTaskContextImpl(Runnable runnable, TimerTask timerTask, long intervalMillis) {
        this.runnable = runnable;
        this.timerTask = timerTask;
        this.intervalMillis = intervalMillis;
    }

    @Override
    public Runnable getRunnable() {
        return runnable;
    }

    @Override
    public long getIntervalMillis() {
        return intervalMillis;
    }
}

