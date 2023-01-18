import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {TagDTO} from "../rest";
import {FormControl} from "@angular/forms";
import {MatAutocompleteSelectedEvent} from "@angular/material/autocomplete";
import {Observable, of} from "rxjs";

@Component({
    selector: 'app-tags-selection',
    templateUrl: './tags-selection.component.html',
    styleUrls: ['./tags-selection.component.scss']
})
export class TagsSelectionComponent implements OnInit {

    @ViewChild('tagInput') tagInput?: ElementRef<HTMLInputElement>;

    @Input()
    availableTags: Observable<TagDTO[]> = of([]);
    allTags: TagDTO[] = [];

    @Input()
    initialSelectedTags: TagDTO[] = [];

    selectedTags: TagDTO[] = [];

    @Output()
    selected = new EventEmitter<TagDTO>();

    @Output()
    removed = new EventEmitter<TagDTO>();

    filteredTags: TagDTO[] = [];
    tagController = new FormControl('');

    constructor() {
    }

    ngOnInit(): void {
        this.availableTags.subscribe(value => {
            this.filteredTags = [...value];
            this.allTags = [...value];
        });
        this.selectedTags = [...this.initialSelectedTags];

        this.tagController.valueChanges.subscribe(value => {
            if (value && value.length > 0) {
                this.filteredTags = this.allTags.filter(tag => tag.name.toLowerCase().includes(value.toLowerCase()));
            } else {
                this.filteredTags = this.allTags.slice();
            }
        });
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
