import {AfterViewInit, Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {YouTubePlayer} from "@angular/youtube-player";
import {VideoReportService} from "../service/video-report.service";
import {OfficiatingMode, Reportee, TagDTO, VideoCommentDTO, VideoReportDTO} from "../rest";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {MatDialog} from "@angular/material/dialog";
import {VideoReportFinishDialogComponent} from "../video-report-finish-dialog/video-report-finish-dialog.component";
import {MatSnackBar} from "@angular/material/snack-bar";
import {Observable, of, share} from "rxjs";
import {VideoReportUnsavedChangesDialogComponent} from "../video-report-unsaved-changes-dialog/video-report-unsaved-changes-dialog.component";
import {VIEW_PATH} from "../app-routing.module";
import {VideoReportCopyDialogComponent, VideoReportCopyDialogData} from "../video-report-copy-dialog/video-report-copy-dialog.component";
import {MatToolbar} from '@angular/material/toolbar';
import {DatePipe, DecimalPipe, NgClass} from '@angular/common';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatButton, MatIconAnchor, MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatTooltip} from '@angular/material/tooltip';
import {MatProgressBar} from '@angular/material/progress-bar';
import {MatCard, MatCardActions, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {CdkTextareaAutosize} from '@angular/cdk/text-field';
import {MatInput} from '@angular/material/input';
import {FormsModule} from '@angular/forms';
import {VideoReportRatingComponent} from '../video-report-rating/video-report-rating.component';
import {MatCheckbox} from '@angular/material/checkbox';
import {TagsSelectionComponent} from '../tags-selection/tags-selection.component';

@Component({
    selector: 'app-video-report',
    templateUrl: './video-report.component.html',
    styleUrls: ['./video-report.component.scss'],
    imports: [MatToolbar, MatProgressSpinner, MatIconButton, MatIcon, MatTooltip, MatIconAnchor, RouterLink, MatProgressBar, MatCard, MatCardHeader, MatCardTitle, MatCardContent, NgClass, MatFormField, MatLabel, CdkTextareaAutosize, MatInput, FormsModule, VideoReportRatingComponent, YouTubePlayer, MatButton, MatCheckbox, TagsSelectionComponent, MatCardActions, DecimalPipe, DatePipe]
})
export class VideoReportComponent implements OnInit, AfterViewInit, OnDestroy {

    @ViewChild('youtubePlayer') youtube?: YouTubePlayer;
    @ViewChild('widthMeasurement') widthMeasurement?: ElementRef<HTMLDivElement>;
    @ViewChild('videoCommentsContainer') videoCommentsContainer?: ElementRef<HTMLDivElement>;

    videoWidth?: number;
    videoHeight?: number;

    report?: VideoReportDTO;
    notFound = false;
    unsavedChanges = false;
    saving = false;

    availableTags: Observable<TagDTO[]> = of([]);

    constructor(private readonly videoReportService: VideoReportService,
                private route: ActivatedRoute,
                private router: Router,
                public dialog: MatDialog,
                public snackBar: MatSnackBar) {
    }

    ngOnInit(): void {
        // This code loads the IFrame Player API code asynchronously, according to the instructions at
        // https://developers.google.com/youtube/iframe_api_reference#Getting_Started
        const tag = document.createElement('script');
        tag.src = 'https://www.youtube.com/iframe_api';
        document.body.appendChild(tag);

        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.videoReportService.getVideoReport(id).subscribe({
                next: dto => {
                    if (dto.finished) {
                        this.router.navigate([VIEW_PATH, dto.id]).catch(reason => {
                            console.error(reason);
                        });
                    }
                    this.report = dto;
                },
                error: () => {
                    this.notFound = true;
                }
            });
        }

        this.availableTags = this.videoReportService.getAllAvailableTags().pipe(share())
    }

    ngAfterViewInit(): void {
        this.onResize();
        window.addEventListener('resize', this.onResize);
    }

    onResize = (): void => {
        // minus padding (16px each side) and margin (10px each)
        const contentWidth = this.widthMeasurement!.nativeElement.clientWidth - 52;

        this.videoWidth = Math.min(contentWidth, 720);
        this.videoHeight = this.videoWidth * 0.6;
    }

    ngOnDestroy(): void {
        window.removeEventListener('resize', this.onResize);
    }

    @HostListener('window:beforeunload', ['$event'])
    handleClose($event: BeforeUnloadEvent) {
        if (this.unsavedChanges) {
            $event.preventDefault();
        }
    }

    jumpTo(time: number): void {
        this.youtube!.seekTo(time, true);
        this.youtube!.playVideo();
    }

    save() {
        if (this.report) {
            this.saving = true;
            this.videoReportService.saveVideoReport(this.report).subscribe({
                next: response => {
                    this.report = response;
                    this.unsavedChanges = false;
                    this.saving = false;
                    this.showMessage("Successfully saved!");
                },
                error: () => {
                    this.saving = false;
                    this.showMessage("An unexpected error occurred, report could not be saved.");
                }
            });
        }
    }

    finish() {
        if (this.report) {
            if (!this.isCriteriaValid()) {
                this.showMessage("Report is not yet completed, please add a comment for each criteria.")
                return;
            }

            if (!this.isVideoCommentsValid()) {
                this.showMessage("Please flag at least 4 video comments to require reply from referee.");
                return;
            }

            this.dialog.open(VideoReportFinishDialogComponent).afterClosed().subscribe(decision => {
                if (decision && this.report) {
                    this.report = {...this.report, finished: true}
                    this.saving = true;
                    this.videoReportService.saveVideoReport(this.report).subscribe({
                        next: response => {
                            this.unsavedChanges = false;
                            this.saving = false;
                            if (response.finished) {
                                this.router.navigate([VIEW_PATH, response.id]).catch(reason => {
                                    console.error(reason);
                                });
                            }
                        },
                        error: () => {
                            this.saving = false;
                            this.showMessage("An unexpected error occurred, report could not be finished.");
                        }
                    });
                }
            });
        }
    }

    private isCriteriaValid(): boolean {
        if (this.report) {
            return this.isNotEmpty(this.report.general.comment) &&
                this.isNotEmpty(this.report.image.comment) &&
                this.isNotEmpty(this.report.fitness.comment) &&
                this.isNotEmpty(this.report.mechanics.comment) &&
                this.isNotEmpty(this.report.fouls.comment) &&
                this.isNotEmpty(this.report.violations.comment) &&
                this.isNotEmpty(this.report.gameManagement.comment) &&
                this.isNotEmpty(this.report.pointsToKeepComment) &&
                this.isNotEmpty(this.report.pointsToImproveComment);
        }

        return false;
    }

    private isVideoCommentsValid(): boolean {
        if (this.report && this.report.basketplanGame.youtubeId) {
            return this.report.videoComments.filter(value => value.requiresReply).length > 3;
        }
        return true;
    }

    private isNotEmpty(value?: string): boolean {
        return !!value && value.length > 0;
    }

    addVideoComment(): void {
        this.report!.videoComments.push({
            id: undefined,
            comment: '',
            requiresReply: false,
            timestamp: Math.round(this.youtube!.getCurrentTime()),
            replies: [],
            tags: []
        });

        setTimeout(() => {
            if (this.videoCommentsContainer) {
                this.videoCommentsContainer.nativeElement.scrollTop = this.videoCommentsContainer.nativeElement.scrollHeight;
            }
        }, 200);
    }

    deleteComment(videoComment: VideoCommentDTO) {
        this.onChange();
        this.report!.videoComments.splice(this.report!.videoComments.indexOf(videoComment), 1);
    }

    copyComment(videoComment: VideoCommentDTO) {
        this.dialog.open(VideoReportCopyDialogComponent, {
            data: {
                reportee: this.report!.reportee,
                referee1: this.report!.otherReportees.indexOf(Reportee.FIRST_REFEREE) >= 0 ? this.report!.basketplanGame.referee1 : null,
                referee2: this.report!.otherReportees.indexOf(Reportee.SECOND_REFEREE) >= 0 ? this.report!.basketplanGame.referee2 : null,
                referee3: this.report!.otherReportees.indexOf(Reportee.THIRD_REFEREE) >= 0 ? this.report!.basketplanGame.referee3 : null,

                title: 'Copy Comment to other Report',
                description: 'This will create the same comment in the report for selected referee.'
            } as VideoReportCopyDialogData
        }).afterClosed().subscribe((reportee?: Reportee) => {
            if (reportee) {
                this.videoReportService.copyVideoComment(videoComment, reportee).subscribe({
                    next: () => {
                        this.showMessage("Successfully copied!");
                    },
                    error: () => {
                        this.showMessage("An unexpected error occurred, comment could not be copied.");
                    }
                })
            }
        });
    }

    isCopyCommentVisible(videoComment: VideoCommentDTO): boolean {
        return this.report!.otherReportees.length > 0 && !!videoComment.id
    }

    is2PO(): boolean {
        return this.report?.basketplanGame.officiatingMode === OfficiatingMode.OFFICIATING_2PO;
    }

    is3PO(): boolean {
        return this.report?.basketplanGame.officiatingMode === OfficiatingMode.OFFICIATING_3PO;
    }

    isFirstUmpire(): boolean {
        return this.report?.reportee === Reportee.FIRST_REFEREE;
    }

    isSecondUmpire(): boolean {
        return this.report?.reportee === Reportee.SECOND_REFEREE;
    }

    isThirdUmpire(): boolean {
        return this.report?.reportee === Reportee.THIRD_REFEREE;
    }

    canDeactivate(): Observable<boolean> {
        if (this.unsavedChanges) {
            return this.dialog.open(VideoReportUnsavedChangesDialogComponent).afterClosed();
        } else {
            return of(true);
        }
    }

    onChange() {
        this.unsavedChanges = true;
    }

    private showMessage(message: string) {
        this.snackBar.open(message, undefined, {
            duration: 3000,
            verticalPosition: "top",
            horizontalPosition: "center"
        });
    }

    getAverage(): number {
        return ((this.report?.image.score ?? 0)
            + (this.report?.fitness.score ?? 0)
            + (this.report?.mechanics.score ?? 0)
            + (this.report?.fouls.score ?? 0)
            + (this.report?.violations.score ?? 0)
            + (this.report?.gameManagement.score ?? 0)) / 6;
    }

    selectTag(videoComment: VideoCommentDTO, tag: TagDTO) {
        this.onChange();
        videoComment.tags.push(tag);
    }

    removeTag(videoComment: VideoCommentDTO, tag: TagDTO) {
        this.onChange();
        videoComment.tags.splice(videoComment.tags.indexOf(tag), 1);
    }
}
