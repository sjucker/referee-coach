import {ChangeDetectionStrategy, Component, inject, OnInit} from '@angular/core';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {AuthenticationService} from "../service/authentication.service";
import {PasskeyService} from "../service/passkey.service";
import {PasskeyDTO} from "../rest";
import {PasskeyDeleteDialogComponent} from "../passkey-delete-dialog/passkey-delete-dialog.component";
import {MatToolbar} from '@angular/material/toolbar';
import {MatButton, MatIconAnchor, MatIconButton} from '@angular/material/button';
import {RouterLink} from '@angular/router';
import {MatIcon} from '@angular/material/icon';
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {DatePipe} from '@angular/common';


@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatToolbar, MatIconAnchor, RouterLink, MatIcon, MatCard, MatCardHeader, MatCardTitle, MatCardContent, FormsModule, ReactiveFormsModule, MatFormField, MatLabel, MatInput, MatButton, MatIconButton, DatePipe]
})
export class SettingsComponent implements OnInit {
    private formBuilder = inject(FormBuilder);
    private authenticationService = inject(AuthenticationService);
    private passkeyService = inject(PasskeyService);
    private dialog = inject(MatDialog);
    private snackBar = inject(MatSnackBar);


    error = false;
    errorMessage = '';
    success = false;

    changePasswordForm = this.formBuilder.group({
        oldPassword: [null, [Validators.required]],
        newPassword1: [null, [Validators.required]],
        newPassword2: [null, [Validators.required]],
    });

    passkeySupported = this.passkeyService.isSupported();
    passkeys: PasskeyDTO[] = [];
    newPasskeyName = '';
    registeringPasskey = false;
    editingPasskeyId?: number;
    editingPasskeyName = '';

    ngOnInit(): void {
        if (this.passkeySupported) {
            this.loadPasskeys();
        }
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

    addPasskey(): void {
        this.registeringPasskey = true;
        this.passkeyService.register(this.newPasskeyName).subscribe({
            next: () => {
                this.registeringPasskey = false;
                this.newPasskeyName = '';
                this.showMessage('Passkey added');
                this.loadPasskeys();
            },
            error: error => {
                this.registeringPasskey = false;
                if (!this.passkeyService.isCancelled(error)) {
                    this.showMessage('Could not add passkey!');
                }
            }
        });
    }

    startRename(passkey: PasskeyDTO): void {
        this.editingPasskeyId = passkey.id;
        this.editingPasskeyName = passkey.name;
    }

    cancelRename(): void {
        this.editingPasskeyId = undefined;
    }

    rename(passkey: PasskeyDTO): void {
        if (!this.editingPasskeyName.trim()) {
            return;
        }
        this.passkeyService.rename(passkey.id, this.editingPasskeyName).subscribe({
            next: () => {
                this.editingPasskeyId = undefined;
                this.loadPasskeys();
            },
            error: () => this.showMessage('Could not rename passkey!')
        });
    }

    deletePasskey(passkey: PasskeyDTO): void {
        this.dialog.open(PasskeyDeleteDialogComponent, {data: passkey}).afterClosed().subscribe((confirm: boolean) => {
            if (confirm) {
                this.passkeyService.delete(passkey.id).subscribe({
                    next: () => this.loadPasskeys(),
                    error: () => this.showMessage('Could not delete passkey!')
                });
            }
        });
    }

    private loadPasskeys(): void {
        this.passkeyService.list().subscribe(passkeys => {
            this.passkeys = passkeys;
        });
    }

    private showMessage(message: string): void {
        this.snackBar.open(message, undefined, {
            duration: 3000,
            horizontalPosition: "center",
            verticalPosition: "top"
        });
    }

    isCoach(): boolean {
        return this.authenticationService.isCoach() || this.authenticationService.isRefereeCoach();
    }

}
