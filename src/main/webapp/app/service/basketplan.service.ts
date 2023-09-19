import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {BasketplanGameDTO} from "../rest";
import {environment} from "../../environments/environment";

@Injectable({
    providedIn: 'root'
})
export class BasketplanService {

    private baseUrl = environment.baseUrl;

    constructor(private readonly httpClient: HttpClient) {
    }

    searchGame(gameNumber: string): Observable<BasketplanGameDTO> {
        return this.httpClient.get<BasketplanGameDTO>(`${this.baseUrl}/game/SBL/${gameNumber}`);
    }

    searchGameForReferee(gameNumber: string): Observable<BasketplanGameDTO> {
        return this.httpClient.get<BasketplanGameDTO>(`${this.baseUrl}/game/SBL/${gameNumber}/referee`);
    }

}
