package org.keycloak.metrics.scheduled;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class MetricsTimerSpi implements Spi {

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "metrics-timer";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return MetricsTimerProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return MetricsTimerProviderFactory.class;
    }
}
