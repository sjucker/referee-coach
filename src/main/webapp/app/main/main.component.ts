import {Component, inject, OnInit, viewChild} from '@angular/core';
import {BasketplanGameDTO, GameDiscussionDTO, OfficiatingMode, OverviewDTO, Reportee, ReportType, UserDTO} from "../rest";
import {VideoReportService} from "../service/video-report.service";
import {BasketplanService} from "../service/basketplan.service";
import {Router, RouterLink} from "@angular/router";
import {
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatHeaderCell,
    MatHeaderCellDef,
    MatHeaderRow,
    MatHeaderRowDef,
    MatNoDataRow,
    MatRow,
    MatRowDef,
    MatTable,
    MatTableDataSource
} from "@angular/material/table";
import {AuthenticationService} from "../service/authentication.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {MatDialog} from "@angular/material/dialog";
import getVideoId from "get-video-id";
import {MatPaginator} from "@angular/material/paginator";
import {EDIT_PATH, GAME_DISCUSSION_PATH, LOGIN_PATH, VIEW_PATH} from "../app-routing.module";
import {saveAs} from "file-saver";
import {DateTime} from "luxon";
import {GameDiscussionService} from "../service/game-discussion.service";
import {SearchService} from "../service/search.service";
import {VideoReportCopyDialogComponent, VideoReportCopyDialogData} from "../video-report-copy-dialog/video-report-copy-dialog.component";
import {VideoReportDeleteDialogComponent} from "../video-report-delete-dialog/video-report-delete-dialog.component";
import {MatToolbar} from '@angular/material/toolbar';
import {DatePipe} from '@angular/common';
import {MatButton, MatIconAnchor, MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatTooltip} from '@angular/material/tooltip';
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';
import {FormsModule} from '@angular/forms';
import {MatFormField, MatLabel, MatSuffix} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatCheckbox} from '@angular/material/checkbox';
import {MatOption, MatSelect} from '@angular/material/select';
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from '@angular/material/datepicker';
import {MatProgressBar} from '@angular/material/progress-bar';

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
    styleUrls: ['./main.component.scss'],
    imports: [MatToolbar, MatIconAnchor, RouterLink, MatIcon, MatIconButton, MatTooltip, MatCard, MatCardContent, FormsModule, MatFormField, MatLabel, MatInput, MatButton, MatCheckbox, MatSelect, MatOption, MatCardHeader, MatCardTitle, MatDatepickerInput, MatDatepickerToggle, MatSuffix, MatDatepicker, MatProgressBar, MatTable, MatColumnDef, MatHeaderCellDef, MatHeaderCell, MatCellDef, MatCell, MatHeaderRowDef, MatHeaderRow, MatRowDef, MatRow, MatNoDataRow, MatPaginator, DatePipe]
})
export class MainComponent implements OnInit {
    private readonly basketplanService = inject(BasketplanService);
    private readonly videoReportService = inject(VideoReportService);
    private readonly searchService = inject(SearchService);
    private readonly gameDiscussionService = inject(GameDiscussionService);
    private readonly authenticationService = inject(AuthenticationService);
    private readonly router = inject(Router);
    private readonly snackBar = inject(MatSnackBar);
    private readonly dialog = inject(MatDialog);

