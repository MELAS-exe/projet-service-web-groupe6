package com.project.paymentservice.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long rideId;
    private Long userId;
    private Double amount;
    private String currency;
    private String method;
}