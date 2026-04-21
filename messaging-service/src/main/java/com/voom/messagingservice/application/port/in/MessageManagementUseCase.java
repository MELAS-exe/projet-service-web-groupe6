package com.voom.messagingservice.application.port.in;

import com.voom.messagingservice.domain.model.Message;
import java.util.List;


public interface MessageManagementUseCase {
    List<Message> loadMessages(String clientId, String riderId, int page, int size);
    Message sendMessage(Message message);
    void markMessageAsRead(String messageId);
}