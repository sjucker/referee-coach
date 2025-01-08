import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA} from "@angular/material/dialog";
import {OverviewDTO} from "../rest";

@Component({
    selector: 'app-video-report-delete-dialog',
    templateUrl: './video-report-delete-dialog.component.html',
    styleUrls: ['./video-report-delete-dialog.component.scss'],
    standalone: false
})
export class VideoReportDeleteDialogComponent {

    constructor(@Inject(MAT_DIALOG_DATA) public dto: OverviewDTO) {
    }

}
