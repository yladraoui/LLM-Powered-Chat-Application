package com.yladraoui.ai.services;


import com.yladraoui.ai.dto.ChatRequest;
import com.yladraoui.ai.dto.ChatResponse;
import reactor.core.publisher.Flux;

public interface ChatService {
    public ChatResponse chat(ChatRequest request);
    Flux<String> streamChat(ChatRequest request);
}
