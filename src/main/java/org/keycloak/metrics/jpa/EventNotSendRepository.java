package org.keycloak.metrics.jpa;

import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.ws.rs.NotFoundException;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

public class EventNotSendRepository {

    protected final EntityManager em;

    public EventNotSendRepository(KeycloakSession session) {
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    public void create(EventNotSendEntity entity) {
        em.persist(entity);
        em.flush();
    }

    public void deleteEntity(EventNotSendEntity entity) {
        em.remove(entity);
        em.flush();
    }

    public Stream<EventNotSendEntity> eventsNotSendByRealm(String realmId){
        return em.createQuery("from EventNotSendEntity f where f.id.realm.id = :realmId", EventNotSendEntity.class).setParameter("realmId",realmId).getResultStream();
    }
}
