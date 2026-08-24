package com.yladraoui.ai.dto;


import java.time.LocalDateTime;

public record ConversationSummary(
        Long id,
        String title,
        LocalDateTime createdAt
) {
}
