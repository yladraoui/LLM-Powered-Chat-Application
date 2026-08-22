package com.yladraoui.ai.dto;

import org.jetbrains.annotations.NotNull;

/**
 * @author $ {USER}
 **/
public record ChatRequest(
        Long conversationId,
        @NotNull
        String message
) {
}
