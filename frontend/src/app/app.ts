import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { hello } from './hello/hello';
import { ButtonModule } from 'primeng/button';


@Component({
  selector: 'app-root',
  imports: [RouterOutlet, hello, ButtonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
}
