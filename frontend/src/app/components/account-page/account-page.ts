import { Component } from '@angular/core';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';

import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { AccountService } from '../../services/account';
import { Password } from '../../models/account';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-account-page',
  imports: [
    NavBarComponent,
    PasswordModule,
    FormsModule,
    ReactiveFormsModule,
    ButtonModule,
    MessageModule,
  ],
  templateUrl: './account-page.html',
  styleUrl: './account-page.css',
})
export class AccountPage {
  error_msg = '';
  passwordForm = new FormGroup({
    current_password: new FormControl<string>('', [Validators.required]),
    new_password: new FormControl<string>('', [
      Validators.required,
      Validators.minLength(8),
      Validators.maxLength(50),
    ]),
    verify_password: new FormControl<string>('', [
      Validators.required,
      Validators.minLength(8),
      Validators.maxLength(50),
    ]),
  });
  new_state = false;
  verify_state = false;

  constructor(
    private accountService: AccountService,
    private messageService: MessageService,
  ) {}

  changePassword(): void {
    if (this.passwordForm.valid) {
      if (this.passwordForm.value.new_password !== this.passwordForm.value.verify_password) {
        // return error
        this.error_msg = 'Nieuwe wachtwoorden komen niet overeen';
        this.new_state = true;
        this.verify_state = true;
        return;
      }
      const { verify_password, ...payload } = this.passwordForm.value;
      // save password
      this.accountService.updatePassword(payload as Password).subscribe({
        next: (a) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Wachtwoord opgeslagen',
            life: 3000,
          });
          this.passwordForm.reset();
        },
        error: (e) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Probleem met wachtwoord op te slagen',
            life: 3000,
          });
        },
      });
    }
  }
}
