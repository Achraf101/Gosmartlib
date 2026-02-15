import { Component, signal } from '@angular/core';
import { NavBar } from './nav-bar/nav-bar';
import { hello } from './hello/hello';
import { ButtonModule } from 'primeng/button';


@Component({
  selector: 'app-root',
  imports: [ButtonModule, NavBar, hello],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
}
