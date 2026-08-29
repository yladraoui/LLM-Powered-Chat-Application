import { Component, input, output, signal } from '@angular/core';

@Component({
  imports: [],
  selector: 'app-message-input',
  styleUrl: './message-input.css',
  templateUrl: './message-input.html',
})
export class MessageInput {
  disabled = input<boolean>(false);
  send = output<string>();

  protected readonly draft = signal('');

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.submit();
    }
  }

  submit(): void {
    const value = this.draft().trim();
    if (!value || this.disabled()) return;
    this.send.emit(value);
    this.draft.set('');
  }
}
