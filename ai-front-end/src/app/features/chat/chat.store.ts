import { computed, inject, Injectable, signal } from "@angular/core";
import { ConversationService } from "../../core/services/conversation.service";
import { ChatStreamService } from "../../core/services/chat-stream.service";
import { ConversationSummary } from "../../core/models/conversation.model";
import { ChatMessage } from "../../core/models/message.model";
import { ChatStreamEvent } from "../../core/models/sse-event.model";

@Injectable({providedIn: 'root'})
export class ChatStore{

    private readonly conversationService = inject(ConversationService);
    private readonly chatStreamService = inject(ChatStreamService);

    readonly conversations = signal<ConversationSummary[]>([]);
    readonly currentConversationId = signal<number | null>(null);
    readonly messages = signal<ChatMessage[]>([]);
    readonly pendingReply = signal<string>('');
    readonly isStreaming = signal<boolean>(false);
    readonly error = signal<string | null>(null);

    readonly haveActiveConversation = computed(( () => this.currentConversationId() != null));

    readonly currentTitle = computed(() => {
        const id = this.currentConversationId();
        if (id == null) return 'New conversation';
        return this.conversations().find((c) => c.id === id)?.title ?? 'Conversation';
    });

    loadConversations(): void {
        this.conversationService.list().subscribe({
        next: (list) => this.conversations.set(list),
        error: () => this.error.set("Can not upload conversations history"),
        });
    }

    openConversation(id: number): void{
        this.conversationService.getById(id).subscribe({
            next: (detail) => {
                this.currentConversationId.set(id);
                this.messages.set(detail.messages);
            },
            error: () => this.error.set("Conversation does not exist")
        })
    }

    startNewConversation(): void {
        this.currentConversationId.set(null);
        this.messages.set([]);
    }

    sendMessage(content: string): void {
        const trimmed = content.trim();
        if (!trimmed || this.isStreaming()){
            return;
        }

        this.appendMessage({id: -Date.now(), role: 'USER', content: trimmed, createdAt: new Date().toISOString()});
        this.pendingReply.set('');
        this.isStreaming.set(true);
        this.error.set(null);

        this.chatStreamService
        .open(trimmed, this.currentConversationId() ?? undefined)
        .subscribe({
            next: (event) => this.handleStreamEvent(event),
            error: () => {
                this.isStreaming.set(false);
                this.error.set('Error generating the response');
            }
        })
    }

    private handleStreamEvent(event: ChatStreamEvent): void{
        switch (event.type) {
            case 'conversation':
                if (this.currentConversationId() === null) {
                this.currentConversationId.set(event.conversationId);
                this.loadConversations();
                }
                break;
            case 'chunk':
                this.pendingReply.update((text) => text + event.text);
                break;
            case 'done':
                this.appendMessage({
                id: -Date.now(),
                role: 'ASSISTANT',
                content: this.pendingReply(),
                createdAt: new Date().toISOString(),
                });
                this.pendingReply.set('');
                this.isStreaming.set(false);
                break;
            case 'error':
                this.error.set(event.message);
                this.isStreaming.set(false);
                break;
        }

    }

    private appendMessage(message: ChatMessage): void {
        this.messages.update((list) => [...list, message]);
    }
}