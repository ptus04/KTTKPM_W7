package com.sba.payment.dto;

import com.sba.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull Long orderId,
        @NotNull PaymentMethod method
) {
}
