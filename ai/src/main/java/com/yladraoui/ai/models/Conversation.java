package com.yladraoui.ai.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Vector;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<ChatMessage> messages = new Vector<>();

    protected Conversation(){

    }

    public Conversation(String title){
        this.title = title;
        this.createdAt = LocalDateTime.now();
    }

    public void appendMessage(ChatMessage message){
        this.messages.add(message);
        message.attachTo(this);
    }

    public Long getId(){
        return this.id;
    }

    public String getTitle(){
        return this.title;
    }

    public LocalDateTime getCreatedAt(){
        return this.createdAt;
    }

    public List<ChatMessage> getMessages(){
        return List.copyOf(messages);
    }


}
