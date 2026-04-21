package com.project.paymentservice.service;

import org.springframework.stereotype.Service;

@Service
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public boolean pay(Double amount) {

        try {
            Thread.sleep(1000); // simulation API
        } catch (InterruptedException e) {}

        return Math.random() > 0.2; // 80% success
    }
}