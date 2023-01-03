import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {VideoReportService} from "../service/video-report.service";
import {TagDTO, VideoCommentDetailDTO} from "../rest";
import {MatTableDataSource} from "@angular/material/table";
import {YouTubePlayer} from "@angular/youtube-player";
import {MatPaginator} from "@angular/material/paginator";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
    selector: 'app-report-search',
    templateUrl: './report-search.component.html',
    styleUrls: ['./report-search.component.css']
})
export class ReportSearchComponent implements OnInit, AfterViewInit, OnDestroy {

    displayedColumns: string[] = ['date', 'gameNumber', 'competition', 'comment', 'tags', 'play'];

    @ViewChild('youtubePlayer') youtube?: YouTubePlayer;
    @ViewChild('widthMeasurement') widthMeasurement?: ElementRef<HTMLDivElement>;

    selectedTags: TagDTO[] = [];
    results: MatTableDataSource<VideoCommentDetailDTO> = new MatTableDataSource<VideoCommentDetailDTO>([]);
    @ViewChild(MatPaginator) paginator: MatPaginator | undefined;

    currentVideoId?: string;
    videoWidth?: number;
    videoHeight?: number;

    searching = false;

    constructor(private readonly videoReportService: VideoReportService,
                private snackBar: MatSnackBar) {
    }

    ngOnInit(): void {
        // This code loads the IFrame Player API code asynchronously, according to the instructions at
        // https://developers.google.com/youtube/iframe_api_reference#Getting_Started
        const tag = document.createElement('script');
        tag.src = 'https://www.youtube.com/iframe_api';
        document.body.appendChild(tag);
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
                if (this.paginator) {
                    this.results.paginator = this.paginator
                }
            },
            error: _ => {
                this.searching = false;
                this.snackBar.open("An unexpected error occurred", undefined, {
                    duration: 3000,
                    horizontalPosition: "center",
                    verticalPosition: "top"
                })
            },
            complete: () => {
                this.searching = false;
            }
        });
    }

    play(element: VideoCommentDetailDTO) {
        this.currentVideoId = element.youtubeId;


        console.log(this.youtube!.getPlayerState());

        const interval = setInterval(() => {
            if (this.youtube!.getPlayerState() !== YT.PlayerState.UNSTARTED) {
                this.youtube!.seekTo(element.timestamp, true);
                this.youtube!.playVideo();
                clearInterval(interval);
            }
        }, 500);

        setTimeout(() => {
            console.log(this.youtube!.getPlayerState());
        }, 1000);
    }

    onResize = (): void => {
        // minus padding (16px each side) and margin (10px each)
        const contentWidth = this.widthMeasurement!.nativeElement.clientWidth - 52;

        this.videoWidth = Math.min(contentWidth, 720);
        this.videoHeight = this.videoWidth * 0.6;
    }

}
