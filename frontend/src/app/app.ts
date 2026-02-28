import { Component, signal } from '@angular/core';
import { NavBar } from './components/nav-bar/nav-bar';
import { ButtonModule } from 'primeng/button';
import { Toast } from "primeng/toast";
import { RouterOutlet } from "@angular/router";


@Component({
  selector: 'app-root',
  imports: [ButtonModule, NavBar, Toast, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
}
