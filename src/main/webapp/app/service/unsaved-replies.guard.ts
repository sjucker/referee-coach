import {Injectable} from '@angular/core';
import {UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {DiscussVideoReportComponent} from "../discuss-report/discuss-video-report.component";

@Injectable({
    providedIn: 'root'
})
export class UnsavedRepliesGuard {

    canDeactivate(component: DiscussVideoReportComponent): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
        return component.canDeactivate();
    }

}
