import {Component, OnInit} from '@angular/core';
import {BasketplanGameDTO, OfficiatingMode, Reportee, ReporterDTO, VideoReportDTO} from "../rest";
import {getReferee, VideoReportService} from "../service/video-report.service";
import {BasketplanService} from "../service/basketplan.service";
import {Router} from "@angular/router";
import {MatTableDataSource} from "@angular/material/table";
import {AuthenticationService} from "../service/authentication.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {MatDialog} from "@angular/material/dialog";
import {VideoReportCopyDialogComponent} from "../video-report-copy-dialog/video-report-copy-dialog.component";
import {VideoReportDeleteDialogComponent} from "../video-report-delete-dialog/video-report-delete-dialog.component";

interface ReporteeSelection {
    reportee: Reportee,
    name: string
}

@Component({
    selector: 'app-main',
    templateUrl: './main.component.html',
    styleUrls: ['./main.component.css']
})
export class MainComponent implements OnInit {
    displayedColumns: string[] = ['date', 'gameNumber', 'competition', 'teams', 'coach', 'reportee', 'edit', 'copy', 'view', 'delete'];
    videoReportDtos: MatTableDataSource<VideoReportDTO> = new MatTableDataSource<VideoReportDTO>([]);

    gameNumberInput: string = '';

    // whether we are ready to start with video-report
    ready = false;
    problemDescription = '';

    reportee?: Reportee
    reportees: ReporteeSelection[] = []
    game?: BasketplanGameDTO;

    constructor(private basketplanService: BasketplanService,
                private videoReportService: VideoReportService,
                private authenticationService: AuthenticationService,
                private router: Router,
                private snackBar: MatSnackBar,
                private dialog: MatDialog) {
    }

    ngOnInit(): void {
        this.loadVideoReports();
    }

    private loadVideoReports() {
        this.videoReportService.getAllVideoReports().subscribe(
            value => {
                this.videoReportDtos = new MatTableDataSource<VideoReportDTO>(value);
                this.videoReportDtos.filterPredicate = (data, filter) => {
                    // default filter cannot handle nested objects, so handle each column specifically
                    return data.basketplanGame.gameNumber.toLowerCase().indexOf(filter) != -1
                        || data.basketplanGame.competition.toLowerCase().indexOf(filter) != -1
                        || data.basketplanGame.teamA.toLowerCase().indexOf(filter) != -1
                        || data.basketplanGame.teamB.toLowerCase().indexOf(filter) != -1
                        || data.basketplanGame.teamB.toLowerCase().indexOf(filter) != -1
                        || data.reporter.name.toLowerCase().indexOf(filter) != -1
                        || this.getReportee(data).toLowerCase().indexOf(filter) != -1;
                }
            },
            error => {
                this.snackBar.open("An unexpected error occurred, video reports could not be loaded.", undefined, {
                    duration: 3000,
                    horizontalPosition: "center",
                    verticalPosition: "top"
                })
            });
    }

    searchGame(): void {
        this.ready = false;
        this.problemDescription = '';

        if (!this.gameNumberInput) {
            this.problemDescription = 'Please enter a game number';
            return;
        }

        this.basketplanService.searchGame(this.gameNumberInput.trim()).subscribe(
            dto => {
                if (dto.referee1 && dto.referee2
                    && (dto.officiatingMode === OfficiatingMode.OFFICIATING_2PO || dto.referee3)) {
                    if (dto.youtubeId) {
                        this.ready = true;
                        this.problemDescription = '';
                        this.game = dto;

                        this.reportees = [
                            {reportee: Reportee.FIRST_REFEREE, name: dto.referee1.name},
                            {reportee: Reportee.SECOND_REFEREE, name: dto.referee2.name}
                        ];

                        if (dto.officiatingMode === OfficiatingMode.OFFICIATING_3PO && dto.referee3) {
                            this.reportees = [...this.reportees, {
                                reportee: Reportee.THIRD_REFEREE,
                                name: dto.referee3.name
                            }];
                        }
                    } else {
                        this.problemDescription = 'No YouTube video available'
                    }
                } else {
                    this.problemDescription = 'At least one referee not available in database';
                }
            }
            ,
            error => {
                if (error.status === 404) {
                    this.problemDescription = 'No game found for given game number';
                } else {
                    this.problemDescription = 'An unexpected error occurred'
                }
            }
        )
        ;
    }

    createVideoReport() {
        if (this.game && this.reportee) {
            this.videoReportService.createVideoReport(this.game!.gameNumber, this.reportee).subscribe(
                response => {
                    this.router.navigate(['/edit/' + response.id]);
                },
                error => {
                    this.snackBar.open("An unexpected error occurred, video report could not be created.", undefined, {
                        duration: 3000,
                        horizontalPosition: "center",
                        verticalPosition: "top"
                    })
                })
        } else {
            this.snackBar.open("Please search for a game or select a referee", undefined, {
                duration: 3000,
                horizontalPosition: "center",
                verticalPosition: "top"
            });
        }
    }

    getReportee(report: VideoReportDTO): string {
        return getReferee(report);
    }

    applyFilter(event: Event) {
        const filterValue = (event.target as HTMLInputElement).value;
        this.videoReportDtos.filter = filterValue.trim().toLowerCase();
    }

    isEditable(report: VideoReportDTO) {
        return !report.finished && this.isCurrentUser(report.reporter);
    }

    isCurrentUser(reporter: ReporterDTO): boolean {
        return reporter.id === this.authenticationService.getUserId();
    }

    copy(report: VideoReportDTO) {
        this.dialog.open(VideoReportCopyDialogComponent, {data: report}).afterClosed().subscribe((reportee: Reportee) => {
            if (reportee) {
                this.videoReportService.copyVideoReport(report.id, reportee).subscribe(
                    response => {
                        this.router.navigate(['/edit/' + response.id]);
                    },
                    error => {
                        this.snackBar.open("An unexpected error occurred, video report could not be copied.", undefined, {
                            duration: 3000,
                            horizontalPosition: "center",
                            verticalPosition: "top"
                        })
                    }
                );
            }
        });
    }

    isDeletable(report: VideoReportDTO): boolean {
        return this.isEditable(report) || this.authenticationService.isAdmin();
    }

    delete(report: VideoReportDTO) {
        this.dialog.open(VideoReportDeleteDialogComponent, {data: report}).afterClosed().subscribe((confirm: boolean) => {
            if (confirm) {
                this.videoReportService.deleteVideoReport(report).subscribe(
                    success => {
                        this.loadVideoReports();
                        this.snackBar.open("Video report successfully deleted", undefined, {
                            duration: 3000,
                            horizontalPosition: "center",
                            verticalPosition: "top"
                        });
                    },
                    error => {
                        this.snackBar.open("Video report could not be deleted", undefined, {
                            duration: 3000,
                            horizontalPosition: "center",
                            verticalPosition: "top"
                        });
                    }
                );
            }
        });
    }

}
