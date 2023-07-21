package org.keycloak.metrics.jpa;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name="EVENT_NOT_SEND")
//@NamedQueries({
//        @NamedQuery(name="eventsNotSendByRealm", query="from EventNotSendEntity f where f.id.realm.id = :realmId")
//})
public class EventNotSendEntity {

    @EmbeddedId
    private EventNotSendId id;

    public EventNotSendEntity(){}

    public EventNotSendId getId() {
        return id;
    }

    public void setId(EventNotSendId id) {
        this.id = id;
    }
}
