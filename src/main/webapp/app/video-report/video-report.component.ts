import {Component, OnInit, ViewChild} from '@angular/core';
import {YouTubePlayer} from "@angular/youtube-player";
import {BasketplanService} from "../service/basketplan.service";
import {VideoReportService} from "../service/video-report.service";
import {OfficiatingMode, Reportee, VideoCommentDTO, VideoReportDTO} from "../rest";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
    selector: 'app-video-report',
    templateUrl: './video-report.component.html',
    styleUrls: ['./video-report.component.css']
})
export class VideoReportComponent implements OnInit {

    @ViewChild('youtubePlayer') youtube?: YouTubePlayer;

    report?: VideoReportDTO;

    constructor(private readonly basketplanService: BasketplanService,
                private readonly videoReportService: VideoReportService,
                private route: ActivatedRoute,
                private router: Router) {
    }

    ngOnInit(): void {
        // This code loads the IFrame Player API code asynchronously, according to the instructions at
        // https://developers.google.com/youtube/iframe_api_reference#Getting_Started
        const tag = document.createElement('script');
        tag.src = 'https://www.youtube.com/iframe_api';
        document.body.appendChild(tag);

        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.videoReportService.getVideoReport(id).subscribe(dto => {
                // TODO error handling
                // TODO if finished redirect to read-only view
                this.report = dto;
            });
        }
    }

    jumpTo(time: number): void {
        this.youtube!.seekTo(time, true)
    }

    save() {
        if (this.report) {
            this.videoReportService.saveVideoReport(this.report).subscribe(dto => {
                this.report = dto;
            })
        }
    }

    finish() {
        if (this.report) {
            this.report = {...this.report, finished: true}
            this.videoReportService.saveVideoReport(this.report).subscribe(response => {
                if (response.finished) {
                    this.router.navigate(['/view/' + response.id]);
                }
            })
        }
    }

    addVideoComment(): void {
        this.report!.videoComments.push({
            comment: '',
            timestamp: Math.round(this.youtube!.getCurrentTime())
        })
    }

    deleteComment(videoComment: VideoCommentDTO) {
        this.report!.videoComments.splice(this.report!.videoComments.indexOf(videoComment), 1);
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

}