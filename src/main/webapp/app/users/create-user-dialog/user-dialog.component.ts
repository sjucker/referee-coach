import {Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from "@angular/material/dialog";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {UserDTO, UserRole} from "../../rest";
import {MatSelectModule} from "@angular/material/select";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {CommonModule} from "@angular/common";

@Component({
    selector: 'app-user-dialog',
    templateUrl: './user-dialog.component.html',
    standalone: true,
    imports: [
        CommonModule,
        MatDialogTitle,
        MatDialogContent,
        MatFormFieldModule,
        MatInputModule,
        ReactiveFormsModule,
        MatDialogActions,
        MatButtonModule,
        MatSelectModule,
        MatCheckboxModule
    ]
})
export class UserDialogComponent {
    data = inject<UserDTO>(MAT_DIALOG_DATA);
    private dialogRef = inject(MatDialogRef<UserDialogComponent>);
    private fb = inject(FormBuilder);

    form: FormGroup;
    userRoles = Object.values(UserRole);
    edit: boolean;

    constructor() {
        const data = this.data;

        this.edit = !!data;

        this.form = this.fb.group({
            name: [data?.name, [Validators.required]],
            email: [data?.email, [Validators.required, Validators.email]],
            password: ['', [data ? Validators.nullValidator : Validators.required]],
            role: [data?.role, [Validators.required]],
            admin: [data?.admin]
        });
    }

    save() {
        this.dialogRef.close(this.form.value);
    }

    close() {
        this.dialogRef.close();
    }
}
