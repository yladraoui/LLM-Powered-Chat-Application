package com.yladraoui.ai;


import com.yladraoui.ai.models.SenderRole;

public record ChatTurn(
        SenderRole role,
        String content
) {
}
