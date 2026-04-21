package com.voom.messagingservice.infractructure.persistence.adapter;

import com.voom.messagingservice.application.port.out.MessagePort;
import com.voom.messagingservice.domain.model.Message;
import com.voom.messagingservice.infractructure.persistence.entity.MessageEntity;
import com.voom.messagingservice.infractructure.persistence.mapper.MessageMapper;
import com.voom.messagingservice.infractructure.persistence.repository.MessageMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MessageAdapter implements MessagePort {
    private final MessageMongoRepository messageMongoRepository;
    private final MessageMapper messageMapper;

    @Override
    public List<Message> loadMessages(String clientId, String riderId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageMongoRepository.findByExpediteurIdAndDestinataireIdOrExpediteurIdAndDestinataireId(
                clientId, riderId,
                riderId, clientId,
                pageable).stream().map(messageMapper::toDomain).toList();
    }

    @Override
    public Message saveMessage(Message message) {
        MessageEntity savedEntity = messageMongoRepository.save(messageMapper.toEntity(message));
        return messageMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Message> findMessageById(String messageId) {
        return messageMongoRepository.findById(messageId).map(messageMapper::toDomain);
    }
}
