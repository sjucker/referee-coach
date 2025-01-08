import {Component} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";

@Component({
    selector: 'app-video-report-finish-dialog',
    templateUrl: './video-report-finish-dialog.component.html',
    styleUrls: ['./video-report-finish-dialog.component.scss'],
    standalone: false
})
export class VideoReportFinishDialogComponent {

    constructor(public dialog: MatDialog) {
    }

}
