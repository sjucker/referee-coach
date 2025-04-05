import {inject, NgModule} from '@angular/core';
import {CanActivateFn, CanDeactivateFn, RouterModule, Routes} from '@angular/router';
import {AuthenticationGuard} from "./service/authentication.guard";
import {UnsavedChangesGuard} from "./service/unsaved-changes.guard";
import {UnsavedRepliesGuard} from "./service/unsaved-replies.guard";
import {HasUnsavedReplies} from "./has-unsaved-replies";
import {VideoReportComponent} from "./video-report/video-report.component";

export const LOGIN_PATH = 'login'
export const FORGOT_PASSWORD_PATH = 'forgot-password';
export const RESET_PASSWORD_PATH = 'reset-passwort';
export const EDIT_PATH = 'edit'
export const VIEW_PATH = 'view'
export const DISCUSS_PATH = 'discuss'
export const SETTINGS_PATH = 'settings'
export const SEARCH_PATH = 'search'
export const ADMIN_PATH = 'admin'
export const GAME_DISCUSSION_PATH = 'game-discussion'

const authenticationGuard: CanActivateFn = () => inject(AuthenticationGuard).isLoggedIn();
const hasAdminRightsGuard: CanActivateFn = () => inject(AuthenticationGuard).isAdmin();
const unsavedRepliesGuard: CanDeactivateFn<HasUnsavedReplies> = (component) => inject(UnsavedRepliesGuard).canDeactivate(component);
const unsavedChangesGuard: CanDeactivateFn<VideoReportComponent> = (component) => inject(UnsavedChangesGuard).canDeactivate(component);

const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('./main/main.component').then(m => m.MainComponent),
        canActivate: [authenticationGuard]
    },
    {
        path: VIEW_PATH + '/:id',
        loadComponent: () => import('./view-report/view-video-report.component').then(m => m.ViewVideoReportComponent)
        // allowed without being logged in, anonymous user only needs to know the report's ID (which should be hard to guess)
    },
    {
        path: DISCUSS_PATH + '/:id',
        loadComponent: () => import('./discuss-report/discuss-video-report.component').then(m => m.DiscussVideoReportComponent),
        // allowed without being logged in, anonymous user only needs to know the report's ID (which should be hard to guess)
        canDeactivate: [unsavedRepliesGuard]
    },
    {
        path: EDIT_PATH + '/:id',
        loadComponent: () => import('./video-report/video-report.component').then(m => m.VideoReportComponent),
        canActivate: [authenticationGuard],
        canDeactivate: [unsavedChangesGuard]
    },
    {
        path: GAME_DISCUSSION_PATH + '/:id',
        loadComponent: () => import('./game-discussion/game-discussion.component').then(m => m.GameDiscussionComponent),
        canActivate: [authenticationGuard],
        canDeactivate: [unsavedRepliesGuard]
    },
    {
        path: LOGIN_PATH,
        loadComponent: () => import('./login/login.component').then(m => m.LoginComponent)
    },
    {
        path: LOGIN_PATH + '/:email',
        loadComponent: () => import('./login/login.component').then(m => m.LoginComponent)
    },
    {
        path: SETTINGS_PATH,
        loadComponent: () => import('./settings/settings.component').then(m => m.SettingsComponent),
        canActivate: [authenticationGuard]
    },
    {
        path: SEARCH_PATH,
        loadComponent: () => import('./report-search/report-search.component').then(m => m.ReportSearchComponent),
        canActivate: [authenticationGuard]
    },
    {
        path: ADMIN_PATH,
        loadComponent: () => import('./admin/admin.component').then(m => m.AdminComponent),
        canActivate: [hasAdminRightsGuard]
    },
    {
        path: FORGOT_PASSWORD_PATH,
        loadComponent: () => import('./forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent)
    },
    {
        path: RESET_PASSWORD_PATH + '/:email/:token',
        loadComponent: () => import('./reset-password/reset-password.component').then(m => m.ResetPasswordComponent)
    },
];

@NgModule({
    imports: [RouterModule.forRoot(routes, {useHash: true})],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
