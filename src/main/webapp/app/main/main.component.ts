import {Component, OnInit, ViewChild} from '@angular/core';
import {BasketplanGameDTO, OfficiatingMode, Reportee, ReporterDTO, VideoReportDTO} from "../rest";
import {getReferee, VideoReportService} from "../service/video-report.service";
import {BasketplanService} from "../service/basketplan.service";
import {Router} from "@angular/router";
import {MatTableDataSource} from "@angular/material/table";
import {AuthenticationService} from "../service/authentication.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {MatDialog} from "@angular/material/dialog";
import {VideoReportCopyDialogComponent, VideoReportCopyDialogData} from "../video-report-copy-dialog/video-report-copy-dialog.component";
import {VideoReportDeleteDialogComponent} from "../video-report-delete-dialog/video-report-delete-dialog.component";
import getVideoId from "get-video-id";
import {MatPaginator} from "@angular/material/paginator";
import {EDIT_PATH, LOGIN_PATH, VIEW_PATH} from "../app-routing.module";
import {saveAs} from "file-saver";
import {DateTime} from "luxon";

interface ReporteeSelection {
    reportee: Reportee,
    name: string
}

const keyFrom = 'referee.coach.from';
const keyTo = 'referee.coach.to';
const keyFilter = 'referee.coach.filter';

const dateFormat = 'yyyy-MM-dd';

@Component({
    selector: 'app-main',
    templateUrl: './main.component.html',
    styleUrls: ['./main.component.css']
})
export class MainComponent implements OnInit {
    displayedColumns: string[] = ['finished', 'date', 'gameNumber', 'competition', 'teams', 'coach', 'reportee', 'edit', 'view', 'copy', 'delete'];
    videoReportDtos: MatTableDataSource<VideoReportDTO> = new MatTableDataSource<VideoReportDTO>([]);
    @ViewChild(MatPaginator) paginator: MatPaginator | undefined;
    reportsLoaded = false;

    gameNumberInput: string = '';
    youtubeUrlInput: string = '';

    problemDescription = '';

    reportee?: Reportee
    reportees: ReporteeSelection[] = []
    game?: BasketplanGameDTO;
    youtubeId?: string;
    youtubeIdInputNeeded = false;
    textOnlyMode = false; // whether Youtube-Id is needed or not (text-only report)

    from = this.getFrom();
    to = this.getTo();
    filter = this.getFilter();

    searching = false;
    creating = false;
    exporting = false;

    constructor(private basketplanService: BasketplanService,
                private videoReportService: VideoReportService,
                private authenticationService: AuthenticationService,
                private router: Router,
                private snackBar: MatSnackBar,
                private dialog: MatDialog) {
    }

    private getFrom(): DateTime {
        const item = sessionStorage.getItem(keyFrom);
        if (item) {
            return DateTime.fromFormat(item, dateFormat);
        }

        const now = DateTime.now();
        if (now.month > 6) {
            return DateTime.local(now.year, 9, 1);
        } else {
            return DateTime.local(now.year - 1, 9, 1);
        }
    }

    private getTo(): DateTime {
        const item = sessionStorage.getItem(keyTo);
        if (item) {
            return DateTime.fromFormat(item, dateFormat);
        }

        const now = DateTime.now();
        if (now.month > 6) {
            return DateTime.local(now.year + 1, 6, 30);
        } else {
            return DateTime.local(now.year, 6, 30);
        }
    }

    private getFilter(): string | null {
        return sessionStorage.getItem(keyFilter);
    }

    ngOnInit(): void {
        this.loadVideoReports();
    }

    private loadVideoReports() {
        this.videoReportService.getAllVideoReports(this.from, this.to).subscribe({
            next: value => {
                this.reportsLoaded = true;
                this.videoReportDtos = new MatTableDataSource<VideoReportDTO>(value);
                if (this.paginator) {
                    this.videoReportDtos.paginator = this.paginator
                }
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
                const currentFilter = sessionStorage.getItem(keyFilter);
                if (currentFilter) {
                    this.videoReportDtos.filter = currentFilter;
                }
            },
            error: _ => {
                this.snackBar.open("An unexpected error occurred, reports could not be loaded.", undefined, {
                    duration: 3000,
                    horizontalPosition: "center",
                    verticalPosition: "top"
                })
            }
        });
    }

    searchGame(): void {
        this.problemDescription = '';

        if (!this.gameNumberInput) {
            this.problemDescription = 'Please enter a game number';
            return;
        }

        this.searching = true;
        this.basketplanService.searchGame(this.gameNumberInput.trim()).subscribe({
                next: dto => {
                    if (dto.referee1 && dto.referee2
                        && (dto.officiatingMode === OfficiatingMode.OFFICIATING_2PO || dto.referee3)) {

                        this.game = dto;
                        this.youtubeId = this.game.youtubeId;

                        this.reportee = undefined;
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
                        this.youtubeIdInputNeeded = !this.youtubeId;
                    } else {
                        this.problemDescription = 'At least one referee not available in database';
                    }
                    this.searching = false;
                },
                error: error => {
                    if (error.status === 404) {
                        this.problemDescription = 'No game found for given game number';
                    } else {
                        this.problemDescription = 'An unexpected error occurred'
                    }
                    this.searching = false;
                }
            }
        );
    }

