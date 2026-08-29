import { Component, inject, OnInit, signal } from '@angular/core';
import { ChatStore } from '../chat.store';
import { MessageInput } from '../../components/message-input/message-input';
import { MessageList } from '../../components/message-list/message-list';
import { ConversationList } from '../../components/conversation-list/conversation-list';

@Component({
  imports: [ConversationList, MessageList, MessageInput],
  selector: 'app-chat-page',
  styleUrl: './chat-page.css',
  templateUrl: './chat-page.html',
})
export class ChatPage implements OnInit{
  protected readonly store = inject(ChatStore);
  protected readonly sidebarOpen = signal(true);

  ngOnInit(): void {
    this.store.loadConversations();
  }

  toggleSidebar(): void {
    this.sidebarOpen.update((open) => !open);
  }

  onSelectConversation(id: number): void {
    this.store.openConversation(id);
    if (window.innerWidth < 768) {
      this.sidebarOpen.set(false);
    }
  }
}
