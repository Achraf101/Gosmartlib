import { Component, signal } from '@angular/core';
import { NavBar } from './nav-bar/nav-bar';
import { ButtonModule } from 'primeng/button';


@Component({
  selector: 'app-root',
  imports: [ButtonModule, NavBar],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
}
