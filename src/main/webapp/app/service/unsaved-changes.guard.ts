import {Injectable} from '@angular/core';
import {UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {VideoReportComponent} from "../video-report/video-report.component";

@Injectable({
    providedIn: 'root'
})
export class UnsavedChangesGuard {

    canDeactivate(component: VideoReportComponent): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
        return component.canDeactivate();
    }

}
