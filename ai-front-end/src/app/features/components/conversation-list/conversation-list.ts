import { Component, input, output } from '@angular/core';
import { ConversationSummary } from '../../../core/models/conversation.model';

@Component({
  imports: [],
  selector: 'app-conversation-list',
  styleUrl: './conversation-list.css',
  templateUrl: './conversation-list.html',
})
export class ConversationList {
  conversations = input<ConversationSummary[]>([]);
  activeId = input<number | null>(null);
  select = output<number>();
  createNew = output<void>();
}
