import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA} from "@angular/material/dialog";
import {VideoReportDTO} from "../rest";
import {getReferee} from "../service/video-report.service";

@Component({
    selector: 'app-video-report-delete-dialog',
    templateUrl: './video-report-delete-dialog.component.html',
    styleUrls: ['./video-report-delete-dialog.component.scss']
})
export class VideoReportDeleteDialogComponent {

    constructor(@Inject(MAT_DIALOG_DATA) public dto: VideoReportDTO) {
    }

    getReferee(): string {
        return getReferee(this.dto);
    }

}
