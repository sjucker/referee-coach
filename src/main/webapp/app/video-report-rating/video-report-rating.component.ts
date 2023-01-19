import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CriteriaEvaluationDTO} from "../rest";

@Component({
    selector: 'app-video-report-rating',
    templateUrl: './video-report-rating.component.html',
    styleUrls: ['./video-report-rating.component.scss']
})
export class VideoReportRatingComponent {

    @Input()
    title = '';

    @Input()
    dto: CriteriaEvaluationDTO = {
        rating: undefined,
        score: undefined,
        comment: undefined
    }

    @Output()
    changed = new EventEmitter<any>();

    constructor() {
    }

    rating() {
        if (this.dto.score) {
            if (this.dto.score >= 8.6) {
                return "excellent";
            } else if (this.dto.score >= 8.1) {
                return "very good";
            } else if (this.dto.score >= 7.6) {
                return "good";
            } else if (this.dto.score >= 7.1) {
                return "discreet";
            } else if (this.dto.score >= 6.6) {
                return "sufficient";
            } else {
                return "insufficient";
            }
        }
        return "";
    }
}
