import { Component, inject, OnInit } from '@angular/core';
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

  ngOnInit(): void {
    this.store.loadConversations();
  }
}
