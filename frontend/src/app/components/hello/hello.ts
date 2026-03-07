import { Component, inject } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { HelloService } from '../../services/hello';
import { Hello } from '../../models/hello';

@Component({
  selector: 'app-hello',
  templateUrl: './hello.html',
  styleUrl: './hello.css',
  imports: [ButtonModule],
})
export class HelloComponent {

  hellos: Hello[] = [];

  constructor(private helloService: HelloService) {}

  ngOnInit(): void {
    this.helloService.getAll().subscribe({
      next: hellos => this.hellos = hellos,
      error: err => console.log(err)
    });
  }
}

export type { Hello };
