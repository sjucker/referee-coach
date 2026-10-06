import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from "@angular/material/dialog";
import {PasskeyDTO} from "../rest";
import {MatButton} from '@angular/material/button';

@Component({
    selector: 'app-passkey-delete-dialog',
    templateUrl: './passkey-delete-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButton, MatDialogClose]
})
export class PasskeyDeleteDialogComponent {
    dto = inject<PasskeyDTO>(MAT_DIALOG_DATA);

}
