package com.yladraoui.ai;

import com.yladraoui.ai.models.SenderRole;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
public class SpringAiChatAdapter implements AiChatPort {

    private final ChatClient chatClient;

    public SpringAiChatAdapter(ChatClient.Builder chatClientBuilder){
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String generateReply(List<ChatTurn> history) {
        List<Message> springAiMessages = history.stream()
                .map(this::toSpringAiMessage)
                .toList();
        return chatClient.prompt(new Prompt(springAiMessages))
                .call()
                .content();
    }

    private Message toSpringAiMessage(ChatTurn turn){
        return turn.role() == SenderRole.USER
                ? new UserMessage(turn.content())
                : new AssistantMessage(turn.content());
    }

    @Override
    public Flux<String> streamReply(List<ChatTurn> history) {
        List<Message> springAiMessages = history.stream()
                .map(this::toSpringAiMessage)
                .toList();
        return chatClient.prompt(new Prompt(springAiMessages))
                .stream()
                .content();
    }
}
