package com.voom.messagingservice.application.service;

import com.voom.messagingservice.application.port.in.MessageManagementUseCase;
import com.voom.messagingservice.application.port.out.MessageEncryptionPort;
import com.voom.messagingservice.application.port.out.MessagePort;
import com.voom.messagingservice.domain.exception.MessageNotFoundException;
import lombok.RequiredArgsConstructor;
import com.voom.messagingservice.domain.model.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService implements MessageManagementUseCase {
    private final MessageEncryptionPort messageEncryptionPort;
    private final MessagePort messagePort;

    @Override
    public List<Message> loadMessages(String clientId, String riderId, int page, int size) {
        return messagePort.loadMessages(clientId, riderId, page, size)
                .stream()
                .map(message -> {
                    String decryptedMessage = messageEncryptionPort.decryptMessage(message.getContenu());
                    message.setContenu(decryptedMessage);
                    return message;
                }).toList();
    }

    @Override
    public Message sendMessage(Message message) {
        message.setId(UUID.randomUUID().toString());
        String encryptedMessage = messageEncryptionPort.encryptMessage(message.getContenu());
        message.setContenu(encryptedMessage);
        message.setLu(Boolean.FALSE);
        message.setDateEnvoi(LocalDateTime.now());
        messagePort.saveMessage(message); //save the encrypted content to the database
        message.setContenu(messageEncryptionPort.decryptMessage(encryptedMessage)); //return the decrypted message to the client
        return message;
    }

    @Override
    public void markMessageAsRead(String messageId) {
        Message readMessage = messagePort.findMessageById(messageId)
                .orElseThrow(
                        () -> new MessageNotFoundException("Message with id: " + messageId + " not found"));
        readMessage.setLu(Boolean.TRUE);
        messagePort.saveMessage(readMessage);
    }

}
