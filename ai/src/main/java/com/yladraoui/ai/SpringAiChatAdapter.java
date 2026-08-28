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


/**
 * {@link AiChatPort} implementation backed by Spring AI.
 * <p>
 * This is the only class in the project that imports Spring AI types
 * ({@link ChatClient}, {@link Prompt}, {@link Message}). It acts as the
 * adapter (in the port/adapter sense) between the application's
 * provider-agnostic {@link ChatTurn} model and Spring AI's own message
 * types, translating between the two in both directions.
 * <p>
 * The underlying {@link ChatClient} is auto-configured by Spring Boot from
 * the {@code spring.ai.openai.*} properties (API key, base URL, model),
 * which currently point to Groq's OpenAI-compatible endpoint. Swapping
 * providers therefore only requires reconfiguring these properties, or at
 * most replacing this single class — no other class in the application is
 * aware that Spring AI, or Groq specifically, is being used.
 */
@Component
public class SpringAiChatAdapter implements AiChatPort {

    private final ChatClient chatClient;


    /**
     * Builds the adapter's {@link ChatClient} from the auto-configured
     * {@link ChatClient.Builder} that Spring Boot provides based on the
     * {@code spring.ai.openai.*} configuration.
     *
     * @param chatClientBuilder the Spring AI-managed builder, pre-configured
     *                          with the provider's API key, base URL and
     *                          default chat options
     */
    public SpringAiChatAdapter(ChatClient.Builder chatClientBuilder){
        this.chatClient = chatClientBuilder.build();
    }


    /**
     * {@inheritDoc}
     * <p>
     * Delegates to Spring AI's blocking {@code call().content()} API, which
     * waits for the provider to return the full completion before returning.
     */
    @Override
    public String generateReply(List<ChatTurn> history) {
        List<Message> springAiMessages = history.stream()
                .map(this::toSpringAiMessage)
                .toList();
        return chatClient.prompt(new Prompt(springAiMessages))
                .call()
                .content();
    }


    /**
     * Converts a provider-agnostic {@link ChatTurn} into the corresponding
     * Spring AI {@link Message} subtype, based on who authored the turn.
     *
     * @param turn the turn to convert
     * @return a {@link UserMessage} if authored by the end user,
     *         or an {@link AssistantMessage} if authored by the assistant
     */
    private Message toSpringAiMessage(ChatTurn turn){
        return turn.role() == SenderRole.USER
                ? new UserMessage(turn.content())
                : new AssistantMessage(turn.content());
    }


    /**
     * {@inheritDoc}
     * <p>
     * Delegates to Spring AI's reactive {@code stream().content()} API,
     * which emits each token or fragment as soon as it is received from
     * the provider, without buffering the full response.
     */
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
