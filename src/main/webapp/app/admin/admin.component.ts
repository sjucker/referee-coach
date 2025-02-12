import {Component, OnInit} from '@angular/core';
import {AdminService} from "../service/admin.service";
import {UserDTO} from "../rest";

@Component({
    selector: 'app-admin',
    templateUrl: './admin.component.html',
    styleUrls: ['./admin.component.scss'],
    standalone: false
})
export class AdminComponent implements OnInit {

    referees: UserDTO[] = [];

    constructor(private adminService: AdminService) {
    }

    ngOnInit(): void {
        this.adminService.getAllReferees().subscribe(value => {
            this.referees = value;
        });
    }

}
