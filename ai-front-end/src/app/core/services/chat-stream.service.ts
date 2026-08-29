import { Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { Observable, Subscriber } from "rxjs";
import { ChatStreamEvent } from "../models/sse-event.model";

@Injectable({providedIn: 'root'})
export class ChatStreamService{
    private readonly baseUrl = `${environment.apiUrl}/chat/stream`;

    open(message: string, conversationId?: number): Observable<ChatStreamEvent> {
        return new Observable<ChatStreamEvent>((Subscriber) => {
            const params = new URLSearchParams({message});
            if (conversationId != null){
                params.set('conversationId',String(conversationId));
            }

            const source = new EventSource(`${this.baseUrl}?${params.toString()}`);


            source.addEventListener('done', () =>{
                Subscriber.next({type: 'done'});
                Subscriber.complete();
                source.close();
            })
            source.addEventListener('chunk', (event) => {
                const text = JSON.parse((event as MessageEvent).data) as string;
                Subscriber.next({ type: 'chunk', text });
            });

            source.addEventListener('stream-error', (event) => {
                const message = JSON.parse((event as MessageEvent).data) as string;
                Subscriber.next({ type: 'error', message });
                Subscriber.complete();
            source.close();
            });

            source.onerror = () => {
                Subscriber.next({type: 'error', message: 'Problem with stream connection'});
                Subscriber.complete();
                source.close();
            }

            return () => source.close();
        });
    }
}