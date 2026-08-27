package com.yladraoui.ai.dto;

import java.time.LocalDateTime;


public record ConversationSummaryResponse(
        Long id,
        String title,
        LocalDateTime createdAt,
        int messageCount
) {
}
