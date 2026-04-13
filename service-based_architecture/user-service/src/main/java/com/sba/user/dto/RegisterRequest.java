package com.sba.user.dto;

import com.sba.common.enums.Role;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        Role role
) {
}
