import {Component} from '@angular/core';
import {AuthenticationService} from "../service/authentication.service";

@Component({
    selector: 'app-forgot-password',
    templateUrl: './forgot-password.component.html',
    styleUrls: ['./forgot-password.component.scss']
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
