import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from "@angular/material/dialog";
import {AuthenticationService} from "../service/authentication.service"
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {CdkTextareaAutosize} from '@angular/cdk/text-field';
import {MatInput} from '@angular/material/input';
import {FormsModule} from '@angular/forms';
import {MatIcon} from '@angular/material/icon';
import {MatButton} from '@angular/material/button';

export interface CommentReplyDialogData {
    referee: string;
}

@Component({
    selector: 'app-comment-reply-dialog',
    templateUrl: './comment-reply-dialog.component.html',
    styleUrls: ['./comment-reply-dialog.component.scss'],
    imports: [MatDialogTitle, MatDialogContent, MatFormField, MatLabel, CdkTextareaAutosize, MatInput, FormsModule, MatIcon, MatDialogActions, MatButton, MatDialogClose]
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
