import { ChatMessage } from "./message.model"

export interface ConversationSummary{
    id: number,
    title: string,
    createdAt: string
};

export interface ConversationDetail extends ConversationSummary{
    messages: ChatMessage[]

}