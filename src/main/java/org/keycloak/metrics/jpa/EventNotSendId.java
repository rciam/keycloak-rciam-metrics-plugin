package org.keycloak.metrics.jpa;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.keycloak.events.jpa.EventEntity;
import org.keycloak.models.jpa.entities.RealmEntity;
import org.keycloak.models.jpa.entities.UserEntity;

@Embeddable
public class EventNotSendId implements Serializable {

    @ManyToOne()
    @JoinColumn(name = "REALM_ID")
    protected RealmEntity realm;

    @ManyToOne()
    @JoinColumn(name = "EVENT_ID")
    protected EventEntity event;

    public EventNotSendId(String realmId, String eventId) {
        RealmEntity realmEntity = new RealmEntity();
        realmEntity.setId(realmId);
        EventEntity eventEntity = new EventEntity();
        eventEntity.setId(eventId);
        this.realm = realmEntity;
        this.event = eventEntity;
    }

    public EventNotSendId(){

    }

    public RealmEntity getRealm() {
        return realm;
    }

    public void setRealm(RealmEntity realm) {
        this.realm = realm;
    }

    public EventEntity getEvent() {
        return event;
    }

    public void setEvent(EventEntity event) {
        this.event = event;
    }
}
