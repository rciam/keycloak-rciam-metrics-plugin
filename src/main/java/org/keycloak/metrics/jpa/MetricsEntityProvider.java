package org.keycloak.metrics.jpa;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.models.KeycloakSession;

public class MetricsEntityProvider implements JpaEntityProvider {

    private KeycloakSession session;

    public MetricsEntityProvider(KeycloakSession session){
        this.session = session;
    }

    /**
     * This is not called anymore in the quarkus based keycloak. Please, load the entities through the beans.xml and the persistence.xml instead
     * @return
     */
    @Deprecated
    @Override
    public List<Class<?>> getEntities() {
        return new ArrayList<>();
    }

    @Override
    public String getChangelogLocation() {
        return "META-INF/metrics-changelog.xml";
    }

    @Override
    public String getFactoryId() {
        return MetricsEntityProviderFactory.ID;
    }

    @Override
    public void close() {

    }
}