import {inject, Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CreateUserDTO, UpdateUserDTO, UserDTO} from "../rest";
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

    createUser(dto: CreateUserDTO): Observable<void> {
        return this.httpClient.post<void>(`${this.baseUrl}/admin/user`, dto);
    }

    updateUser(id: number, dto: UpdateUserDTO): Observable<void> {
        return this.httpClient.put<void>(`${this.baseUrl}/admin/user/${id}`, dto);
    }

}
