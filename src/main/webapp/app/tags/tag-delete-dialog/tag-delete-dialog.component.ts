import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from "@angular/material/dialog";
import {MatButton} from '@angular/material/button';
import {TagUsageDTO} from "../../rest";

@Component({
    selector: 'app-tag-delete-dialog',
    templateUrl: './tag-delete-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButton, MatDialogClose]
})
export class TagDeleteDialogComponent {
    dto = inject<TagUsageDTO>(MAT_DIALOG_DATA);
}
