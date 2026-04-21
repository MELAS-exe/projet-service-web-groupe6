package com.project.paymentservice.service;

import org.springframework.stereotype.Service;

@Service
public class PaymentEventProducer {

    // Simulation (sans Kafka pour l’instant)

    public void sendPaymentDone(Long rideId, Long userId, Double amount) {

        String event = buildEvent("payment.done", rideId, userId, amount);

        System.out.println("EVENT SENT => " + event);
    }

    public void sendPaymentFailed(Long rideId, Long userId, Double amount) {

        String event = buildEvent("payment.failed", rideId, userId, amount);

        System.out.println("EVENT SENT => " + event);
    }

    private String buildEvent(String type, Long rideId, Long userId, Double amount) {
        return "{"
                + "\"event\":\"" + type + "\","
                + "\"rideId\":" + rideId + ","
                + "\"userId\":" + userId + ","
                + "\"amount\":" + amount +
                "}";
    }
}