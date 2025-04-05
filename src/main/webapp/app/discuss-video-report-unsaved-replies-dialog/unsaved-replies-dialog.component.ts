import {Component} from '@angular/core';
import {MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from '@angular/material/dialog';
import {MatIcon} from '@angular/material/icon';
import {MatButton} from '@angular/material/button';

@Component({
    selector: 'app-unsaved-replies-dialog',
    templateUrl: './unsaved-replies-dialog.component.html',
    styleUrls: ['./unsaved-replies-dialog.component.scss'],
    imports: [MatDialogTitle, MatDialogContent, MatIcon, MatDialogActions, MatButton, MatDialogClose]
})
export class UnsavedRepliesDialogComponent {

    constructor() {
    }

}
