import {Injectable} from '@angular/core';
import {
    CommentReplyDTO,
    CreateGameDiscussionCommentDTO,
    CreateGameDiscussionDTO,
    CreateRepliesDTO,
    Federation,
    GameDiscussionCommentDTO,
    GameDiscussionDTO,
    VideoReportDTO
} from "../rest";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";

@Injectable({
    providedIn: 'root'
})
export class GameDiscussionService {

    private baseUrl = environment.baseUrl;

    constructor(private readonly httpClient: HttpClient) {
    }

    createGameDiscussion(gameNumber: string): Observable<any> {
        const request: CreateGameDiscussionDTO = {
            gameNumber: gameNumber,
            federation: Federation.SBL
        };
        return this.httpClient.post<VideoReportDTO>(`${this.baseUrl}/game-discussion`, request);
    }

    getGameDiscussion(id: string) {
        return this.httpClient.get<GameDiscussionDTO>(`${this.baseUrl}/game-discussion/${id}`);
    }

    comment(id: string, replies: CommentReplyDTO[], newComments: GameDiscussionCommentDTO[]): Observable<any> {
        const request: CreateGameDiscussionCommentDTO = {
            replies: replies,
            newComments: newComments
        };
        return this.httpClient.post<CreateRepliesDTO>(`${this.baseUrl}/game-discussion/${id}/comment`, request);
    }
}
