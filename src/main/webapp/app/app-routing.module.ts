import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {VideoReportComponent} from "./video-report/video-report.component";
import {ViewVideoReportComponent} from "./view-report/view-video-report.component";
import {MainComponent} from "./main/main.component";
import {LoginComponent} from "./login/login.component";
import {AuthenticationGuard} from "./service/authentication.guard";
import {SettingsComponent} from "./settings/settings.component";
import {UnsavedChangesGuard} from "./service/unsaved-changes.guard";

const routes: Routes = [
    {
        path: '',
        component: MainComponent,
        canActivate: [AuthenticationGuard]
    },
    {
        path: 'view/:id',
        component: ViewVideoReportComponent
        // allowed without being logged in, anonymous user only needs to know the report's ID (which should be hard to guess)
    },
    {
        path: 'edit/:id',
        component: VideoReportComponent,
        canActivate: [AuthenticationGuard],
        canDeactivate: [UnsavedChangesGuard]
    },
    {
        path: 'login',
        component: LoginComponent
    },
    {
        path: 'settings',
        component: SettingsComponent,
        canActivate: [AuthenticationGuard]
    }
];

@NgModule({
    imports: [RouterModule.forRoot(routes, {useHash: true})],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
