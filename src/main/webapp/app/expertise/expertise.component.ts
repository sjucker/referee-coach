import {Component, OnInit, ViewChild} from '@angular/core';
import {YouTubePlayer} from "@angular/youtube-player";
import {BasketplanService} from "../service/basketplan.service";
import {ExpertiseService} from "../service/expertise.service";
import {BasketplanGameDTO, ExpertiseDTO, Reportee, VideoCommentDTO} from "../rest";
import {ActivatedRoute, Router} from "@angular/router";

interface VideoExpertiseModel {
    gameNumber: string;
    youtubeLink: string;
    expertise?: ExpertiseDTO;
}

@Component({
    selector: 'app-expertise',
    templateUrl: './expertise.component.html',
    styleUrls: ['./expertise.component.css']
})
export class ExpertiseComponent implements OnInit {

    @ViewChild('youtubePlayer') youtube?: YouTubePlayer;

    reportees: Map<Reportee, string> = new Map<Reportee, string>();

    model: VideoExpertiseModel = {
        gameNumber: '21-05249', // TODO remove
        youtubeLink: '',
        expertise: undefined
    }

    constructor(private readonly basketplanService: BasketplanService,
                private readonly expertiseService: ExpertiseService,
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
            this.expertiseService.getExpertise(id).subscribe(dto => {
                this.model.expertise = dto;
                this.fillReporteeMap(dto.basketplanGame);
            });
        }
    }

    jumpTo(time: number): void {
        this.youtube!.seekTo(time, true)
    }

    searchGame() {
        // TODO error handling
        this.basketplanService.searchGame(this.model.gameNumber).subscribe(result => {
            this.model.expertise = {
                id: undefined,
                basketplanGame: result,
                reportee: Reportee.FIRST_REFEREE,
                imageComment: '',
                mechanicsComment: '',
                foulsComment: '',
                gameManagementComment: '',
                pointsToKeepComment: '',
                pointsToImproveComment: '',
                videoComments: [],
            }
            this.fillReporteeMap(result);
        })
    }

    private fillReporteeMap(result: BasketplanGameDTO) {
        this.reportees.set(Reportee.FIRST_REFEREE, 'Umpire 1');
        this.reportees.set(Reportee.SECOND_REFEREE, 'Umpire 2');
        if (result.referee3) {
            this.reportees.set(Reportee.THIRD_REFEREE, "Umpire 3");
        }
    }

    loadVideo() {
        if (this.model.expertise && this.model.youtubeLink) {
            const match = this.model.youtubeLink.match(/v=([^&]+)/);
            if (match) {
                this.model.expertise.basketplanGame.youtubeId = match[1];
            } else {
                alert("Could not parse provided YouTube link...")
            }
        } else {
            // TODO alert
        }

    }

    save() {
        this.expertiseService.saveExpertise(this.model.expertise!).subscribe(dto => {
            this.model.expertise = dto;
            if (this.route.snapshot.url.length == 0) {
                this.router.navigate(['/edit/' + dto.id]);
            }
        })
    }

    addVideoComment(): void {
        this.model.expertise!.videoComments.push({
            comment: '',
            timestamp: Math.round(this.youtube!.getCurrentTime())
        })
    }

    deleteComment(videoComment: VideoCommentDTO) {
        this.model.expertise!.videoComments.splice(this.model.expertise!.videoComments.indexOf(videoComment), 1);
    }

}
