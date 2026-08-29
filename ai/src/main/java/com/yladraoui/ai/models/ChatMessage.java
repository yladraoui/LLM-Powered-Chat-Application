package com.yladraoui.ai.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SenderRole role;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at",nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    protected ChatMessage(){

    }

    public ChatMessage(SenderRole role, String content){
        this.role =  role;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public ChatMessage(Conversation conversation, SenderRole role, String content) {
        this.conversation = conversation;
        this.role = role;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    protected void attachTo(Conversation conversation){
        this.conversation =  conversation;
    }

    public Long getId(){
        return this.id;
    }

    public SenderRole getRole(){
        return this.role;
    }

    public String getContent(){
        return this.content;
    }

    public LocalDateTime getCreatedAt(){
        return this.createdAt;
    }

}
