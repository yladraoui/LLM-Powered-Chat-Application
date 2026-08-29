import { Routes } from '@angular/router';
import path from 'path';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () =>
        import('./features/chat/chat-page/chat-page').then((m) => m.ChatPage),
    },
];
