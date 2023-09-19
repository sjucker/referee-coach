import {Injectable} from '@angular/core';
import {UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {HasUnsavedReplies} from "../has-unsaved-replies";

@Injectable({
    providedIn: 'root'
})
export class UnsavedRepliesGuard {

    canDeactivate(component: HasUnsavedReplies): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
        return component.canDeactivate();
    }

}
