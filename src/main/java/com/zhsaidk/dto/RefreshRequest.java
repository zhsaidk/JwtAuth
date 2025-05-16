package com.zhsaidk.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class RefreshRequest {
    @NotBlank(message = "Refresh token required")
    String refresh_token;
}
