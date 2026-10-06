import {ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthenticationService} from "../service/authentication.service";
import {PasskeyService} from "../service/passkey.service";
import {LoginResponseDTO} from "../rest";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {MatSnackBar} from "@angular/material/snack-bar";
import {FORGOT_PASSWORD_PATH} from "../app-routing.module";
import {MatToolbar} from '@angular/material/toolbar';

import {MatProgressBar} from '@angular/material/progress-bar';
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatToolbar, MatProgressBar, MatCard, MatCardHeader, MatCardTitle, MatCardContent, FormsModule, ReactiveFormsModule, MatFormField, MatLabel, MatInput, MatButton, MatIcon, RouterLink]
})
export class LoginComponent implements OnInit, OnDestroy {
    private formBuilder = inject(FormBuilder);
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private authenticationService = inject(AuthenticationService);
    private snackBar = inject(MatSnackBar);
    private passkeyService = inject(PasskeyService);


    authenticating = false;

    forgotPasswordUrl = `/${FORGOT_PASSWORD_PATH}`;

    passkeySupported = this.passkeyService.isSupported();
    private autofillAbortController?: AbortController;

    loginForm = this.formBuilder.group({
        email: ['', [Validators.required]],
        password: ['', [Validators.required]],
    });

    ngOnInit(): void {
        const email = this.route.snapshot.paramMap.get('email');
        if (email) {
            this.loginForm.setValue({
                email: email,
                password: ''
            });
        }
        this.startPasskeyAutofill();
    }

    ngOnDestroy(): void {
        this.autofillAbortController?.abort();
    }

    login(): void {
        if (this.loginForm.valid) {
            this.authenticating = true;
            const val = this.loginForm.value;
            this.authenticationService.login(val.email!, val.password!).subscribe({
                next: response => this.loggedIn(response),
                error: () => {
                    this.showError('Email/Password is not correct!');
                    this.authenticating = false;
                },
            });
        }
    }

    loginWithPasskey(): void {
        // only one WebAuthn request can be pending at a time
        this.autofillAbortController?.abort();
        this.authenticating = true;
        this.passkeyService.login(false).subscribe({
            next: response => this.loggedIn(response),
            error: error => {
                this.authenticating = false;
                if (!this.passkeyService.isCancelled(error)) {
                    this.showError('Login with passkey failed!');
                }
                this.startPasskeyAutofill();
            }
        });
    }

    private startPasskeyAutofill(): void {
        this.passkeyService.isAutofillSupported().then(supported => {
            if (!supported) {
                return;
            }
            this.autofillAbortController = new AbortController();
            this.passkeyService.login(true, this.autofillAbortController.signal).subscribe({
                next: response => this.loggedIn(response),
                error: error => {
                    if (!this.passkeyService.isCancelled(error)) {
                        this.showError('Login with passkey failed!');
                    }
                }
            });
        }).catch(reason => {
            console.error(reason);
        });
    }

    private loggedIn(response: LoginResponseDTO): void {
        this.authenticating = false;
        this.authenticationService.setCredentials(response);
        this.router.navigate(['/']).catch(reason => {
            console.error(reason);
        });
    }

    private showError(message: string): void {
        this.snackBar.open(message, undefined, {
            duration: 3000,
            horizontalPosition: "center",
            verticalPosition: "top"
        });
    }

}
