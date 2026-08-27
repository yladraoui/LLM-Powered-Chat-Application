package com.yladraoui.ai.dto;

import java.time.LocalDateTime;
import java.util.List;


public record ConversationDetailResponse(
        Long id,
        String title,
        LocalDateTime createdAt,
        List<MessageResponse> messages
) {
}
