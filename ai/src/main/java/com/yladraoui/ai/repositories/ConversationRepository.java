package com.yladraoui.ai.repositories;

import com.yladraoui.ai.models.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findAllByOrderByCreatedAtDesc();
}
