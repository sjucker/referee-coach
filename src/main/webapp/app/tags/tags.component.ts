import {AfterViewInit, ChangeDetectionStrategy, Component, inject, OnInit, ViewChild} from '@angular/core';
import {AdminService} from "../service/admin.service";
import {TagNameDTO, TagUsageDTO} from "../rest";

import {MatTableDataSource, MatTableModule} from "@angular/material/table";
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {RouterLink} from "@angular/router";
import {MatPaginator, MatPaginatorModule} from "@angular/material/paginator";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatDialog, MatDialogModule} from "@angular/material/dialog";
import {MatCardModule} from "@angular/material/card";
import {MatSnackBar} from "@angular/material/snack-bar";
import {TagDialogComponent} from "./tag-dialog/tag-dialog.component";
import {TagDeleteDialogComponent} from "./tag-delete-dialog/tag-delete-dialog.component";

@Component({
    selector: 'app-tags',
    templateUrl: './tags.component.html',
    styleUrls: ['./tags.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatTableModule, MatToolbarModule, MatButtonModule, MatIconModule, MatTooltipModule, RouterLink, MatPaginatorModule, MatFormFieldModule, MatInputModule, MatDialogModule, MatCardModule]
})
export class TagsComponent implements OnInit, AfterViewInit {

    private adminService = inject(AdminService);
    private dialog = inject(MatDialog);
    private snackBar = inject(MatSnackBar);

    dataSource: MatTableDataSource<TagUsageDTO> = new MatTableDataSource<TagUsageDTO>();
    displayedColumns: string[] = ['name', 'usageCount', 'actions'];

    @ViewChild(MatPaginator) paginator!: MatPaginator;

    ngOnInit(): void {
        this.loadTags();
    }

    private loadTags() {
        this.adminService.getAllTags().subscribe(tags => {
            this.dataSource.data = tags;
            this.dataSource.paginator = this.paginator;
        });
    }

    ngAfterViewInit() {
        this.dataSource.paginator = this.paginator;
    }

    applyFilter(event: Event) {
        const filterValue = (event.target as HTMLInputElement).value;
        this.dataSource.filter = filterValue.trim().toLowerCase();
    }

    openCreateTagDialog(): void {
        this.dialog.open(TagDialogComponent, {data: null})
            .afterClosed()
            .subscribe((result: TagNameDTO | undefined) => {
                if (result) {
                    this.adminService.createTag(result).subscribe({
                        next: () => {
                            this.loadTags();
                            this.showMessage("Tag created.");
                        },
                        error: () => this.showMessage("Could not create tag. Does it already exist?")
                    });
                }
            });
    }

    openRenameTagDialog(tag: TagUsageDTO): void {
        this.dialog.open(TagDialogComponent, {data: tag})
            .afterClosed()
            .subscribe((result: TagNameDTO | undefined) => {
                if (result) {
                    this.adminService.renameTag(tag.id, result).subscribe({
                        next: () => {
                            this.loadTags();
                            this.showMessage("Tag renamed.");
                        },
                        error: () => this.showMessage("Could not rename tag. Does the name already exist?")
                    });
                }
            });
    }

    openDeleteTagDialog(tag: TagUsageDTO): void {
        this.dialog.open(TagDeleteDialogComponent, {data: tag})
            .afterClosed()
            .subscribe((confirmed: boolean | undefined) => {
                if (confirmed) {
                    this.adminService.deleteTag(tag.id).subscribe({
                        next: () => {
                            this.loadTags();
                            this.showMessage("Tag deleted.");
                        },
                        error: () => this.showMessage("Could not delete tag...")
                    });
                }
            });
    }

    private showMessage(message: string) {
        this.snackBar.open(message, undefined, {
            duration: 3000,
            horizontalPosition: "center",
            verticalPosition: "top",
        });
    }
}
