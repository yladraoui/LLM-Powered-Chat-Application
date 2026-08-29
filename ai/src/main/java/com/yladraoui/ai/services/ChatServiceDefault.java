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
import com.yladraoui.ai.repositories.ChatMessageRepository;
import com.yladraoui.ai.repositories.ConversationRepository;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import tools.jackson.databind.json.JsonMapper;


import java.util.List;

@Service
public class ChatServiceDefault implements ChatService{

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiChatPort aiChatPort;
    private final AiChatStreamPort aiChatStreamPort;
    private final AssistantReplyRecorder assistantReplyRecorder;
    private final JsonMapper jsonMapper;

    public ChatServiceDefault(ConversationRepository conversationRepository,
                           ChatMessageRepository chatMessageRepository,
                           AiChatPort aiChatPort,
                           AiChatStreamPort aiChatStreamPort,
                           AssistantReplyRecorder assistantReplyRecorder,
                           JsonMapper jsonMapper) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.aiChatPort = aiChatPort;
        this.aiChatStreamPort = aiChatStreamPort;
        this.assistantReplyRecorder = assistantReplyRecorder;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        Long conversationId = resolveConversationId(request);

        chatMessageRepository.save(
                new ChatMessage(conversationRepository.getReferenceById(conversationId), SenderRole.USER, request.message())
        );

        List<ChatTurn> history = loadHistory(conversationId);
        String reply = aiChatPort.generateReply(history);

        chatMessageRepository.save(
                new ChatMessage(conversationRepository.getReferenceById(conversationId), SenderRole.ASSISTANT, reply)
        );

        return new ChatResponse(conversationId, reply);
    }

    @Override
    public Flux<ServerSentEvent<String>> streamChat(ChatRequest request) {
        Long conversationId = resolveConversationId(request);

        chatMessageRepository.save(
                new ChatMessage(conversationRepository.getReferenceById(conversationId), SenderRole.USER, request.message())
        );

        List<ChatTurn> history = loadHistory(conversationId);
        StringBuilder fullReply = new StringBuilder();

        Flux<ServerSentEvent<String>> meta = Flux.just(sse(ChatStreamEventType.CONVERSATION, conversationId.toString()));

        Flux<ServerSentEvent<String>> chunks = aiChatStreamPort.streamReply(history)
                .doOnNext(fullReply::append)
                .map(token -> sse(ChatStreamEventType.CHUNK, toJson(token)))
                .doOnComplete(() -> assistantReplyRecorder.record(conversationId, fullReply.toString()))
                .onErrorResume(ex -> Flux.just(sse(ChatStreamEventType.STREAM_ERROR, toJson("La génération a échoué"))));

        Flux<ServerSentEvent<String>> done = Flux.just(sse(ChatStreamEventType.DONE, ""));

        return Flux.concat(meta, chunks, done);
    }

    private Long resolveConversationId(ChatRequest request) {
        if (request.conversationId() != null) {
            if (!conversationRepository.existsById(request.conversationId())) {
                throw new ConversationNotFoundException(request.conversationId());
            }
            return request.conversationId();
        }
        String title = request.message().length() > 40
                ? request.message().substring(0, 40) + "..."
                : request.message();
        return conversationRepository.save(new Conversation(title)).getId();
    }

    private List<ChatTurn> loadHistory(Long conversationId) {
        return chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(m -> new ChatTurn(m.getRole(), m.getContent()))
                .toList();
    }

    private String toJson(String value) {
        return jsonMapper.writeValueAsString(value);
    }

    private ServerSentEvent<String> sse(ChatStreamEventType type, String data) {
        return ServerSentEvent.<String>builder()
                .event(type.name().toLowerCase().replace('_', '-'))
                .data(data)
                .build();
    }
}
