package com.yladraoui.ai.services;

import com.yladraoui.ai.models.ChatMessage;
import com.yladraoui.ai.models.Conversation;
import com.yladraoui.ai.models.SenderRole;
import com.yladraoui.ai.repositories.ChatMessageRepository;
import com.yladraoui.ai.repositories.ConversationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Isolated in its own bean: @Transactional only works if the call
 * goes through the Spring proxy, so no self-invocation from ChatServiceImpl.
 * Executes on a Reactor thread, independently of any session bound
 * to the original HTTP request thread.
 */
@Component
public class AssistantReplyRecorder {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;

    public AssistantReplyRecorder(ConversationRepository conversationRepository,
                                  ChatMessageRepository chatMessageRepository) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Transactional
    public void record(Long conversationId, String reply) {
        Conversation reference = conversationRepository.getReferenceById(conversationId);
        chatMessageRepository.save(new ChatMessage(reference, SenderRole.ASSISTANT, reply));
    }
}