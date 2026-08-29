import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { Observable } from "rxjs";
import { ConversationDetail, ConversationSummary } from "../models/conversation.model";


@Injectable({providedIn: 'root'})
export class ConversationService {
    private readonly http = inject(HttpClient);
    private readonly baseUrl = `${environment.apiUrl}/conversations`;

    list(): Observable<ConversationSummary[]> {
        return this.http.get<ConversationSummary[]>(this.baseUrl);
    }

    getById(id: number): Observable<ConversationDetail> {
        return this.http.get<ConversationDetail>(`${this.baseUrl}/${id}`);
    }
}