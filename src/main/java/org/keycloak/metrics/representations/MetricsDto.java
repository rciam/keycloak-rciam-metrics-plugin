package org.keycloak.metrics.representations;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.keycloak.events.Details;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.metrics.utils.MetricsUtils;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.RealmModel;
import org.keycloak.util.JsonSerialization;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetricsDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime date;
    private String voPersonId;
    private String entityId;
    private String idpName;
    private String identifier;
    private String spName;
    private String ipAddress;
    private String tenenvId;
    private String source;
    private String eventIdentifier;
    private String type;

    private String voName;
    private String voDescription;

    private Boolean failedLogin;

    private String status;

    public MetricsDto() {

    }

    public MetricsDto(Event event, RealmModel realm) throws Exception {
        this.date = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTime()), ZoneId.systemDefault());
        this.tenenvId = realm.getAttribute(MetricsUtils.TENENV_ID);
        this.source = realm.getAttribute(MetricsUtils.SOURCE);
        this.eventIdentifier = event.getId();
        String userIdentifier = realm.getAttribute(MetricsUtils.METRICS_USER_ID_ATTRIBUTE);
        this.voPersonId = event.getDetails().get(userIdentifier != null ? userIdentifier : Details.USERNAME);
        if ( this.voPersonId == null)
            throw new Exception(userIdentifier + " as userIdentifier does not exist in event");
        switch (event.getType().toString()) {
            case MetricsUtils.LOGIN:
                this.type = "login";
                this.failedLogin = Boolean.FALSE;
                setLogin(event, realm);
                break;
            case MetricsUtils.LOGIN_ERROR:
                this.type = "login";
                this.failedLogin = Boolean.TRUE;
                setLogin(event, realm);
                break;
            case MetricsUtils.REGISTER:
                this.type = "registration";
                break;
            case MetricsUtils.GROUP_MEMBERSHIP_CREATE:
                this.status = "A";
                setGroupMembership(event);
                break;
            case MetricsUtils.GROUP_MEMBERSHIP_DELETE:
                this.status = "XP";
                //or 'D"???
                setGroupMembership(event);
                break;
            case MetricsUtils.GROUP_MEMBERSHIP_SUSPEND:
                this.status = "S";
                setGroupMembership(event);
                break;
        }
    }

    public MetricsDto(AdminEvent event, GroupModel group, RealmModel realm) {
        this.date = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTime()), ZoneId.systemDefault());
        this.tenenvId = realm.getAttribute(MetricsUtils.TENENV_ID);
        this.source = realm.getAttribute(MetricsUtils.SOURCE);
        this.eventIdentifier = event.getId();
        this.status = "A";
        this.type = "vo";
        this.voName = group.getName();
        this.voDescription = group.getFirstAttribute(MetricsUtils.DESCRIPTION);
    }

    private void setLogin(Event event, RealmModel realm) throws Exception {
        this.ipAddress = event.getIpAddress();
        if (event.getDetails() == null)
            event.setDetails(new HashMap<>());
        if (event.getDetails().get(MetricsUtils.IDENTITY_PROVIDER_AUTHN_AUTHORITIES) != null) {
            AuthnAuthorityRepresentation firstAuthnAuthority = JsonSerialization.readValue(event.getDetails().get(MetricsUtils.IDENTITY_PROVIDER_AUTHN_AUTHORITIES),new TypeReference<LinkedList<AuthnAuthorityRepresentation>>(){}).getFirst();
            this.entityId = firstAuthnAuthority.getId();
            this.idpName  = firstAuthnAuthority.getName();
        } else if (event.getDetails().get(MetricsUtils.AUTHN_AUTHORITY) != null) {
            //authnAuthority of IdP in user session note (name = identity_provider_id)
            this.entityId = event.getDetails().get(MetricsUtils.AUTHN_AUTHORITY);
            this.idpName  = event.getDetails().get(MetricsUtils.IDP_NAME);
        } else if (event.getDetails().get(MetricsUtils.IDP_ALIAS) != null) {
            //idp
            String idpAlias = event.getDetails().get(MetricsUtils.IDP_ALIAS);
            IdentityProviderModel idp = realm.getIdentityProviderByAlias(idpAlias);
            if (idp != null) {
                this.entityId = idp.getConfig().get(MetricsUtils.ENTITY_ID) != null ? idp.getConfig().get(MetricsUtils.ENTITY_ID) : (idp.getConfig().get(MetricsUtils.ISSUER) != null ? idp.getConfig().get(MetricsUtils.ISSUER) : idpAlias);
                this.idpName = idp.getDisplayName() != null ? idp.getDisplayName() : idpAlias;
            }
        } else {
            //Keycloak user
            this.entityId = realm.getAttribute(MetricsUtils.KEYCLOAK_URL) + "/realms/" + realm.getName();
            this.idpName = realm.getDisplayName() != null ? realm.getDisplayName() : realm.getName();
        }
        this.identifier = event.getClientId();
        ClientModel client = realm.getClientByClientId(event.getClientId());
        if (client != null) {
            this.spName = client.getName();
        }

    }


    private void setGroupMembership(Event event) {
        this.type = "membership";
        this.voName = event.getDetails().get(MetricsUtils.EVENT_GROUP).split("/")[1];
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getVoPersonId() {
        return voPersonId;
    }

    public void setVoPersonId(String voPersonId) {
        this.voPersonId = voPersonId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSpName() {
        return spName;
    }

    public void setSpName(String spName) {
        this.spName = spName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getTenenvId() {
        return tenenvId;
    }

    public void setTenenvId(String tenenvId) {
        this.tenenvId = tenenvId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getFailedLogin() {
        return failedLogin;
    }

    public void setFailedLogin(Boolean failedLogin) {
        this.failedLogin = failedLogin;
    }

    public String getEventIdentifier() {
        return eventIdentifier;
    }

    public void setEventIdentifier(String eventIdentifier) {
        this.eventIdentifier = eventIdentifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIdpName() {
        return idpName;
    }

    public void setIdpName(String idpName) {
        this.idpName = idpName;
    }

    public String getVoName() {
        return voName;
    }

    public void setVoName(String voName) {
        this.voName = voName;
    }

    public String getVoDescription() {
        return voDescription;
    }

    public void setVoDescription(String voDescription) {
        this.voDescription = voDescription;
    }
}
