import {Observable} from "rxjs";

export interface HasUnsavedReplies {
    canDeactivate(): Observable<boolean>;
}
