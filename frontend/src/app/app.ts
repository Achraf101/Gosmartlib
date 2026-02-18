import { Component, signal } from '@angular/core';
import { NavBar } from './components/nav-bar/nav-bar';
import { ButtonModule } from 'primeng/button';
import { Toast } from "primeng/toast";


@Component({
  selector: 'app-root',
  imports: [ButtonModule, NavBar, Toast],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
}
