package com.yladraoui.ai.controllers;

import com.yladraoui.ai.dto.ChatRequest;
import com.yladraoui.ai.dto.ChatResponse;
import com.yladraoui.ai.services.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.chat(request));
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(
            @RequestParam(required = false) Long conversationId,
            @RequestParam String message
    ) {
        return chatService.streamChat(new ChatRequest(conversationId, message));
    }
}
