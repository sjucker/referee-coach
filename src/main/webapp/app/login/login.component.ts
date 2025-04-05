import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthenticationService} from "../service/authentication.service";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {MatSnackBar} from "@angular/material/snack-bar";
import {FORGOT_PASSWORD_PATH} from "../app-routing.module";
import {MatToolbar} from '@angular/material/toolbar';
import {NgIf} from '@angular/common';
import {MatProgressBar} from '@angular/material/progress-bar';
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatButton} from '@angular/material/button';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss'],
    imports: [MatToolbar, NgIf, MatProgressBar, MatCard, MatCardHeader, MatCardTitle, MatCardContent, FormsModule, ReactiveFormsModule, MatFormField, MatLabel, MatInput, MatButton, RouterLink]
})
export class LoginComponent implements OnInit {

    authenticating = false;

    forgotPasswordUrl = `/${FORGOT_PASSWORD_PATH}`;

    loginForm = this.formBuilder.group({
        email: ['', [Validators.required]],
        password: ['', [Validators.required]],
    });

    constructor(private formBuilder: FormBuilder,
                private router: Router,
                private route: ActivatedRoute,
                private authenticationService: AuthenticationService,
                private snackBar: MatSnackBar) {
    }

    ngOnInit(): void {
        const email = this.route.snapshot.paramMap.get('email');
        if (email) {
            this.loginForm.setValue({
                email: email,
                password: ''
            });
        }
    }

    login(): void {
        if (this.loginForm.valid) {
            this.authenticating = true;
            const val = this.loginForm.value;
            this.authenticationService.login(val.email!, val.password!).subscribe({
                next: response => {
                    this.authenticating = false;
                    this.authenticationService.setCredentials(response);
                    this.router.navigate(['/']).catch(reason => {
                        console.error(reason);
                    });
                },
                error: () => {
                    this.snackBar.open('Email/Password is not correct!', undefined, {
                        duration: 3000,
                        horizontalPosition: "center",
                        verticalPosition: "top"
                    })
                    this.authenticating = false;
                },
            });
        }
    }

}
