package com.sba.payment.controller;

import com.sba.payment.dto.PaymentRequest;
import com.sba.payment.dto.PaymentResponse;
import com.sba.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> pay(@Valid @RequestBody PaymentRequest request,
                                               @RequestHeader("Authorization") String bearerToken) {
        return ResponseEntity.ok(paymentService.pay(request, bearerToken));
    }
}
