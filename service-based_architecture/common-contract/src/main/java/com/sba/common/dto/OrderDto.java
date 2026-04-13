package com.sba.common.dto;

import com.sba.common.enums.OrderStatus;

public record OrderDto(
        Long id,
        Long userId,
        Double total,
        OrderStatus status
) {
}
