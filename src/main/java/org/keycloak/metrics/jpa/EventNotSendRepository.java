package org.keycloak.metrics.jpa;

import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;

public class EventNotSendRepository {

    protected final EntityManager em;

    public EventNotSendRepository(KeycloakSession session) {
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    public void create(String eventId, String realmId) {
        em.createNativeQuery("INSERT INTO event_not_send (realm_id,event_id) VALUES (:realmId, :eventId)").setParameter("eventId", eventId).setParameter("realmId", realmId).executeUpdate();
    }

    public void deleteEntity(String eventId) {
        em.createNativeQuery("delete from event_not_send where event_id = :eventId").setParameter("eventId", eventId).executeUpdate();
    }

    public Stream<Tuple> eventsNotSendByRealm(String realmId) {
        return em.createNativeQuery("select f.*  from EVENT_ENTITY f join EVENT_NOT_SEND e on f.id = e.event_id where f.realm_id= :realmId", Tuple.class).setParameter("realmId", realmId).getResultStream();
    }

    public void createAdmin(String eventId, String realmId) {
        em.createNativeQuery("INSERT INTO admin_event_not_send (realm_id,event_id) VALUES (:realmId, :eventId)").setParameter("eventId", eventId).setParameter("realmId", realmId).executeUpdate();
    }

    public void deleteAdminEntity(String eventId) {
        em.createNativeQuery("delete from admin_event_not_send where event_id = :eventId").setParameter("eventId", eventId).executeUpdate();
    }

    public Stream<Tuple> adminEventsNotSendByRealm(String realmId) {
        return em.createNativeQuery("select f.*  from ADMIN_EVENT_ENTITY f join ADMIN_EVENT_NOT_SEND e on f.id = e.event_id where f.realm_id= :realmId", Tuple.class).setParameter("realmId", realmId).getResultStream();
    }
}
