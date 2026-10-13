import {inject, Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CreateUserDTO, TagNameDTO, TagUsageDTO, UpdateUserDTO, UserDTO} from "../rest";
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
        return this.httpClient.post<void>(`${this.baseUrl}/admin/user`, {...dto, admin: !!dto.admin});
    }

    updateUser(id: number, dto: UpdateUserDTO): Observable<void> {
        return this.httpClient.put<void>(`${this.baseUrl}/admin/user/${id}`, {...dto, admin: !!dto.admin});
    }

    getAllTags(): Observable<TagUsageDTO[]> {
        return this.httpClient.get<TagUsageDTO[]>(`${this.baseUrl}/admin/tag`);
    }

    createTag(dto: TagNameDTO): Observable<TagUsageDTO> {
        return this.httpClient.post<TagUsageDTO>(`${this.baseUrl}/admin/tag`, dto);
    }

    renameTag(id: number, dto: TagNameDTO): Observable<void> {
        return this.httpClient.put<void>(`${this.baseUrl}/admin/tag/${id}`, dto);
    }

    deleteTag(id: number): Observable<void> {
        return this.httpClient.delete<void>(`${this.baseUrl}/admin/tag/${id}`);
    }

}
