package com.yladraoui.ai;


import reactor.core.publisher.Flux;

import java.util.List;


/**
 * Port (in the hexagonal architecture sense) abstracting access to an
 * underlying LLM provider.
 * <p>
 * The rest of the application depends solely on this interface — never on
 * a concrete AI framework or vendor SDK. This keeps the domain and service
 * layers free of any third-party AI dependency and allows the underlying
 * provider (currently Groq via Spring AI's OpenAI-compatible client) to be
 * swapped without touching any calling code.
 *
 * @see SpringAiChatAdapter the sole implementation, and the only class in
 *      this project aware of Spring AI
 */
public interface AiChatPort {

    /**
     * Generates a complete reply synchronously from the given conversation
     * history.
     * <p>
     * The call blocks until the full response has been received from the
     * provider — suitable for use cases where the caller needs the entire
     * reply before proceeding (e.g. persisting it as a single message).
     *
     * @param history the full conversation so far, in chronological order;
     *                the last element is expected to be the newest user message
     * @return the assistant's complete reply as plain text
     */
    public String generateReply(List<ChatTurn> history);
}