    dtos: MatTableDataSource<OverviewDTO> = new MatTableDataSource<OverviewDTO>([]);
    readonly paginator = viewChild(MatPaginator);
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
        this.searchService.find(this.from, this.to).subscribe({
            next: value => {
                this.reportsLoaded = true;
                this.dtos = new MatTableDataSource<OverviewDTO>(value);
                const paginator = this.paginator();
                if (paginator) {
                    this.dtos.paginator = paginator
                }
                this.dtos.filterPredicate = (data, filter) => {
                    // default filter cannot handle nested objects, so handle each column specifically
                    return data.gameNumber.toLowerCase().indexOf(filter) != -1
                        || data.competition.toLowerCase().indexOf(filter) != -1
                        || data.teamA.toLowerCase().indexOf(filter) != -1
                        || data.teamB.toLowerCase().indexOf(filter) != -1
                        || data.teamB.toLowerCase().indexOf(filter) != -1
                        || (data.type === ReportType.GAME_DISCUSSION ? false : data.coach?.name.toLowerCase().indexOf(filter) != -1)
                        || (data.type === ReportType.GAME_DISCUSSION ? false : data.relevantReferee?.name.toLowerCase().indexOf(filter) != -1);
                }
                const currentFilter = sessionStorage.getItem(keyFilter);
                if (currentFilter) {
                    this.dtos.filter = currentFilter;
                }
            },
            error: () => {
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

    searchGameForReferee(): void {
        this.problemDescription = '';
        this.game = undefined;

        if (!this.gameNumberInput) {
            this.problemDescription = 'Please enter a game number';
            return;
        }

        this.searching = true;
        this.basketplanService.searchGameForReferee(this.gameNumberInput.trim()).subscribe({
                next: dto => {
                    this.game = dto;
                    this.youtubeId = this.game.youtubeId;
                    this.youtubeIdInputNeeded = !this.youtubeId;
                    this.searching = false;
                },
                error: error => {
                    if (error.status === 404) {
                        this.problemDescription = 'No game found for given game number that is relevant for you';
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
        if (this.game && (this.textOnlyMode || this.youtubeId) && this.reportee) {
            this.creating = true;
            this.videoReportService.createVideoReport(this.game.gameNumber, this.reportee, this.textOnlyMode ? undefined : this.youtubeId).subscribe({
                next: response => {
                    this.creating = false;
                    this.edit(response.id, ReportType.COACHING);
                },
                error: () => {
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

    createGameDiscussion() {
        if (this.game && this.youtubeId) {
            this.creating = true;
            this.gameDiscussionService.createGameDiscussion(this.game.gameNumber, this.youtubeId).subscribe({
                next: response => {
                    this.creating = false;
                    this.discuss(response);
                },
                error: err => {
                    this.creating = false;
                    if (err.status === 204) {
                        this.snackBar.open("There is already a game discussion for this game number.", undefined, {
                            duration: 3000,
                            horizontalPosition: "center",
                            verticalPosition: "top"
                        });
                    } else {
                        this.snackBar.open("An unexpected error occurred, report could not be created.", undefined, {
                            duration: 3000,
                            horizontalPosition: "center",
                            verticalPosition: "top"
                        });
                    }
                }
            })
        } else {
            this.snackBar.open("Please search for a game", undefined, {
                duration: 3000,
                horizontalPosition: "center",
                verticalPosition: "top"
            });
        }

    }

    applyFilter(event: Event) {
        const filterValue = (event.target as HTMLInputElement).value;
        this.dtos.filter = filterValue.trim().toLowerCase();
        sessionStorage.setItem(keyFilter, this.dtos.filter);
    }

    isEditable(report: OverviewDTO) {
        if (report.coach) {
            return !report.finished && this.isCurrentUser(report.coach);
        } else {
            return false;
        }
    }

    isCurrentUser(coach?: UserDTO): boolean {
        if (coach) {
            return coach.id === this.authenticationService.getUserId();
        } else {
            return false;
        }
    }

    getCoach(coach?: UserDTO): string {
        if (coach) {
            return coach.name;
        } else {
            return "";
        }
    }

    getReferee(referee?: UserDTO): string {
        if (referee) {
            return referee.name;
        } else {
            return "";
        }
    }

    edit(id: string, type: ReportType) {
        if (type === ReportType.COACHING) {
            this.router.navigate([EDIT_PATH, id]).catch(reason => {
                console.error(reason);
            });
        }
    }

    discuss(gameDiscussion: GameDiscussionDTO) {
        this.router.navigate([GAME_DISCUSSION_PATH, gameDiscussion.id]).catch(reason => {
            console.error(reason);
        });
    }

    copy(report: OverviewDTO) {
        this.dialog.open(VideoReportCopyDialogComponent, {
            data: {
                reportee: report.reportee,
                referee1: report.referee1,
                referee2: report.referee2,
                referee3: report.referee3,
                title: 'Copy Report',
                description: 'A new report will be created containing all comments from the existing source report.'
            } as VideoReportCopyDialogData
        }).afterClosed().subscribe((reportee?: Reportee) => {
            if (reportee) {
                this.reportsLoaded = false;
                this.videoReportService.copyVideoReport(report.id, reportee).subscribe({
                    next: response => {
                        this.edit(response.id, ReportType.COACHING);
                    },
                    error: () => {
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

    view(report: OverviewDTO) {
        this.router.navigate([report.type === ReportType.GAME_DISCUSSION ? GAME_DISCUSSION_PATH : VIEW_PATH, report.id]).catch(reason => {
            console.error(reason);
        })
    }

    isDeletable(report: OverviewDTO): boolean {
        return this.isCoaching(report) && (this.isEditable(report) || this.authenticationService.isAdmin());
    }

    delete(report: OverviewDTO) {
        this.dialog.open(VideoReportDeleteDialogComponent, {data: report}).afterClosed().subscribe((confirm: boolean) => {
            if (confirm) {
                this.reportsLoaded = false;
                this.videoReportService.deleteVideoReport(report.id).subscribe({
                    next: () => {
                        this.loadVideoReports();
                        this.snackBar.open("Report successfully deleted", undefined, {
                            duration: 3000,
                            horizontalPosition: "center",
                            verticalPosition: "top"
                        });
                    },
                    error: () => {
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
        this.router.navigate([LOGIN_PATH]).catch(reason => {
            console.error(reason);
        });
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
            error: () => {
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

    isCoach(): boolean {
        return this.authenticationService.isCoach();
    }

    isRefereeCoach(): boolean {
        return this.authenticationService.isRefereeCoach();
    }

    isReferee(): boolean {
        return this.authenticationService.isReferee();
    }

    get displayedColumns(): string[] {
        if (this.isCoach() || this.isRefereeCoach()) {
            return ['finished', 'date', 'gameNumber', 'competition', 'teams', 'coach', 'reportee', 'edit', 'view', 'copy', 'delete'];
        } else {
            return ['date', 'gameNumber', 'competition', 'teams', 'coach', 'view'];
        }
    }

    isCoaching(report: OverviewDTO): boolean {
        return report.type === ReportType.COACHING;
    }
}
