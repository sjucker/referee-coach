import {Component} from '@angular/core';
import {FormBuilder, Validators} from "@angular/forms";
import {AuthenticationService} from "../service/authentication.service";

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss']
})
export class SettingsComponent {

    error = false;
    errorMessage = '';
    success = false;

    changePasswordForm = this.formBuilder.group({
        oldPassword: [null, [Validators.required]],
        newPassword1: [null, [Validators.required]],
        newPassword2: [null, [Validators.required]],
    });

    constructor(private formBuilder: FormBuilder,
                private authenticationService: AuthenticationService) {
    }

    changePassword() {
        if (this.changePasswordForm.valid) {
            this.error = false;
            this.success = false;
            if (this.changePasswordForm.value.newPassword1 !== this.changePasswordForm.value.newPassword2) {
                this.error = true;
                this.errorMessage = 'New password does not match'
            } else {
                this.authenticationService.changePassword(
                    this.changePasswordForm.value.oldPassword!,
                    this.changePasswordForm.value.newPassword1!
                ).subscribe({
                    next: () => {
                        this.success = true;
                    },
                    error: () => {
                        this.error = true;
                        this.errorMessage = 'Could not change the existing password!'
                    }
                });
            }
        }
    }

    isCoach(): boolean {
        return this.authenticationService.isCoach();
    }

}
