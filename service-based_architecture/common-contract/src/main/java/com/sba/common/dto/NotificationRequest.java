package com.sba.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull Long userId,
        @NotNull Long orderId,
        @NotBlank String message
) {
}
