import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { DividerModule } from 'primeng/divider';
import { AccordionModule } from 'primeng/accordion';
import { AuthService } from '../../services/auth';
import { Login } from '../../models/login';

@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [
    FormsModule,
    CardModule,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    MessageModule,
    DividerModule,
    AccordionModule,
  ],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {
  username = '';
  password = '';
  errorMessage = '';
  loading = false;
  smartschoolUrl =
    'https://oauth.smartschool.be/OAuth?client_id=2ebf496d131b&redirect_uri=https%3A%2F%2Fgosmartlib.tech%2Fapi%2Foauth&response_type=code&scope=userinfo groupinfo sendmessage sendnotif';

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  onSubmit(): void {
    if (!this.username.trim() || !this.password) {
      this.errorMessage = 'Vul gebruikersnaam en wachtwoord in.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/']);
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Ongeldige gebruikersnaam of wachtwoord.';
      },
    });
  }

  loginSmartschool(): void {}
}