    parseYouTubeUrl() {
        this.problemDescription = '';
        this.youtubeId = undefined;

        if (!this.youtubeUrlInput) {
            this.problemDescription = 'Please enter a YouTube URL';
            return;
        }

        const {id} = getVideoId(this.youtubeUrlInput);
        if (id) {
            this.youtubeId = id;
        } else {
            this.problemDescription = 'Not a valid YouTube URL';
        }
    }

    createVideoReport() {
        this.creating = true;
        if (this.game && (this.textOnlyMode || this.youtubeId) && this.reportee) {
            this.videoReportService.createVideoReport(this.game.gameNumber, this.reportee, this.textOnlyMode ? undefined : this.youtubeId).subscribe({
                next: response => {
                    this.creating = false;
                    this.edit(response);
                },
                error: _ => {
                    this.creating = false;
                    this.snackBar.open("An unexpected error occurred, report could not be created.", undefined, {
                        duration: 3000,
                        horizontalPosition: "center",
                        verticalPosition: "top"
                    })
                }
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
        sessionStorage.setItem(keyFilter, this.videoReportDtos.filter);
    }

    isEditable(report: VideoReportDTO) {
        return !report.finished && this.isCurrentUser(report.reporter);
    }

    isCurrentUser(reporter: ReporterDTO): boolean {
        return reporter.id === this.authenticationService.getUserId();
    }

    edit(report: VideoReportDTO) {
        this.router.navigate([EDIT_PATH, report.id]);
    }

    copy(report: VideoReportDTO) {
        this.dialog.open(VideoReportCopyDialogComponent, {
            data: {
                reportee: report.reportee,
                referee1: report.basketplanGame.referee1,
                referee2: report.basketplanGame.referee2,
                referee3: report.basketplanGame.referee3,
                title: 'Copy Report',
                description: 'A new report will be created containing all comments from the existing source report.'
            } as VideoReportCopyDialogData
        }).afterClosed().subscribe((reportee?: Reportee) => {
            if (reportee) {
                this.reportsLoaded = false;
                this.videoReportService.copyVideoReport(report.id, reportee).subscribe({
                    next: response => {
                        this.edit(response);
                    },
                    error: _ => {
                        this.reportsLoaded = true;
                        this.snackBar.open("An unexpected error occurred, report could not be copied.", undefined, {
                            duration: 3000,
                            horizontalPosition: "center",
                            verticalPosition: "top"
                        })
                    }
                });
            }
        });
    }

    view(report: VideoReportDTO) {
        this.router.navigate([VIEW_PATH, report.id]);
    }

    isDeletable(report: VideoReportDTO): boolean {
        return this.isEditable(report) || this.authenticationService.isAdmin();
    }

    delete(report: VideoReportDTO) {
        this.dialog.open(VideoReportDeleteDialogComponent, {data: report}).afterClosed().subscribe((confirm: boolean) => {
            if (confirm) {
                this.reportsLoaded = false;
                this.videoReportService.deleteVideoReport(report).subscribe({
                    next: _ => {
                        this.loadVideoReports();
                        this.snackBar.open("Report successfully deleted", undefined, {
                            duration: 3000,
                            horizontalPosition: "center",
                            verticalPosition: "top"
                        });
                    },
                    error: _ => {
                        this.snackBar.open("Report could not be deleted", undefined, {
                            duration: 3000,
                            horizontalPosition: "center",
                            verticalPosition: "top"
                        });
                    }
                });
            }
        });
    }

    logout() {
        this.authenticationService.logout();
        this.router.navigate([LOGIN_PATH])
    }

    dateFilterChanged() {
        if (this.from && this.to) {
            this.reportsLoaded = false;
            sessionStorage.setItem(keyFrom, this.from.toFormat(dateFormat));
            sessionStorage.setItem(keyTo, this.to.toFormat(dateFormat));
            this.loadVideoReports();
        }
    }

    export() {
        this.exporting = true;
        this.videoReportService.export().subscribe({
            next: response => {
                saveAs(response, "export.xlsx");
            },
            error: _ => {
                this.snackBar.open("An unexpected error occurred, export could not be created!", undefined, {
                    duration: 3000,
                    horizontalPosition: "center",
                    verticalPosition: "top"
                })
            },
            complete: () => {
                this.exporting = false;
            }
        })
    }
}
