import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from "../service/authentication.service";
import {ActivatedRoute, Router} from "@angular/router";
import {MatSnackBar} from "@angular/material/snack-bar";
import {LOGIN_PATH} from "../app-routing.module";

@Component({
    selector: 'app-reset-password',
    templateUrl: './reset-password.component.html',
    styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {
    loginUrl = '';
    email?: string | null;
    token?: string | null;

    password = '';

    processing = false;
    success = false;

    constructor(private authenticationService: AuthenticationService,
                private router: Router,
                private route: ActivatedRoute,
                private snackBar: MatSnackBar) {
    }

    ngOnInit(): void {
        this.email = this.route.snapshot.paramMap.get('email');
        this.token = this.route.snapshot.paramMap.get('token');

        if (!this.email || !this.token) {
            this.router.navigate(['/']).then();
        }
    }

    resetPassword() {
        if (this.email && this.token && this.password) {
            this.processing = true;
            this.authenticationService.resetPassword(this.email, this.token, this.password).subscribe({
                next: () => {
                    this.processing = false;
                    this.success = true;
                    this.loginUrl = `/${LOGIN_PATH}/${this.email}`;
                },
                error: () => {
                    this.processing = false;
                    this.snackBar.open("An error has occurred.", undefined, {
                        verticalPosition: "top",
                        panelClass: "error"
                    });
                }
            });
        }
    }

}
