import {Component} from '@angular/core';
import {MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';

@Component({
    selector: 'app-discuss-video-report-finish-dialog',
    templateUrl: './discuss-video-report-finish-dialog.component.html',
    styleUrls: ['./discuss-video-report-finish-dialog.component.scss'],
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButton, MatDialogClose]
})
export class DiscussVideoReportFinishDialogComponent {

    constructor() {
    }

}
