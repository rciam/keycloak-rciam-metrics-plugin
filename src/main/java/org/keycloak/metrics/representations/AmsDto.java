package org.keycloak.metrics.representations;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AmsDto {

    private List<MessagesDto> messages;

    public AmsDto(){

    }

    public AmsDto(MessagesDto message){
        messages = Stream.of(message).collect(Collectors.toList());
    }

    public List<MessagesDto> getMessages() {
        return messages;
    }

    public void setMessages(List<MessagesDto> messages) {
        this.messages = messages;
    }
}
