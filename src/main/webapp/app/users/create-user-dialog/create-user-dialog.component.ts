import {Component, inject} from '@angular/core';
import {MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from "@angular/material/dialog";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {UserRole} from "../../rest";
import {MatSelectModule} from "@angular/material/select";
import {MatCheckboxModule} from "@angular/material/checkbox";

@Component({
    selector: 'app-create-user-dialog',
    templateUrl: './create-user-dialog.component.html',
    standalone: true,
    imports: [
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
export class CreateUserDialogComponent {
    private dialogRef = inject(MatDialogRef<CreateUserDialogComponent>);
    private fb = inject(FormBuilder);

    form: FormGroup;
    userRoles = Object.values(UserRole);

    constructor() {
        this.form = this.fb.group({
            name: ['', [Validators.required]],
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required]],
            role: [UserRole.REFEREE, [Validators.required]],
            admin: [false, [Validators.required]]
        });
    }

    save() {
        this.dialogRef.close(this.form.value);
    }

    close() {
        this.dialogRef.close();
    }
}
