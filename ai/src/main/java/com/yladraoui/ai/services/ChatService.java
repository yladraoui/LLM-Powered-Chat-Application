package com.yladraoui.ai.services;

import com.yladraoui.ai.dto.ChatRequest;
import com.yladraoui.ai.dto.ChatResponse;
import com.yladraoui.ai.models.Conversation;
import com.yladraoui.ai.repositories.ConversationRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final ChatClient chatClient;
    private final ConversationRepository conversationRepository;

    public ChatService(ChatClient.Builder chatClientBuilder,
                       ConversationRepository conversationRepository) {
        this.chatClient = chatClientBuilder.build();
        this.conversationRepository = conversationRepository;
    }

    public ChatResponse chat(ChatRequest request) {
        Conversation conversation = resolveConversation(request);

        // Historique existant, transformé en messages Spring AI pour garder le contexte
        List<Message> history = conversation.getMessages().stream()
                .map(this::toSpringAiMessage)
                .collect(Collectors.toList());

        // Ajoute le nouveau message utilisateur
        history.add(new UserMessage(request.message()));

        String reply = chatClient.prompt(new Prompt(history))
                .call()
                .content();

        // Persistance des deux messages (user + assistant)
        conversation.addMessage(new Message(Message.Role.USER, request.message()));
        conversation.addMessage(new Message(Message.Role.ASSISTANT, reply));
        conversationRepository.save(conversation);

        return new ChatResponse(conversation.getId(), reply);
    }

    private Conversation resolveConversation(ChatRequest request) {
        if (request.conversationId() != null) {
            return conversationRepository.findById(request.conversationId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Conversation introuvable: " + request.conversationId()));
        }
        String title = request.message().length() > 40
                ? request.message().substring(0, 40) + "..."
                : request.message();
        return conversationRepository.save(new Conversation(title));
    }

    private Message toSpringAiMessage(com.yladraoui.ai.models.Message m) {
        return m.getRole() == Message.Role.USER
                ? new UserMessage(m.getContent())
                : new AssistantMessage(m.getContent());
    }
}
