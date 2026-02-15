import { Component } from '@angular/core';
import { PersonService } from '../services/PersonService';
import { Person } from '../models/interfaces';
import { ButtonModule } from 'primeng/button';

@Component({
    selector: 'app-hello',
    templateUrl: './hello.html',
    styleUrl: './hello.css',
    imports: [ButtonModule]
})
export class hello {
    
    users: Person[] = [];


    constructor(private PersonService: PersonService) {}


    ngOnInit() {
        this.PersonService.getUsers().subscribe(data => {
        this.users = data;
        });
    }

    showMessage(){
        console.log(this.users);
        alert(
        this.users
            .map(user => user.name + ": " + user.tekst + ".")
            .join("\n"))
    }
}