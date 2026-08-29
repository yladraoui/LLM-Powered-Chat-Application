import { Component, computed, input } from '@angular/core';
import { ChatMessage } from '../../../core/models/message.model';
import { marked } from 'marked';

@Component({
  imports: [],
  selector: 'app-message-list',
  styleUrl: './message-list.css',
  templateUrl: './message-list.html',
})
export class MessageList {
    messages = input<ChatMessage[]>([]);
    pendingReply = input<string>('');
    isStreaming = input<boolean>(false);

    protected readonly renderedMessages = computed(() =>
      this.messages().map((m) => ({ ...m, html: this.render(m.content) }))
    );

    protected readonly renderedPending = computed(() => this.render(this.pendingReply()));

    private render(markdown: string): string {
      return marked.parse(markdown, { async: false, breaks: true }) as string;
    }
  }
