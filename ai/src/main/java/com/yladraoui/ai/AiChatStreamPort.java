package com.yladraoui.ai;


import reactor.core.publisher.Flux;

import java.util.List;

public interface AiChatStreamPort {

    /**
     * Generates a reply as a stream of incremental text chunks (Server-Sent
     * Events use case).
     * <p>
     * This method does not wait for the
     * full response: chunks are emitted to the returned {@link Flux} as soon
     * as they arrive from the provider, allowing callers to forward them to
     * the client in real time.
     *
     * @param history the full conversation so far, in chronological order;
     *                the last element is expected to be the newest user message
     * @return a reactive stream emitting successive fragments of the
     *         assistant's reply, completing once the full reply has been sent
     */
    Flux<String> streamReply(List<ChatTurn> history);
}
