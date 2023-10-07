import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA} from "@angular/material/dialog";
import {AuthenticationService} from "../service/authentication.service";

export interface CommentReplyDialogData {
    referee: string;
}

@Component({
    selector: 'app-comment-reply-dialog',
    templateUrl: './comment-reply-dialog.component.html',
    styleUrls: ['./comment-reply-dialog.component.scss']
})
export class CommentReplyDialogComponent {

    reply: string = '';

    constructor(@Inject(MAT_DIALOG_DATA) public data: CommentReplyDialogData,
                private authenticationService: AuthenticationService) {
    }

    isLoggedIn(): boolean {
        return this.authenticationService.isLoggedIn();
    }

}
