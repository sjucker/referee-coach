import {Component, inject} from '@angular/core';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {AuthenticationService} from "../service/authentication.service";
import {MatToolbar} from '@angular/material/toolbar';
import {MatButton, MatIconAnchor} from '@angular/material/button';
import {RouterLink} from '@angular/router';
import {MatIcon} from '@angular/material/icon';
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';


@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss'],
    imports: [MatToolbar, MatIconAnchor, RouterLink, MatIcon, MatCard, MatCardHeader, MatCardTitle, MatCardContent, FormsModule, ReactiveFormsModule, MatFormField, MatLabel, MatInput, MatButton]
})
export class SettingsComponent {
    private formBuilder = inject(FormBuilder);
    private authenticationService = inject(AuthenticationService);


    error = false;
    errorMessage = '';
    success = false;

    changePasswordForm = this.formBuilder.group({
        oldPassword: [null, [Validators.required]],
        newPassword1: [null, [Validators.required]],
        newPassword2: [null, [Validators.required]],
    });

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
        return this.authenticationService.isCoach() || this.authenticationService.isRefereeCoach();
    }

}
