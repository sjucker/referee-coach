import {Component} from '@angular/core';
import {MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';

@Component({
    selector: 'app-video-report-unsaved-changes-dialog',
    templateUrl: './video-report-unsaved-changes-dialog.component.html',
    styleUrls: ['./video-report-unsaved-changes-dialog.component.scss'],
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButton, MatDialogClose]
})
export class VideoReportUnsavedChangesDialogComponent {

    constructor() {
    }

}
