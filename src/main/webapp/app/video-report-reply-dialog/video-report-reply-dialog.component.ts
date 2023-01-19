import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA} from "@angular/material/dialog";
import {AuthenticationService} from "../service/authentication.service";
import {VideoReportDiscussionDTO} from "../rest";

@Component({
    selector: 'app-video-report-reply-dialog',
    templateUrl: './video-report-reply-dialog.component.html',
    styleUrls: ['./video-report-reply-dialog.component.scss']
})
export class VideoReportReplyDialogComponent {

    reply: String = '';

    constructor(@Inject(MAT_DIALOG_DATA) public data: VideoReportDiscussionDTO,
                private authenticationService: AuthenticationService) {
    }

    isLoggedIn(): boolean {
        return this.authenticationService.isLoggedIn();
    }

}
