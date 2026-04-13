package com.sba.payment.dto;

public record PaymentResponse(
        Long paymentId,
        String status,
        String message
) {
}
