import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanDeactivate, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {VideoReportComponent} from "../video-report/video-report.component";

@Injectable({
    providedIn: 'root'
})
export class UnsavedChangesGuard implements CanDeactivate<VideoReportComponent> {

    canDeactivate(component: VideoReportComponent,
                  currentRoute: ActivatedRouteSnapshot,
                  currentState: RouterStateSnapshot,
                  nextState?: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
        return component.canDeactivate();
    }

}
