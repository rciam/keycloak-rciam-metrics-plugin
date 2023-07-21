package org.keycloak.metrics.representations;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.keycloak.events.Event;
import org.keycloak.metrics.utils.MetricsUtils;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;

public class MetricsDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime date;
    private String voPersonId;
    private String entityId;
    private String identifier;
    private String spName;
    private String ipAddress;
    private String tenenvId;
    private String source;
    private String eventIdentifier;
    private String type;

    @JsonProperty("failed_login")
    private Boolean failedLogin;

    private String status;

    public MetricsDto() {

    }

    public MetricsDto(Event event, RealmModel realm){
        this.date = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTime()), ZoneId.systemDefault());
        this.tenenvId = realm.getAttribute(MetricsUtils.TENENV_ID);
        this.source = realm.getAttribute(MetricsUtils.SOURCE);
        this.eventIdentifier = event.getId();
        switch (event.getType()) {
            case LOGIN:
                this.type = "login";
                this.failedLogin = false;
                setLogin(event, realm);
                break;
            case LOGIN_ERROR:
                this.type = "login";
                this.failedLogin = true;
                setLogin(event, realm);
                break;
            case REGISTER:
                this.type = "registration";
                this.voPersonId = event.getDetails().get(MetricsUtils.VO_PERSON_ID);
                break;
        }
    }

    private void setLogin(Event event, RealmModel realm){
        this.ipAddress = event.getIpAddress();
        this.voPersonId = event.getDetails().get(MetricsUtils.VO_PERSON_ID);
        this.entityId = event.getDetails().get(MetricsUtils.AUTHN_AUTHORITY);
        this.identifier =  event.getClientId();
        ClientModel client = realm.getClientByClientId(event.getClientId());
        if (client != null) {
            this.spName = client.getName();
        }

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

    public boolean getFailedLogin() {
        return failedLogin;
    }

    public void setFailedLogin(boolean failedLogin) {
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
}
