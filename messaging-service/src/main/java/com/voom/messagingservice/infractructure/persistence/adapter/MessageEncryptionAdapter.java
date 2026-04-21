package com.voom.messagingservice.infractructure.persistence.adapter;

import com.voom.messagingservice.application.port.out.MessageEncryptionPort;
import com.voom.messagingservice.domain.exception.DecryptionException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MessageEncryptionAdapter implements MessageEncryptionPort {
    @Value("${encryption.salt}")
    private String salt;

    @Value("${encryption.password}")
    private String password;

    @Override
    public String encryptMessage(String rawMessage) {
        TextEncryptor textEncryptor = Encryptors.text(password, salt);
        return textEncryptor.encrypt(rawMessage);
    }

    @Override
    public String decryptMessage(String encryptedMessage) {
        try {
            TextEncryptor textEncryptor = Encryptors.text(password, salt);
            return textEncryptor.decrypt(encryptedMessage);
        } catch (Exception e) {
            throw new DecryptionException("Error while decrypting message");
        }
    }
}
