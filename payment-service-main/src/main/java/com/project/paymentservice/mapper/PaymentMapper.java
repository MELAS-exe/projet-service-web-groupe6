package com.project.paymentservice.mapper;

import com.project.paymentservice.entity.Payment;
import com.project.paymentservice.dto.PaymentRequest;
import com.project.paymentservice.dto.PaymentResponse;

public class PaymentMapper {

    // DTO --> Entity
    public static Payment toEntity(PaymentRequest request) {
        Payment payment = new Payment();

        payment.setRideId(request.getRideId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setMethod(request.getMethod());

        return payment;
    }

    // Entity --> Response DTO
    public static PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();

        response.setId(payment.getId());
        response.setStatus(payment.getStatus());
        response.setTransactionRef(payment.getTransactionRef());

        return response;
    }
}