package com.yladraoui.ai.dto;

import jakarta.validation.constraints.NotBlank;


public record ChatRequest(
        Long conversationId,
        @NotBlank(message = "The message can't be validated")
        String message
) {
}
