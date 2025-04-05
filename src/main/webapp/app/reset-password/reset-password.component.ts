import {Component, inject, OnInit} from '@angular/core';
import {AuthenticationService} from "../service/authentication.service";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {MatSnackBar} from "@angular/material/snack-bar";
import {LOGIN_PATH} from "../app-routing.module";
import {MatToolbar} from '@angular/material/toolbar';

import {MatProgressBar} from '@angular/material/progress-bar';
import {MatCard, MatCardContent} from '@angular/material/card';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {FormsModule} from '@angular/forms';
import {MatButton} from '@angular/material/button';

@Component({
    selector: 'app-reset-password',
    templateUrl: './reset-password.component.html',
    styleUrls: ['./reset-password.component.scss'],
    imports: [MatToolbar, MatProgressBar, MatCard, MatCardContent, MatFormField, MatLabel, MatInput, FormsModule, MatButton, RouterLink]
})
export class ResetPasswordComponent implements OnInit {
    private authenticationService = inject(AuthenticationService);
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private snackBar = inject(MatSnackBar);

    loginUrl = '';
    email?: string | null;
    token?: string | null;

    password = '';

    processing = false;
    success = false;

    ngOnInit(): void {
        this.email = this.route.snapshot.paramMap.get('email');
        this.token = this.route.snapshot.paramMap.get('token');

        if (!this.email || !this.token) {
            this.router.navigate(['/']).catch(reason => {
                console.error(reason);
            });
        }
    }

    resetPassword() {
        if (this.email && this.token && this.password) {
            this.processing = true;
            this.authenticationService.resetPassword(this.email, this.token, this.password).subscribe({
                next: () => {
                    this.processing = false;
                    this.success = true;
                    this.loginUrl = `/${LOGIN_PATH}/${this.email}`;
                },
                error: () => {
                    this.processing = false;
                    this.snackBar.open("An error has occurred.", undefined, {
                        verticalPosition: "top",
                        panelClass: "error"
                    });
                }
            });
        }
    }

}
