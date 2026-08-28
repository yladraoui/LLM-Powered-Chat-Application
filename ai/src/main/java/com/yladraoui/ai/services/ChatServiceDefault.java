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
import reactor.core.publisher.Flux;


import java.util.List;

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

    //Streaming method
    @Override
    @Transactional
    public Flux<String> streamChat(ChatRequest request) {
        Conversation conversation = resolveConversation(request);

        // Step 1 : Save the user message
        conversation.appendMessage(new ChatMessage(SenderRole.USER, request.message()));
        conversationRepository.save(conversation);

        Long conversationId = conversation.getId();

        // this variable is for save the assistant reply chunk by chunk
        StringBuilder fullReply = new StringBuilder();

        List<ChatTurn> history = conversation.getMessages().stream()
                .map(m -> new ChatTurn(m.getRole(), m.getContent()))
                .toList();

        return aiChatPort.streamReply(history)
                .doOnNext(chunk -> fullReply.append(chunk))  // add every chunk to the variable that we will save
                .doOnComplete(() -> {
                    //Now, all is done, we have the entire reply, we can save it.
                    Conversation freshConversation = conversationRepository.findById(conversationId).orElseThrow();
                    freshConversation.appendMessage(new ChatMessage(SenderRole.ASSISTANT, fullReply.toString()));
                    conversationRepository.save(freshConversation);
                });
    }
}
