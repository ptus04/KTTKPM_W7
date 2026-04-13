package com.sba.common.dto;

public record UserSummary(
        Long id,
        String username,
        String role
) {
}
