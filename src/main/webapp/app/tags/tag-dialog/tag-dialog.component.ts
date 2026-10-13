import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from "@angular/material/dialog";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {TagUsageDTO} from "../../rest";

@Component({
    selector: 'app-tag-dialog',
    templateUrl: './tag-dialog.component.html',
    standalone: true,
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [
        MatDialogTitle,
        MatDialogContent,
        MatFormFieldModule,
        MatInputModule,
        ReactiveFormsModule,
        MatDialogActions,
        MatButtonModule
    ]
})
export class TagDialogComponent {
    data = inject<TagUsageDTO | null>(MAT_DIALOG_DATA);
    private dialogRef = inject(MatDialogRef<TagDialogComponent>);
    private fb = inject(FormBuilder);

    form: FormGroup;
    edit: boolean;

    constructor() {
        this.edit = !!this.data;

        this.form = this.fb.group({
            name: [this.data?.name, [Validators.required, Validators.maxLength(255), Validators.pattern(/\S/)]],
        });
    }

    save() {
        if (this.form.valid) {
            this.dialogRef.close(this.form.value);
        }
    }

    close() {
        this.dialogRef.close();
    }
}
