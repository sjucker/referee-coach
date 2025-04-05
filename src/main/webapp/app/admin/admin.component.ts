import {Component, inject, OnInit} from '@angular/core';
import {AdminService} from "../service/admin.service";
import {UserDTO} from "../rest";

@Component({
    selector: 'app-admin',
    templateUrl: './admin.component.html',
    styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {
    private adminService = inject(AdminService);

    referees: UserDTO[] = [];

    ngOnInit(): void {
        this.adminService.getAllReferees().subscribe(value => {
            this.referees = value;
        });
    }

}
