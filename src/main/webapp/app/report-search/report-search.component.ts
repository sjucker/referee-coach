import {AfterViewInit, Component, ElementRef, inject, OnDestroy, OnInit, viewChild} from '@angular/core';
import {VideoReportService} from "../service/video-report.service";
import {TagDTO, VideoCommentDetailDTO} from "../rest";
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
import {YouTubePlayer} from "@angular/youtube-player";
import {MatPaginator} from "@angular/material/paginator";
import {MatSnackBar} from "@angular/material/snack-bar";
import {Observable, of, share} from "rxjs";
import {MatToolbar} from '@angular/material/toolbar';
import {MatButton, MatIconAnchor, MatIconButton} from '@angular/material/button';
import {RouterLink} from '@angular/router';
import {MatIcon} from '@angular/material/icon';
import {MatCard, MatCardActions, MatCardContent} from '@angular/material/card';
import {TagsSelectionComponent} from '../tags-selection/tags-selection.component';
import {DatePipe} from '@angular/common';
import {MatProgressBar} from '@angular/material/progress-bar';
import {AuthenticationService} from "../service/authentication.service";

@Component({
    selector: 'app-report-search',
    templateUrl: './report-search.component.html',
    styleUrls: ['./report-search.component.scss'],
    imports: [MatToolbar, MatIconAnchor, RouterLink, MatIcon, MatCard, MatCardContent, TagsSelectionComponent, YouTubePlayer, MatCardActions, MatButton, MatProgressBar, MatTable, MatColumnDef, MatHeaderCellDef, MatHeaderCell, MatCellDef, MatCell, MatIconButton, MatHeaderRowDef, MatHeaderRow, MatRowDef, MatRow, MatNoDataRow, MatPaginator, DatePipe]
})
export class ReportSearchComponent implements OnInit, AfterViewInit, OnDestroy {
    private readonly videoReportService = inject(VideoReportService);
    private readonly snackBar = inject(MatSnackBar);
    private readonly authenticationService = inject(AuthenticationService);

    displayedColumns: string[] = this.isCoach()
        ? ['date', 'gameNumber', 'competition', 'comment', 'tags', 'play']
        : ['date', 'gameNumber', 'competition', 'tags', 'play'];

    readonly youtube = viewChild<YouTubePlayer>('youtubePlayer');
    readonly widthMeasurement = viewChild<ElementRef<HTMLDivElement>>('widthMeasurement');

    selectedTags: TagDTO[] = [];
    results: MatTableDataSource<VideoCommentDetailDTO> = new MatTableDataSource<VideoCommentDetailDTO>([]);
    readonly paginator = viewChild(MatPaginator);

    currentVideoId?: string;
    videoWidth?: number;
    videoHeight?: number;

    searching = false;

    availableTags: Observable<TagDTO[]> = of([]);

    ngOnInit(): void {
        // This code loads the IFrame Player API code asynchronously, according to the instructions at
        // https://developers.google.com/youtube/iframe_api_reference#Getting_Started
        const tag = document.createElement('script');
        tag.src = 'https://www.youtube.com/iframe_api';
        document.body.appendChild(tag);

        this.availableTags = this.videoReportService.getAllAvailableTags().pipe(share())
    }

    ngAfterViewInit(): void {
        this.onResize();
        window.addEventListener('resize', this.onResize);
    }

    ngOnDestroy(): void {
        window.removeEventListener('resize', this.onResize);
    }

    selectTag(tag: TagDTO) {
        this.selectedTags.push(tag);
    }

    removeTag(tag: TagDTO) {
        this.selectedTags.splice(this.selectedTags.indexOf(tag), 1);
    }

    search(): void {
        this.searching = true;
        this.videoReportService.search({
            tags: this.selectedTags
        }).subscribe({
            next: response => {
                this.results = new MatTableDataSource<VideoCommentDetailDTO>(response.results);
                const paginator = this.paginator();
                if (paginator) {
                    this.results.paginator = paginator
                }
            },
            error: () => {
                this.searching = false;
                this.snackBar.open("An unexpected error occurred", undefined, {
                    duration: 3000,
                    horizontalPosition: "center",
                    verticalPosition: "top",
                })
            },
            complete: () => {
                this.searching = false;
            }
        });
    }

    play(element: VideoCommentDetailDTO) {
        this.currentVideoId = element.youtubeId;

        const interval = setInterval(() => {
            const youtube = this.youtube();
            if (youtube!.getPlayerState() !== YT.PlayerState.UNSTARTED) {
                youtube!.seekTo(element.timestamp, true);
                youtube!.playVideo();
                clearInterval(interval);
            }
        }, 500);
    }

    onResize = (): void => {
        setTimeout(() => {
            // minus padding (16px each side) and margin (10px each)
            const contentWidth = this.widthMeasurement()!.nativeElement.clientWidth - 52;

            this.videoWidth = Math.min(contentWidth, 720);
            this.videoHeight = this.videoWidth * 0.6;
        });
    };

    isCoach(): boolean {
        return this.authenticationService.isCoach() || this.authenticationService.isRefereeCoach();
    }

}
