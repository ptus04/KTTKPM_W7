package com.sba.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record FoodDto(
        Long id,
        @NotBlank String name,
        @Min(0) Double price
) {
}
