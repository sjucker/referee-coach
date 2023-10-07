import {AfterViewInit, Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {VideoReportService} from "../service/video-report.service";
import {AuthenticationService} from "../service/authentication.service";
import {MatDialog, MatDialogConfig} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {CommentReplyDTO, OfficiatingMode, VideoCommentDTO, VideoCommentReplyDTO, VideoReportDiscussionDTO} from "../rest";
import {YouTubePlayer} from "@angular/youtube-player";
import {CommentReplyDialogComponent, CommentReplyDialogData} from "../comment-reply-dialog/comment-reply-dialog.component";
import {Observable, of} from "rxjs";
import {UnsavedRepliesDialogComponent} from "../discuss-video-report-unsaved-replies-dialog/unsaved-replies-dialog.component";
import {DiscussVideoReportFinishDialogComponent} from "../discuss-video-report-finish-dialog/discuss-video-report-finish-dialog.component";
import {HasUnsavedReplies} from "../has-unsaved-replies";

@Component({
    selector: 'app-discuss-video-report',
    templateUrl: './discuss-video-report.component.html',
    styleUrls: ['./discuss-video-report.component.scss']
})
export class DiscussVideoReportComponent implements OnInit, AfterViewInit, OnDestroy, HasUnsavedReplies {

    @ViewChild('youtubePlayer') youtube?: YouTubePlayer;
    @ViewChild('widthMeasurement') widthMeasurement?: ElementRef<HTMLDivElement>;

    videoWidth?: number;
    videoHeight?: number;

    dto?: VideoReportDiscussionDTO;
    notFound = false;
    saving = false;

    replies: CommentReplyDTO[] = [];
    newComments: VideoCommentDTO[] = [];

    constructor(private route: ActivatedRoute,
                private videoReportService: VideoReportService,
                private authenticationService: AuthenticationService,
                public dialog: MatDialog,
                public snackBar: MatSnackBar) {
    }

    ngOnInit(): void {
        // This code loads the IFrame Player API code asynchronously, according to the instructions at
        // https://developers.google.com/youtube/iframe_api_reference#Getting_Started
        const tag = document.createElement('script');
        tag.src = 'https://www.youtube.com/iframe_api';
        document.body.appendChild(tag);

        this.videoReportService.getVideoReportDiscussion(this.route.snapshot.paramMap.get('id')!).subscribe({
            next: result => {
                this.dto = result;
            },
            error: () => {
                this.notFound = true;
            }
        });
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
        if (this.hasUnsavedReplies()) {
            $event.returnValue = 'hasUnsavedReplies';
        }
    }

    isLoggedIn(): boolean {
        return this.authenticationService.isLoggedIn();
    }


    is2PO(): boolean {
        return this.dto?.basketplanGame.officiatingMode === OfficiatingMode.OFFICIATING_2PO;
    }

    is3PO(): boolean {
        return this.dto?.basketplanGame.officiatingMode === OfficiatingMode.OFFICIATING_3PO;
    }

    play(time: number): void {
        this.youtube!.seekTo(time, true);
        this.youtube!.playVideo();
    }

    reply(comment: VideoCommentDTO): void {
        this.dialog.open(CommentReplyDialogComponent, {
            data: {
                referee: this.dto?.referee
            },
            disableClose: true,
            hasBackdrop: true,
        } as MatDialogConfig<CommentReplyDialogData>).afterClosed().subscribe(reply => {
            if (reply) {
                this.replies = [...this.replies, {
                    commentId: comment.id!,
                    comment: reply
                }];
                comment.replies.push({
                    id: 0,
                    reply: reply,
                    repliedAt: new Date(),
                    repliedBy: 'New Reply'
                })
            }
        });
    }

    finishReply(): void {
        this.dialog.open(DiscussVideoReportFinishDialogComponent).afterClosed().subscribe(decision => {
            if (decision && this.dto) {
                this.saving = true;
                this.videoReportService.reply(this.dto.videoReportId, this.replies, this.newComments).subscribe({
                    next: () => {
                        this.showMessage("Replies saved");
                        this.replies = [];
                        this.newComments = [];
                        this.saving = false;
                    },
                    error: () => {
                        this.showMessage("Replies could not be saved...");
                        this.saving = false;
                    }
                });
            }
        });
    }

    private showMessage(message: string) {
        this.snackBar.open(message, undefined, {
            duration: 3000,
            verticalPosition: "top",
            horizontalPosition: "center"
        });
    }

    hasUnsavedReplies(): boolean {
        return this.replies.length > 0 || this.newComments.length > 0;
    }

    canDeactivate(): Observable<boolean> {
        if (this.hasUnsavedReplies()) {
            return this.dialog.open(UnsavedRepliesDialogComponent).afterClosed();
        } else {
            return of(true);
        }
    }

    deleteReply(videoComment: VideoCommentDTO, reply: VideoCommentReplyDTO) {
        videoComment.replies.splice(videoComment.replies.indexOf(reply), 1)
        this.replies = this.replies.filter(r => !(r.comment === reply.reply && r.commentId === videoComment.id));
    }

    addVideoComment() {
        const timestamp = Math.round(this.youtube!.getCurrentTime());

        // check if there is already a comment in the same timestamp range +/-3 seconds
        if (this.dto!.videoComments.some(comment => timestamp >= comment.timestamp - 3 && timestamp <= comment.timestamp + 3)) {
            this.showMessage('There is already an existing comment around this timestamp');
        } else {
            const newComment = {
                id: undefined,
                comment: '',
                timestamp: timestamp,
                replies: [],
                tags: []
            };

            this.dto!.videoComments.push(newComment);
            this.newComments.push(newComment);
        }
    }
}
