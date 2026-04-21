package com.voom.messagingservice.infractructure.web.controller;

import com.voom.messagingservice.application.port.in.MessageManagementUseCase;
import com.voom.messagingservice.domain.model.Message;
import com.voom.messagingservice.infractructure.persistence.mapper.MessageMapper;
import com.voom.messagingservice.infractructure.web.dto.MessageRequest;
import com.voom.messagingservice.infractructure.web.dto.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageManagementUseCase messageManagementUseCase;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageMapper messageMapper;

    // 1. Point d'entrée REST : Récupérer l'historique des messages
    @GetMapping("/api/messages")
    public ResponseEntity<List<MessageResponse>> loadMessages(
            @RequestParam String clientId,
            @RequestParam String riderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Message> messages = messageManagementUseCase.loadMessages(clientId, riderId, page, size);

        List<MessageResponse> response = messages.stream()
                .map(messageMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @MessageMapping("/chat")
    public void sendMessage(@Valid @Payload MessageRequest request) {
        Message domainMessage = Message.builder()
                .expediteurId(request.expediteurId())
                .destinataireId(request.destinataireId())
                .trajetId(request.trajetId())
                .contenu(request.contenu())
                .build();

        Message savedMessage = messageManagementUseCase.sendMessage(domainMessage);

        MessageResponse response = messageMapper.toResponse(savedMessage);

        messagingTemplate.convertAndSend("/topic/trajet/" + response.trajetId(), response);
    }

    @PatchMapping("/api/messages/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String id) {
        messageManagementUseCase.markMessageAsRead(id);
        return ResponseEntity.noContent().build();
    }

}