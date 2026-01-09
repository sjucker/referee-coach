import {inject, Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {UserDTO} from "../rest";
import {environment} from "../../environments/environment";

@Injectable({
    providedIn: 'root'
})
export class AdminService {
    private readonly httpClient = inject(HttpClient);

    private baseUrl = environment.baseUrl;

    getAllUsers(): Observable<UserDTO[]> {
        return this.httpClient.get<UserDTO[]>(`${this.baseUrl}/admin/user`);
    }

}
