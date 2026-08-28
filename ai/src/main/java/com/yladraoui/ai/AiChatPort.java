package com.yladraoui.ai;


import reactor.core.publisher.Flux;

import java.util.List;

public interface AiChatPort {
    public String generateReply(List<ChatTurn> history);
    Flux<String> streamReply(List<ChatTurn> history);
}
