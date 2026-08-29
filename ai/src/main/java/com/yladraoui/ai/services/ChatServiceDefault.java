package com.yladraoui.ai.services;

import com.yladraoui.ai.AiChatPort;
import com.yladraoui.ai.AiChatStreamPort;
import com.yladraoui.ai.ChatTurn;
import com.yladraoui.ai.dto.ChatRequest;
import com.yladraoui.ai.dto.ChatResponse;
import com.yladraoui.ai.dto.ChatStreamEventType;
import com.yladraoui.ai.exceptions.ConversationNotFoundException;
import com.yladraoui.ai.models.ChatMessage;
import com.yladraoui.ai.models.Conversation;
import com.yladraoui.ai.models.SenderRole;
import com.yladraoui.ai.repositories.ConversationRepository;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;


import java.util.List;

@Service
public class ChatServiceDefault implements ChatService{

    private static final int TITLE_MAX_LENGTH = 50;
    private final ConversationRepository conversationRepository;
    private final AiChatPort aiChatPort;
    private final AiChatStreamPort aiChatStreamPort;

    public ChatServiceDefault(
            ConversationRepository conversationRepository,
            AiChatPort aiChatPort,
            AiChatStreamPort aiChatStreamPort
    ) {
        this.conversationRepository = conversationRepository;
        this.aiChatPort = aiChatPort;
        this.aiChatStreamPort = aiChatStreamPort;
    }

    @Override
    @Transactional
    public ChatResponse chat(ChatRequest request) {
        Conversation conversation = resolveConversation(request);
        conversation.appendMessage(new ChatMessage(SenderRole.USER, request.message()));

        String reply = aiChatPort.generateReply(
                conversation.getMessages().stream()
                        .map(m -> new ChatTurn(m.getRole(), m.getContent()))
                        .toList()
        );

        conversation.appendMessage(new ChatMessage(SenderRole.ASSISTANT, reply));

        Conversation saved = conversationRepository.save(conversation);

        return new ChatResponse(saved.getId(), reply);
    }

    private Conversation resolveConversation(ChatRequest request) {
        if (request.conversationId() == null) {
            return new Conversation(buildTitle(request.message()));
        }
        return conversationRepository.findById(request.conversationId())
                .orElseThrow(() -> new ConversationNotFoundException(request.conversationId()));
    }

    private String buildTitle(String firstMessage) {
        String trimmed = firstMessage.trim();
        return trimmed.length() <= TITLE_MAX_LENGTH
                ? trimmed
                : trimmed.substring(0, TITLE_MAX_LENGTH) + "…";
    }

    //Streaming method
    @Override
    @Transactional
    public Flux<ServerSentEvent<String>> streamChat(ChatRequest request) {
        Conversation conversation = resolveConversation(request);
        conversation.appendMessage(new ChatMessage(SenderRole.USER, request.message()));
        conversationRepository.save(conversation);

        Long conversationId = conversation.getId();
        List<ChatTurn> history = toChatTurns(conversation);
        StringBuilder fullReply = new StringBuilder();

        Flux<ServerSentEvent<String>> meta = Flux.just(
                sse(ChatStreamEventType.CONVERSATION, conversationId.toString())
        );

        Flux<ServerSentEvent<String>> chunks = aiChatStreamPort.streamReply(history)
                .doOnNext(fullReply::append)
                .map(token -> sse(ChatStreamEventType.CHUNK, token))
                .doOnComplete(() -> persistAssistantReply(conversationId, fullReply.toString()));

        Flux<ServerSentEvent<String>> done = Flux.just(sse(ChatStreamEventType.DONE, ""));

        return Flux.concat(meta, chunks, done);
    }

    private ServerSentEvent<String> sse(ChatStreamEventType type, String data) {
        return ServerSentEvent.<String>builder()
                .event(type.name().toLowerCase())
                .data(data)
                .build();
    }

    private void persistAssistantReply(Long conversationId, String reply) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));
        conversation.appendMessage(new ChatMessage(SenderRole.ASSISTANT, reply));
        conversationRepository.save(conversation);
    }

    private List<ChatTurn> toChatTurns(Conversation conversation) {
        return conversation.getMessages().stream()
                .map(m -> new ChatTurn(m.getRole(), m.getContent()))
                .toList();
    }
}
