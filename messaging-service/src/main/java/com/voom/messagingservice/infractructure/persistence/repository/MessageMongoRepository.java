package com.voom.messagingservice.infractructure.persistence.repository;

import com.voom.messagingservice.infractructure.persistence.entity.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageMongoRepository extends MongoRepository<MessageEntity, String> {
    Page<MessageEntity> findByExpediteurIdAndDestinataireIdOrExpediteurIdAndDestinataireId(
            String sender1, String receiver1,
            String sender2, String receiver2,
            Pageable pageable
    );
}
