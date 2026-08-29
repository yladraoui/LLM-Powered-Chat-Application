package com.yladraoui.ai.services;


import com.yladraoui.ai.dto.ChatRequest;
import com.yladraoui.ai.dto.ChatResponse;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface ChatService {
    public ChatResponse chat(ChatRequest request);
    Flux<ServerSentEvent<String>> streamChat(ChatRequest request);
}
