package com.project.paymentservice.service;

import com.project.paymentservice.entity.Payment;
import com.project.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import com.project.paymentservice.exception.PaymentNotFoundException;

import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final PaymentProvider provider;
    private final PaymentEventProducer producer;

    public PaymentService(PaymentRepository repository,
                          PaymentProvider provider,
                          PaymentEventProducer producer) {
        this.repository = repository;
        this.provider = provider;
        this.producer = producer;
    }

    public Payment createPayment(Payment payment) {

        payment.setStatus("PENDING");

        boolean success = provider.pay(payment.getAmount());

        if (success) {
            payment.setStatus("SUCCESS");
            payment.setTransactionRef(UUID.randomUUID().toString());

            //  EVENT SUCCESS
            producer.sendPaymentDone(
                    payment.getRideId(),
                    payment.getUserId(),
                    payment.getAmount()
            );

        } else {
            payment.setStatus("FAILED");

            //  EVENT FAILED
            producer.sendPaymentFailed(
                    payment.getRideId(),
                    payment.getUserId(),
                    payment.getAmount()
            );
        }

        return repository.save(payment);
    }
    public Payment getPayment(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with id " + id + " not found"));
    }
}