import { Component, signal } from '@angular/core';
import { PrimeNgNavBar } from './components/primeng-navbar/primeng-navbar';
import { ButtonModule } from 'primeng/button';
import { Toast } from 'primeng/toast';
import { RouterOutlet } from '@angular/router';
@Component({
  selector: 'app-root',
  imports: [ButtonModule, PrimeNgNavBar, Toast, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly title = signal('frontend');
}
