import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {ChangePasswordRequestDTO, LoginRequestDTO, LoginResponseDTO, UserRole} from "../rest";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class AuthenticationService {

    private readonly token = 'token';
    private readonly userId = 'user-id';
    private readonly admin = 'admin';
    private readonly role = 'role';

    private baseUrl = environment.baseUrl;

    constructor(private readonly httpClient: HttpClient) {
    }

    login(email: string, password: string): Observable<LoginResponseDTO> {
        const request: LoginRequestDTO = {
            email: email,
            password: password
        };

        return this.httpClient.post<LoginResponseDTO>(`${this.baseUrl}/authenticate`, request);
    }

    logout(): void {
        localStorage.clear();
    }

    changePassword(oldPassword: string, newPassword: string): Observable<never> {
        const request: ChangePasswordRequestDTO = {
            oldPassword: oldPassword,
            newPassword: newPassword
        }
        return this.httpClient.post<never>(`${this.baseUrl}/authenticate/change-password`, request);
    }

    setCredentials(dto: LoginResponseDTO): void {
        localStorage.setItem(this.token, dto.jwt);
        localStorage.setItem(this.userId, String(dto.id));
        localStorage.setItem(this.role, String(dto.role));
        if (dto.admin) {
            localStorage.setItem(this.admin, "1");
        } else {
            localStorage.removeItem(this.admin);
        }
    }

    getAuthorizationToken(): string | null {
        return localStorage.getItem(this.token);
    }

    getUserId(): number | null {
        const userId = localStorage.getItem(this.userId);
        if (userId) {
            return parseInt(userId);
        }
        return null;
    }

    isLoggedIn(): boolean {
        return localStorage.getItem(this.token) !== null;
    }

    isAdmin(): boolean {
        return this.isLoggedIn() && localStorage.getItem(this.admin) !== null;
    }

    getRole(): UserRole {
        return UserRole[localStorage.getItem(this.role) as keyof typeof UserRole]
    }

}
