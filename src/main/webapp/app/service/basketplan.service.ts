import {inject, Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {BasketplanGameDTO} from "../rest";
import {environment} from "../../environments/environment";

@Injectable({
    providedIn: 'root'
})
export class BasketplanService {
    private readonly httpClient = inject(HttpClient);


    private baseUrl = environment.baseUrl;

    searchGame(gameNumber: string): Observable<BasketplanGameDTO> {
        return this.httpClient.get<BasketplanGameDTO>(`${this.baseUrl}/game/${gameNumber}`);
    }

    searchGameForReferee(gameNumber: string): Observable<BasketplanGameDTO> {
        return this.httpClient.get<BasketplanGameDTO>(`${this.baseUrl}/game/${gameNumber}/referee`);
    }

}
