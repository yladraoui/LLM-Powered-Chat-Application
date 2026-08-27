package com.yladraoui.ai.dto;


import com.yladraoui.ai.models.SenderRole;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        SenderRole role,
        String content,
        LocalDateTime createdAt
) {
}
