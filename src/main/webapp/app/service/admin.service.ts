import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {RefereeDTO} from "../rest";
import {environment} from "../../environments/environment";

@Injectable({
    providedIn: 'root'
})
export class AdminService {

    private baseUrl = environment.baseUrl;

    constructor(private readonly httpClient: HttpClient) {
    }

    getAllReferees(): Observable<RefereeDTO[]> {
        return this.httpClient.get<RefereeDTO[]>(`${this.baseUrl}/admin/referee`);
    }

}
