import {AfterViewInit, Component, inject, OnInit, ViewChild} from '@angular/core';
import {AdminService} from "../service/admin.service";
import {UserDTO} from "../rest";
import {CommonModule} from "@angular/common";
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
import {MatCard, MatCardContent} from "@angular/material/card";

@Component({
    selector: 'app-users',
    templateUrl: './users.component.html',
    styleUrls: ['./users.component.scss'],
    standalone: true,
    imports: [CommonModule, MatTableModule, MatToolbarModule, MatButtonModule, MatIconModule, MatTooltipModule, RouterLink, MatPaginatorModule, MatFormFieldModule, MatInputModule, MatCard, MatCardContent]
})
export class UsersComponent implements OnInit, AfterViewInit {

    private adminService = inject(AdminService);
    private authenticationService = inject(AuthenticationService);

    dataSource: MatTableDataSource<UserDTO> = new MatTableDataSource<UserDTO>();
    displayedColumns: string[] = ['name', 'email', 'role', 'admin'];

    @ViewChild(MatPaginator) paginator!: MatPaginator;

    ngOnInit(): void {
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
}
