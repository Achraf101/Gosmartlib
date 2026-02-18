import { Component } from '@angular/core';
import { ButtonModule } from 'primeng/button';

@Component({
    selector: 'app-hello',
    templateUrl: './hello.html',
    styleUrl: './hello.css',
    imports: [ButtonModule]
})
export class hello {

    constructor() {}

}