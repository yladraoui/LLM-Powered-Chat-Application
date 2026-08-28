package com.yladraoui.ai;


import com.yladraoui.ai.models.SenderRole;


/**
 * Provider-agnostic representation of a single turn in a conversation.
 * <p>
 * This record decouples the application's domain model from any specific
 * AI framework or vendor SDK (e.g. Spring AI's {@code Message} hierarchy).
 * It is the shared language between the persistence layer, the service
 * layer, and the {@link AiChatPort} implementation, ensuring that no
 * Spring AI type leaks outside the {@code ai} package.
 *
 * @param role    who authored this turn — either the end user or the assistant
 * @param content the raw text content of the turn
 */
public record ChatTurn(
        SenderRole role,
        String content
) {
}
