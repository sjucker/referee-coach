import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {version} from '../../../../package.json'

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    imports: [RouterOutlet]
})
export class AppComponent {

    version = ""

    constructor() {
        this.version = version
    }
}
