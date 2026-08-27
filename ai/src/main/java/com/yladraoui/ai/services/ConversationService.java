package com.yladraoui.ai.services;


import com.yladraoui.ai.dto.ConversationDetailResponse;
import com.yladraoui.ai.dto.ConversationSummaryResponse;

import java.util.List;

public interface ConversationService {
    List<ConversationSummaryResponse> listConversations();
    ConversationDetailResponse getConversation(Long id);
    void deleteConversation(Long id);
}
