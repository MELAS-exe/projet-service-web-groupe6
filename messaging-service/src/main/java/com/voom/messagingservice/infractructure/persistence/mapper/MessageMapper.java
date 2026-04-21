package com.voom.messagingservice.infractructure.persistence.mapper;

import com.voom.messagingservice.domain.model.Message;
import com.voom.messagingservice.infractructure.persistence.entity.MessageEntity;
import com.voom.messagingservice.infractructure.web.dto.MessageResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    Message toDomain(MessageEntity entity);
    MessageEntity toEntity(Message domain);
    MessageResponse toResponse(Message domain);
}
