package com.yladraoui.ai.services;

import com.yladraoui.ai.AiChatPort;
import com.yladraoui.ai.ChatTurn;
import com.yladraoui.ai.dto.ChatRequest;
import com.yladraoui.ai.dto.ChatResponse;
import com.yladraoui.ai.exceptions.ConversationNotFoundException;
import com.yladraoui.ai.models.ChatMessage;
import com.yladraoui.ai.models.Conversation;
import com.yladraoui.ai.models.SenderRole;
import com.yladraoui.ai.repositories.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceDefault implements ChatService{

    private static final int TITLE_MAX_LENGTH = 50;
    private final ConversationRepository conversationRepository;
    private final AiChatPort aiChatPort;

    public ChatServiceDefault(ConversationRepository conversationRepository, AiChatPort aiChatPort) {
        this.conversationRepository = conversationRepository;
        this.aiChatPort = aiChatPort;
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
}
