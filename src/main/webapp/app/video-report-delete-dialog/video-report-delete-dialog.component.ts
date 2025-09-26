import {Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from "@angular/material/dialog";
import {OverviewDTO} from "../rest";
import {MatButton} from '@angular/material/button';
import {DatePipe} from '@angular/common';

@Component({
    selector: 'app-video-report-delete-dialog',
    templateUrl: './video-report-delete-dialog.component.html',
    styleUrls: ['./video-report-delete-dialog.component.scss'],
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButton, MatDialogClose, DatePipe]
})
export class VideoReportDeleteDialogComponent {
    dto = inject<OverviewDTO>(MAT_DIALOG_DATA);

}
