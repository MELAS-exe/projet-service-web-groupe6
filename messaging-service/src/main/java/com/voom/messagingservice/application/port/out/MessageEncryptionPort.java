package com.voom.messagingservice.application.port.out;

import org.apache.hc.core5.http.Message;

public interface MessageEncryptionPort {
    String encryptMessage(String rawMessage);
    String decryptMessage(String encryptedMessage);
}
