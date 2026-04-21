package com.project.paymentservice.controller;

import com.project.paymentservice.entity.Payment;
import com.project.paymentservice.service.PaymentService;
import org.springframework.web.bind.annotation.*;
import com.project.paymentservice.dto.PaymentResponse;
import com.project.paymentservice.mapper.PaymentMapper;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    public Payment create(@RequestBody Payment payment) {
        return service.createPayment(payment);
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable Long id) {

        Payment payment = service.getPayment(id);

        return PaymentMapper.toResponse(payment);
    }
}