import {inject, Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {from, map, Observable, switchMap} from "rxjs";
import {
    LoginResponseDTO,
    PasskeyDTO,
    PasskeyLoginRequestDTO,
    PasskeyOptionsDTO,
    PasskeyRegistrationRequestDTO,
    RenamePasskeyRequestDTO
} from "../rest";
import {environment} from "../../environments/environment";

@Injectable({
    providedIn: 'root'
})
export class PasskeyService {
    private readonly httpClient = inject(HttpClient);

    private baseUrl = environment.baseUrl;

    isSupported(): boolean {
        return typeof PublicKeyCredential !== 'undefined'
            && typeof PublicKeyCredential.parseRequestOptionsFromJSON === 'function'
            && typeof PublicKeyCredential.parseCreationOptionsFromJSON === 'function';
    }

    async isAutofillSupported(): Promise<boolean> {
        return this.isSupported()
            && typeof PublicKeyCredential.isConditionalMediationAvailable === 'function'
            && await PublicKeyCredential.isConditionalMediationAvailable();
    }

    /**
     * @param conditional if true, passkeys are offered via the browser's autofill of the username field instead of a modal dialog
     * @param signal used to abort a pending (conditional) request
     */
    login(conditional: boolean, signal?: AbortSignal): Observable<LoginResponseDTO> {
        return this.httpClient.post<PasskeyOptionsDTO>(`${this.baseUrl}/passkey/login/start`, {}).pipe(
            switchMap(options => from(navigator.credentials.get({
                publicKey: PublicKeyCredential.parseRequestOptionsFromJSON(JSON.parse(options.optionsJson).publicKey),
                mediation: conditional ? 'conditional' : 'optional',
                signal: signal
            })).pipe(map(credential => ({options, credential: credential as PublicKeyCredential})))),
            switchMap(({options, credential}) => {
                const request: PasskeyLoginRequestDTO = {
                    ceremonyId: options.ceremonyId,
                    credentialJson: JSON.stringify(credential.toJSON())
                };
                return this.httpClient.post<LoginResponseDTO>(`${this.baseUrl}/passkey/login/finish`, request);
            })
        );
    }

    register(name: string): Observable<PasskeyDTO> {
        return this.httpClient.post<PasskeyOptionsDTO>(`${this.baseUrl}/passkey/register/start`, {}).pipe(
            switchMap(options => from(navigator.credentials.create({
                publicKey: PublicKeyCredential.parseCreationOptionsFromJSON(JSON.parse(options.optionsJson).publicKey)
            })).pipe(map(credential => ({options, credential: credential as PublicKeyCredential})))),
            switchMap(({options, credential}) => {
                const request: PasskeyRegistrationRequestDTO = {
                    ceremonyId: options.ceremonyId,
                    credentialJson: JSON.stringify(credential.toJSON()),
                    name: name
                };
                return this.httpClient.post<PasskeyDTO>(`${this.baseUrl}/passkey/register/finish`, request);
            })
        );
    }

    list(): Observable<PasskeyDTO[]> {
        return this.httpClient.get<PasskeyDTO[]>(`${this.baseUrl}/passkey`);
    }

    rename(id: number, name: string): Observable<PasskeyDTO> {
        const request: RenamePasskeyRequestDTO = {
            name: name
        };
        return this.httpClient.put<PasskeyDTO>(`${this.baseUrl}/passkey/${id}`, request);
    }

    delete(id: number): Observable<void> {
        return this.httpClient.delete<void>(`${this.baseUrl}/passkey/${id}`);
    }

    /**
     * User cancelled the browser dialog or the request was aborted: nothing to report.
     */
    isCancelled(error: unknown): boolean {
        return error instanceof DOMException && (error.name === 'NotAllowedError' || error.name === 'AbortError');
    }
}
