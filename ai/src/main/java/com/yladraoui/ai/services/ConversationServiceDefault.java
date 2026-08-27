package com.yladraoui.ai.services;

import com.yladraoui.ai.dto.ConversationDetailResponse;
import com.yladraoui.ai.dto.ConversationSummaryResponse;
import com.yladraoui.ai.exceptions.ConversationNotFoundException;
import com.yladraoui.ai.mappers.ConversationMapper;
import com.yladraoui.ai.models.Conversation;
import com.yladraoui.ai.repositories.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConversationServiceDefault implements ConversationService{

    private final ConversationRepository conversationRepository;
    private final ConversationMapper mapper;

    public ConversationServiceDefault(ConversationRepository conversationRepository, ConversationMapper mapper) {
        this.conversationRepository = conversationRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> listConversations() {
        return conversationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(mapper::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationDetailResponse getConversation(Long id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new ConversationNotFoundException(id));
        return mapper.toDetail(conversation);
    }

    @Override
    @Transactional
    public void deleteConversation(Long id) {
        if (!conversationRepository.existsById(id)) {
            throw new ConversationNotFoundException(id);
        }
        conversationRepository.deleteById(id);

    }
}
