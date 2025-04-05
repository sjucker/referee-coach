import {Component} from '@angular/core';
import {AuthenticationService} from "../service/authentication.service";
import {MatToolbar} from '@angular/material/toolbar';

import {MatProgressBar} from '@angular/material/progress-bar';
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {FormsModule} from '@angular/forms';
import {MatButton} from '@angular/material/button';

@Component({
    selector: 'app-forgot-password',
    templateUrl: './forgot-password.component.html',
    styleUrls: ['./forgot-password.component.scss'],
    imports: [MatToolbar, MatProgressBar, MatCard, MatCardHeader, MatCardTitle, MatCardContent, MatFormField, MatLabel, MatInput, FormsModule, MatButton]
})
export class ForgotPasswordComponent {
    requesting = false;
    requested = false;
    email = '';

    constructor(private authenticationService: AuthenticationService) {
    }

    requestPasswordReset() {
        if (this.email) {
            this.requesting = true;
            this.requested = false;
            this.authenticationService.forgotPassword(this.email).subscribe({
                next: () => {
                    this.requesting = false;
                    this.requested = true;
                },
                error: () => {
                    this.requesting = false;
                    this.requested = false;
                }
            });
        }
    }
}
