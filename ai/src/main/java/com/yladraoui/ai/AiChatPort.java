package com.yladraoui.ai;


import java.util.List;

public interface AiChatPort {
    public String generateReply(List<ChatTurn> history);
}
