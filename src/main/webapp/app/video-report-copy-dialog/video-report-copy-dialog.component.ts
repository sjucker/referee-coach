import {Component, Inject, OnInit} from '@angular/core';
import {Reportee, UserDTO} from "../rest";
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from "@angular/material/dialog";
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatOption, MatSelect} from '@angular/material/select';

import {MatButton} from '@angular/material/button';

interface ReporteeSelection {
    reportee: Reportee,
    name: string
}

export interface VideoReportCopyDialogData {
    reportee: Reportee;
    referee1?: UserDTO;
    referee2?: UserDTO;
    referee3?: UserDTO;
    title: string;
    description: string;
}

@Component({
    selector: 'app-video-report-copy-dialog',
    templateUrl: './video-report-copy-dialog.component.html',
    styleUrls: ['./video-report-copy-dialog.component.scss'],
    imports: [MatDialogTitle, MatDialogContent, MatFormField, MatLabel, MatSelect, MatOption, MatDialogActions, MatButton, MatDialogClose]
})
export class VideoReportCopyDialogComponent implements OnInit {

    title = '';
    description = '';

    reportee?: Reportee
    reportees: ReporteeSelection[] = []

    constructor(@Inject(MAT_DIALOG_DATA) public data: VideoReportCopyDialogData) {
    }

    ngOnInit(): void {
        this.title = this.data.title;
        this.description = this.data.description;

        if (this.data.reportee !== Reportee.FIRST_REFEREE && this.data.referee1) {
            this.reportees = [...this.reportees, {
                reportee: Reportee.FIRST_REFEREE,
                name: this.data.referee1.name
            }];
            this.reportee = Reportee.FIRST_REFEREE;
        }

        if (this.data.reportee !== Reportee.SECOND_REFEREE && this.data.referee2) {
            this.reportees = [...this.reportees, {
                reportee: Reportee.SECOND_REFEREE,
                name: this.data.referee2.name
            }];
            if (!this.reportee) {
                this.reportee = Reportee.SECOND_REFEREE;
            }
        }

        if (this.data.reportee !== Reportee.THIRD_REFEREE && this.data.referee3) {
            this.reportees = [...this.reportees, {
                reportee: Reportee.THIRD_REFEREE,
                name: this.data.referee3.name
            }];
            if (!this.reportee) {
                this.reportee = Reportee.THIRD_REFEREE;
            }
        }
    }

}
