import {AfterViewInit, Component, ElementRef, HostListener, inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {HasUnsavedReplies} from "../has-unsaved-replies";
import {Observable, of} from "rxjs";
import {YouTubePlayer} from "@angular/youtube-player";
import {CommentReplyDTO, GameDiscussionCommentDTO, GameDiscussionCommentReplyDTO, GameDiscussionDTO, OfficiatingMode} from "../rest";
import {ActivatedRoute, RouterLink} from "@angular/router";
import {AuthenticationService} from "../service/authentication.service";
import {MatDialog, MatDialogConfig} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {GameDiscussionService} from "../service/game-discussion.service";
import {UnsavedRepliesDialogComponent} from "../discuss-video-report-unsaved-replies-dialog/unsaved-replies-dialog.component";
import {CommentReplyDialogComponent, CommentReplyDialogData} from "../comment-reply-dialog/comment-reply-dialog.component";
import {GameDiscussionFinishCommentsDialogComponent} from "../game-discussion-finish-comments-dialog/game-discussion-finish-comments-dialog.component";
import {MatToolbar} from '@angular/material/toolbar';
import {DatePipe, NgClass} from '@angular/common';
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

@Component({
    selector: 'app-game-discussion',
    templateUrl: './game-discussion.component.html',
    styleUrls: ['./game-discussion.component.scss'],
    imports: [MatToolbar, MatProgressSpinner, MatIconButton, MatIcon, MatTooltip, MatIconAnchor, RouterLink, MatProgressBar, MatCard, MatCardHeader, MatCardTitle, MatCardContent, YouTubePlayer, MatButton, NgClass, MatFormField, MatLabel, CdkTextareaAutosize, MatInput, FormsModule, MatCardActions, DatePipe]
})
export class GameDiscussionComponent implements OnInit, AfterViewInit, OnDestroy, HasUnsavedReplies {
    private route = inject(ActivatedRoute);
    private gameDiscussionService = inject(GameDiscussionService);
    private authenticationService = inject(AuthenticationService);
    dialog = inject(MatDialog);
    snackBar = inject(MatSnackBar);


    @ViewChild('youtubePlayer') youtube?: YouTubePlayer;
    @ViewChild('widthMeasurement') widthMeasurement?: ElementRef<HTMLDivElement>;

    videoWidth?: number;
    videoHeight?: number;

    gameDiscussion?: GameDiscussionDTO;
    notFound = false;
    saving = false;

    replies: CommentReplyDTO[] = [];
    newComments: GameDiscussionCommentDTO[] = [];

    ngOnInit(): void {
        // This code loads the IFrame Player API code asynchronously, according to the instructions at
        // https://developers.google.com/youtube/iframe_api_reference#Getting_Started
        const tag = document.createElement('script');
        tag.src = 'https://www.youtube.com/iframe_api';
        document.body.appendChild(tag);

        this.gameDiscussionService.getGameDiscussion(this.route.snapshot.paramMap.get('id')!).subscribe({
            next: value => {
                this.gameDiscussion = value;
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
        if (this.hasUnsavedComments()) {
            $event.preventDefault();
        }
    }

    isLoggedIn(): boolean {
        return this.authenticationService.isLoggedIn();
    }

    is2PO(): boolean {
        return this.gameDiscussion?.basketplanGame.officiatingMode === OfficiatingMode.OFFICIATING_2PO;
    }

    is3PO(): boolean {
        return this.gameDiscussion?.basketplanGame.officiatingMode === OfficiatingMode.OFFICIATING_3PO;
    }

    canDeactivate(): Observable<boolean> {
        if (this.hasUnsavedComments()) {
            return this.dialog.open(UnsavedRepliesDialogComponent).afterClosed();
        } else {
            return of(true);
        }
    }

    save() {
        this.dialog.open(GameDiscussionFinishCommentsDialogComponent).afterClosed().subscribe(decision => {
            if (decision && this.gameDiscussion) {
                this.saving = true;
                this.gameDiscussionService.comment(this.gameDiscussion.id, this.replies, this.newComments).subscribe({
                    next: () => {
                        this.showMessage("Comments saved");
                        this.replies = [];
                        this.newComments = [];
                        this.saving = false;
                    },
                    error: () => {
                        this.showMessage("Comments could not be saved...");
                        this.saving = false;
                    }
                });
            }
        });
    }

    hasUnsavedComments() {
        return this.replies.length > 0 || this.newComments.length > 0;
    }

    deleteReply(comment: GameDiscussionCommentDTO, reply: GameDiscussionCommentReplyDTO) {
        comment.replies.splice(comment.replies.indexOf(reply), 1)
        this.replies = this.replies.filter(r => !(r.comment === reply.reply && r.commentId === comment.id));
    }

    play(time: number) {
        this.youtube!.seekTo(time, true);
        this.youtube!.playVideo();
    }

    reply(comment: GameDiscussionCommentDTO) {
        this.dialog.open(CommentReplyDialogComponent, {
            data: {
                referee: ''
            },
            disableClose: true,
            hasBackdrop: true,
        } as MatDialogConfig<CommentReplyDialogData>).afterClosed().subscribe((reply: string) => {
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

    addComment() {
        const timestamp = Math.round(this.youtube!.getCurrentTime());

        // check if there is already a scene in the same timestamp range +/-3 seconds
        if (this.gameDiscussion!.comments.some(comment => timestamp >= comment.timestamp - 3 && timestamp <= comment.timestamp + 3)) {
            this.showMessage('There is already an existing comment around this timestamp');
        } else {
            const newComment: GameDiscussionCommentDTO = {
                id: undefined,
                comment: '',
                timestamp: timestamp,
                replies: [],
            };

            this.gameDiscussion!.comments.push(newComment);
            this.newComments.push(newComment);
        }
    }

    private showMessage(message: string) {
        this.snackBar.open(message, undefined, {
            duration: 3000,
            verticalPosition: "top",
            horizontalPosition: "center"
        });
    }
}
