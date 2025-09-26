import {inject, Injectable} from '@angular/core';
import {CommentReplyDTO, CreateGameDiscussionCommentDTO, CreateGameDiscussionDTO, GameDiscussionCommentDTO, GameDiscussionDTO} from "../rest";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";

@Injectable({
    providedIn: 'root'
})
export class GameDiscussionService {
    private readonly httpClient = inject(HttpClient);


    private baseUrl = environment.baseUrl;

    createGameDiscussion(gameNumber: string, youtubeId: string): Observable<GameDiscussionDTO> {
        const request: CreateGameDiscussionDTO = {
            gameNumber: gameNumber,
            youtubeId: youtubeId,
        };
        return this.httpClient.post<GameDiscussionDTO>(`${this.baseUrl}/game-discussion`, request);
    }

    getGameDiscussion(id: string) {
        return this.httpClient.get<GameDiscussionDTO>(`${this.baseUrl}/game-discussion/${id}`);
    }

    comment(id: string, replies: CommentReplyDTO[], newComments: GameDiscussionCommentDTO[]): Observable<GameDiscussionDTO> {
        const request: CreateGameDiscussionCommentDTO = {
            replies: replies,
            newComments: newComments
        };
        return this.httpClient.post<GameDiscussionDTO>(`${this.baseUrl}/game-discussion/${id}/comment`, request);
    }
}
