package org.keycloak.metrics.representations;

public class MessagesDto {

    private String data;

    public MessagesDto(String data)  {
        this.data =  data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
