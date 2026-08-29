export type ChatStreamEvent = 
| { type: 'conversation', conversationId: number }
| { type: 'chunk', text: string }
| { type: 'done'}
| { type: 'error', message: string };