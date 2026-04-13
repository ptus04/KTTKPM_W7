package com.sba.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull Long foodId,
        @Min(1) int quantity
) {
}
