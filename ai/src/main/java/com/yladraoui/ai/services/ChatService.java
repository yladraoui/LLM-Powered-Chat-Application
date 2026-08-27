package com.yladraoui.ai.services;


import com.yladraoui.ai.dto.ChatRequest;
import com.yladraoui.ai.dto.ChatResponse;

public interface ChatService {
    public ChatResponse chat(ChatRequest request);
}
