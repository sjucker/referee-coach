import {Component, OnInit} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {AuthenticationService} from "../service/authentication.service";
import {Router} from "@angular/router";

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

    authenticationError = false;

    loginForm = this.formBuilder.group({
        email: [null, [Validators.required]],
        password: [null, [Validators.required]],
        rememberMe: [false],
    });

    constructor(private formBuilder: FormBuilder,
                private router: Router,
                private authenticationService: AuthenticationService) {
    }

    ngOnInit(): void {
    }

    login(): void {
        if (this.loginForm.valid) {
            this.authenticationError = false;
            const val = this.loginForm.value;
            this.authenticationService.login(val.email, val.password).subscribe(response => {
                    this.authenticationService.setAuthorizationToken(response.jwt);
                    this.router.navigate(['/']);
                },
                error => {
                    this.authenticationError = true;
                })
        }
    }

}