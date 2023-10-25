import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {YouTubePlayerModule} from "@angular/youtube-player";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatCardModule} from "@angular/material/card";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {MatGridListModule} from "@angular/material/grid-list";
import {MatIconModule} from "@angular/material/icon";
import {VideoReportComponent} from './video-report/video-report.component';
import {ViewVideoReportComponent} from './view-report/view-video-report.component';
import {MatRadioModule} from "@angular/material/radio";
import {MainComponent} from "./main/main.component";
import {MatTableModule} from "@angular/material/table";
import {MatSelectModule} from "@angular/material/select";
import {LoginComponent} from './login/login.component';
import {AuthenticationInterceptor} from "./interceptors/authentication-interceptor.service";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {SettingsComponent} from './settings/settings.component';
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatDialogModule} from "@angular/material/dialog";
import {VideoReportFinishDialogComponent} from "./video-report-finish-dialog/video-report-finish-dialog.component";
import {VideoReportCopyDialogComponent} from './video-report-copy-dialog/video-report-copy-dialog.component';
import {VideoReportDeleteDialogComponent} from './video-report-delete-dialog/video-report-delete-dialog.component';
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {VideoReportUnsavedChangesDialogComponent} from './video-report-unsaved-changes-dialog/video-report-unsaved-changes-dialog.component';
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {CommentReplyDialogComponent} from './comment-reply-dialog/comment-reply-dialog.component';
import {MatTooltipModule} from "@angular/material/tooltip";
import {DiscussVideoReportComponent} from './discuss-report/discuss-video-report.component';
import {UnsavedRepliesDialogComponent} from './discuss-video-report-unsaved-replies-dialog/unsaved-replies-dialog.component';
import {DiscussVideoReportFinishDialogComponent} from './discuss-video-report-finish-dialog/discuss-video-report-finish-dialog.component';
import {MatSliderModule} from "@angular/material/slider";
import {VideoReportRatingComponent} from './video-report-rating/video-report-rating.component';
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatLuxonDateModule} from "@angular/material-luxon-adapter";
import {MAT_DATE_FORMATS} from "@angular/material/core";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatChipsModule} from "@angular/material/chips";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {ReportSearchComponent} from './report-search/report-search.component';
import {TagsSelectionComponent} from './tags-selection/tags-selection.component';
import {AdminComponent} from './admin/admin.component';
import {GameDiscussionComponent} from './game-discussion/game-discussion.component';
import {GameDiscussionFinishCommentsDialogComponent} from './game-discussion-finish-comments-dialog/game-discussion-finish-comments-dialog.component';
import {ForgotPasswordComponent} from './forgot-password/forgot-password.component';
import {ResetPasswordComponent} from './reset-password/reset-password.component';

@NgModule({
    declarations: [
        AppComponent,
        MainComponent,
        VideoReportComponent,
        VideoReportFinishDialogComponent,
        ViewVideoReportComponent,
        LoginComponent,
        SettingsComponent,
        VideoReportCopyDialogComponent,
        VideoReportDeleteDialogComponent,
        VideoReportUnsavedChangesDialogComponent,
        CommentReplyDialogComponent,
        DiscussVideoReportComponent,
        UnsavedRepliesDialogComponent,
        DiscussVideoReportFinishDialogComponent,
        VideoReportRatingComponent,
        ReportSearchComponent,
        TagsSelectionComponent,
        AdminComponent,
        GameDiscussionComponent,
        GameDiscussionFinishCommentsDialogComponent,
        ForgotPasswordComponent,
        ResetPasswordComponent,
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        BrowserAnimationsModule,
        YouTubePlayerModule,
        MatButtonModule,
        MatFormFieldModule,
        MatToolbarModule,
        MatInputModule,
        MatCardModule,
        FormsModule,
        HttpClientModule,
        MatGridListModule,
        MatIconModule,
        MatRadioModule,
        MatTableModule,
        MatSelectModule,
        MatSnackBarModule,
        ReactiveFormsModule,
        MatSidenavModule,
        MatDialogModule,
        MatProgressSpinnerModule,
        MatPaginatorModule,
        MatProgressBarModule,
        MatTooltipModule,
        MatSliderModule,
        MatDatepickerModule,
        MatLuxonDateModule,
        MatCheckboxModule,
        MatChipsModule,
        MatAutocompleteModule
    ],
    providers: [
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
        }],
    bootstrap: [AppComponent]
})
export class AppModule {
}
