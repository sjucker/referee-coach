import {enableProdMode, importProvidersFrom} from '@angular/core';


import {environment} from './environments/environment';
import {HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {AuthenticationInterceptor} from './app/interceptors/authentication-interceptor.service';
import {MAT_DATE_FORMATS} from '@angular/material/core';
import {bootstrapApplication, BrowserModule} from '@angular/platform-browser';
import {AppRoutingModule} from './app/app-routing.module';
import {provideAnimations} from '@angular/platform-browser/animations';
import {YouTubePlayerModule} from '@angular/youtube-player';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatInputModule} from '@angular/material/input';
import {MatCardModule} from '@angular/material/card';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatIconModule} from '@angular/material/icon';
import {MatRadioModule} from '@angular/material/radio';
import {MatTableModule} from '@angular/material/table';
import {MatSelectModule} from '@angular/material/select';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatDialogModule} from '@angular/material/dialog';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatSliderModule} from '@angular/material/slider';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatLuxonDateModule} from '@angular/material-luxon-adapter';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatChipsModule} from '@angular/material/chips';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {AppComponent} from './app/app.component';

if (environment.production) {
    enableProdMode();
}

bootstrapApplication(AppComponent, {
    providers: [
        importProvidersFrom(BrowserModule, AppRoutingModule, YouTubePlayerModule, MatButtonModule, MatFormFieldModule, MatToolbarModule, MatInputModule, MatCardModule, FormsModule, MatGridListModule, MatIconModule, MatRadioModule, MatTableModule, MatSelectModule, MatSnackBarModule, ReactiveFormsModule, MatSidenavModule, MatDialogModule, MatProgressSpinnerModule, MatPaginatorModule, MatProgressBarModule, MatTooltipModule, MatSliderModule, MatDatepickerModule, MatLuxonDateModule, MatCheckboxModule, MatChipsModule, MatAutocompleteModule),
        {provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true},
        {
            provide: MAT_DATE_FORMATS, useValue: {
                parse: {
                    dateInput: 'dd.MM.yyyy',
                },
                display: {
                    dateInput: 'dd.MM.yyyy',
                    monthYearLabel: 'MMM yyyy',
                    dateA11yLabel: 'dd.MM.yyyy',
                    monthYearA11yLabel: 'MMMM yyyy',
                },
            }
        },
        provideHttpClient(withInterceptorsFromDi()),
        provideAnimations()
    ]
}).catch(err => console.error(err));
