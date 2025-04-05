import {Component, EventEmitter, input, Output} from '@angular/core';
import {CriteriaEvaluationDTO} from "../rest";
import {MatSlider, MatSliderThumb} from '@angular/material/slider';
import {FormsModule} from '@angular/forms';

@Component({
    selector: 'app-video-report-rating',
    templateUrl: './video-report-rating.component.html',
    styleUrls: ['./video-report-rating.component.scss'],
    imports: [MatSlider, MatSliderThumb, FormsModule]
})
export class VideoReportRatingComponent {

    readonly title = input.required<string>();

    readonly dto = input.required<CriteriaEvaluationDTO>();

    @Output()
    changed = new EventEmitter<void>();

    rating() {
        const dto = this.dto();
        if (dto.score) {
            if (dto.score >= 8.6) {
                return "excellent";
            } else if (dto.score >= 8.1) {
                return "very good";
            } else if (dto.score >= 7.6) {
                return "good";
            } else if (dto.score >= 7.1) {
                return "discreet";
            } else if (dto.score >= 6.6) {
                return "sufficient";
            } else {
                return "insufficient";
            }
        }
        return "";
    }
}
