import {Component} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {AuthenticationService} from "../service/authentication.service";
import {Router} from "@angular/router";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent {

    authenticating = false;

    loginForm = this.formBuilder.group({
        email: [null, [Validators.required]],
        password: [null, [Validators.required]],
    });

    constructor(private formBuilder: FormBuilder,
                private router: Router,
                private authenticationService: AuthenticationService,
                private snackBar: MatSnackBar) {
    }

    login(): void {
        if (this.loginForm.valid) {
            this.authenticating = true;
            const val = this.loginForm.value;
            this.authenticationService.login(val.email!, val.password!).subscribe({
                next: response => {
                    this.authenticationService.setCredentials(response);
                    this.router.navigate(['/']);
                },
                error: () => {
                    this.snackBar.open('Email/Password is not correct!', undefined, {
                        duration: 3000,
                        horizontalPosition: "center",
                        verticalPosition: "top"
                    })
                    this.authenticating = false;
                }
            })
        }
    }

}
