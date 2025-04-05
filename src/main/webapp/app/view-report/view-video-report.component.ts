import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {VideoReportService} from "../service/video-report.service";
import {YouTubePlayer} from "@angular/youtube-player";
import {OfficiatingMode, Reportee, VideoCommentDTO, VideoReportDTO} from "../rest";
import {AuthenticationService} from "../service/authentication.service";
import {MatDialog} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {DISCUSS_PATH} from "../app-routing.module";
import {MatToolbar} from '@angular/material/toolbar';
import {DatePipe, NgClass} from '@angular/common';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatButton, MatIconAnchor, MatIconButton} from '@angular/material/button';
import {MatTooltip} from '@angular/material/tooltip';
import {MatIcon} from '@angular/material/icon';
import {MatCard, MatCardActions, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';

@Component({
    selector: 'app-view-video-report',
    templateUrl: './view-video-report.component.html',
    styleUrls: ['./view-video-report.component.scss'],
    imports: [MatToolbar, MatProgressSpinner, MatIconButton, MatTooltip, MatIcon, MatIconAnchor, RouterLink, MatCard, MatCardHeader, MatCardTitle, MatCardContent, NgClass, YouTubePlayer, MatCardActions, MatButton, DatePipe]
})
export class ViewVideoReportComponent implements OnInit, AfterViewInit, OnDestroy {

    @ViewChild('youtubePlayer') youtube?: YouTubePlayer;
    @ViewChild('widthMeasurement') widthMeasurement?: ElementRef<HTMLDivElement>;

    videoWidth?: number;
    videoHeight?: number;

    dto?: VideoReportDTO;
    notFound = false;

    constructor(private route: ActivatedRoute,
                private router: Router,
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

        this.videoReportService.getVideoReport(this.route.snapshot.paramMap.get('id')!).subscribe({
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

    play(time: number): void {
        this.youtube!.seekTo(time, true);
        this.youtube!.playVideo();
    }

    is2PO(): boolean {
        return this.dto?.basketplanGame.officiatingMode === OfficiatingMode.OFFICIATING_2PO;
    }

    is3PO(): boolean {
        return this.dto?.basketplanGame.officiatingMode === OfficiatingMode.OFFICIATING_3PO;
    }

    isFirstUmpire(): boolean {
        return this.dto?.reportee === Reportee.FIRST_REFEREE;
    }

    isSecondUmpire(): boolean {
        return this.dto?.reportee === Reportee.SECOND_REFEREE;
    }

    isThirdUmpire(): boolean {
        return this.dto?.reportee === Reportee.THIRD_REFEREE;
    }

    isLoggedIn(): boolean {
        return this.authenticationService.isLoggedIn();
    }

    isReferee(): boolean {
        return this.authenticationService.isReferee();
    }

    isCoach(): boolean {
        return this.authenticationService.isCoach();
    }


    navigateToDiscussion(): void {
        this.router.navigate([DISCUSS_PATH, this.dto?.id]).catch(reason => {
            console.error(reason);
        });
    }

    requiresReply(videoComment: VideoCommentDTO): boolean {
        return (!this.isLoggedIn() || this.isReferee()) && videoComment.requiresReply && videoComment.replies.length < 1;
    }

    flaggedForReply(videoComment: VideoCommentDTO): boolean {
        return this.isCoach() && videoComment.requiresReply;
    }
}
