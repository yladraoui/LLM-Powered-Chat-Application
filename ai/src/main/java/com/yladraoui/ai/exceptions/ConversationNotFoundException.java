package com.yladraoui.ai.exceptions;


public class ConversationNotFoundException extends RuntimeException {
    public ConversationNotFoundException(Long id) {

        super("Can not find Conversation with ID: " + id);
    }
}
