import {Component} from '@angular/core';
import {MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';

@Component({
    selector: 'app-game-discussion-finish-comments-dialog',
    templateUrl: './game-discussion-finish-comments-dialog.component.html',
    styleUrls: ['./game-discussion-finish-comments-dialog.component.scss'],
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButton, MatDialogClose]
})
export class GameDiscussionFinishCommentsDialogComponent {

}
