import {Component, ElementRef, EventEmitter, OnInit, Output, ViewChild} from '@angular/core';
import {TagDTO} from "../rest";
import {FormControl} from "@angular/forms";
import {MatAutocompleteSelectedEvent} from "@angular/material/autocomplete";
import {VideoReportService} from "../service/video-report.service";

@Component({
    selector: 'app-tags-selection',
    templateUrl: './tags-selection.component.html',
    styleUrls: ['./tags-selection.component.css']
})
export class TagsSelectionComponent implements OnInit {

    @ViewChild('tagInput') tagInput?: ElementRef<HTMLInputElement>;

    selectedTags: TagDTO[] = [];

    @Output()
    selected = new EventEmitter<TagDTO>();

    @Output()
    removed = new EventEmitter<TagDTO>();

    tags: TagDTO[] = [];
    filteredTags: TagDTO[] = [];
    tagController = new FormControl('');

    constructor(private readonly videoReportService: VideoReportService) {
    }

    ngOnInit(): void {
        this.videoReportService.getAllAvailableTags().subscribe(tags => {
            this.tags = tags;
            this.filteredTags = tags;
        });

        this.tagController.valueChanges.subscribe(value => {
            if (value && value.length > 0) {
                this.filteredTags = this.tags.filter(tag => tag.name.toLowerCase().includes(value.toLowerCase()));
            } else {
                this.filteredTags = this.tags.slice();
            }
        })
    }

    removeTag(tag: TagDTO) {
        this.selectedTags.splice(this.selectedTags.indexOf(tag), 1);
        this.removed.emit(tag);
    }

    selectTag($event: MatAutocompleteSelectedEvent) {
        this.selectedTags.push($event.option.value);
        this.selected.emit($event.option.value);
        this.tagController.setValue(null);
        this.tagInput!.nativeElement.value = '';
    }
}
