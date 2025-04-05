import {Component, inject} from '@angular/core';
import {MatDialog, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from "@angular/material/dialog";
import {MatButton} from '@angular/material/button';

@Component({
    selector: 'app-video-report-finish-dialog',
    templateUrl: './video-report-finish-dialog.component.html',
    styleUrls: ['./video-report-finish-dialog.component.scss'],
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButton, MatDialogClose]
})
export class VideoReportFinishDialogComponent {
    dialog = inject(MatDialog);

}
