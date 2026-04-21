package com.voom.messagingservice.application.port.out;


import com.voom.messagingservice.domain.model.Message;

import java.util.List;
import java.util.Optional;

public interface MessagePort {
    List<Message> loadMessages(String clientId, String riderId, int page, int size);
    Message saveMessage(Message message);
    Optional<Message> findMessageById(String messageId);
}
