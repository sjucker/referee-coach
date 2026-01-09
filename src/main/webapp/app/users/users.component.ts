import {AfterViewInit, Component, inject, OnInit, ViewChild} from '@angular/core';
import {AdminService} from "../service/admin.service";
import {CreateUserDTO, UserDTO} from "../rest";

import {MatTableDataSource, MatTableModule} from "@angular/material/table";
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {RouterLink} from "@angular/router";
import {AuthenticationService} from "../service/authentication.service";
import {MatPaginator, MatPaginatorModule} from "@angular/material/paginator";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatDialog, MatDialogModule} from "@angular/material/dialog";
import {CreateUserDialogComponent} from "./create-user-dialog/create-user-dialog.component";
import {MatCardModule} from "@angular/material/card";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
    selector: 'app-users',
    templateUrl: './users.component.html',
    styleUrls: ['./users.component.scss'],
    standalone: true,
    imports: [MatTableModule, MatToolbarModule, MatButtonModule, MatIconModule, MatTooltipModule, RouterLink, MatPaginatorModule, MatFormFieldModule, MatInputModule, MatDialogModule, MatCardModule]
})
export class UsersComponent implements OnInit, AfterViewInit {

    private adminService = inject(AdminService);
    private authenticationService = inject(AuthenticationService);
    private dialog = inject(MatDialog);
    private snackBar = inject(MatSnackBar);

    dataSource: MatTableDataSource<UserDTO> = new MatTableDataSource<UserDTO>();
    displayedColumns: string[] = ['name', 'email', 'role', 'admin'];

    @ViewChild(MatPaginator) paginator!: MatPaginator;

    ngOnInit(): void {
        this.loadUsers();
    }

    private loadUsers() {
        this.adminService.getAllUsers().subscribe(users => {
            this.dataSource.data = users;
        });
    }

    ngAfterViewInit() {
        this.dataSource.paginator = this.paginator;
    }

    applyFilter(event: Event) {
        const filterValue = (event.target as HTMLInputElement).value;
        this.dataSource.filter = filterValue.trim().toLowerCase();
    }

    isAdmin(): boolean {
        return this.authenticationService.isAdmin();
    }

    openCreateUserDialog(): void {
        const dialogRef = this.dialog.open(CreateUserDialogComponent);

        dialogRef.afterClosed().subscribe((result: CreateUserDTO | undefined) => {
            if (result) {
                this.adminService.createUser(result).subscribe({
                    next: () => {
                        this.loadUsers();
                    },
                    error: () => {
                        this.snackBar.open("Could not create user...", undefined, {
                            duration: 3000,
                            horizontalPosition: "center",
                            verticalPosition: "top",
                        })
                    }
                });
            }
        });
    }
}
