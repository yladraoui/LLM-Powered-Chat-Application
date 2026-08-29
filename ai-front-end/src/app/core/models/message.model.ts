export type SenderRole = 'USER'|'ASSISTANT';

export interface ChatMessage{
    id: number,
    role: SenderRole,
    content: string,
    createdAt: string
};