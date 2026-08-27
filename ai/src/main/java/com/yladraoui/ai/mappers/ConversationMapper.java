package com.yladraoui.ai.mappers;


import com.yladraoui.ai.dto.ConversationDetailResponse;
import com.yladraoui.ai.dto.ConversationSummaryResponse;
import com.yladraoui.ai.dto.MessageResponse;
import com.yladraoui.ai.models.ChatMessage;
import com.yladraoui.ai.models.Conversation;
import org.springframework.stereotype.Component;

@Component
public class ConversationMapper {
    public ConversationSummaryResponse toSummary(Conversation conversation){
        return new ConversationSummaryResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getMessages().size()
        );
    }
    public ConversationDetailResponse toDetail(Conversation conversation){
        return new ConversationDetailResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getMessages().stream()
                        .map(this::toMessageResponse)
                        .toList()
        );
    }

    private MessageResponse toMessageResponse(ChatMessage message) {
        return new MessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
        );
    }


}
