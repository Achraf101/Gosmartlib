import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { SchoolService } from '../../services/school';
import { School } from '../../models/school';
import { InputTextModule } from 'primeng/inputtext';
import { TooltipModule } from 'primeng/tooltip';
import { IftaLabel } from 'primeng/iftalabel';
import { Textarea } from 'primeng/textarea';
import { Message } from 'primeng/message';
import { Button } from 'primeng/button';
import { CharCounterComponent } from '../char-counter/char-counter';
import { Router } from '@angular/router';

@Component({
  selector: 'app-school',
  imports: [
    ReactiveFormsModule,
    InputTextModule,
    TooltipModule,
    IftaLabel,
    Textarea,
    Message,
    Button,
    CharCounterComponent,
  ],
  templateUrl: './school-add.html',
  styleUrl: './school-add.css',
})
export class SchoolAddComponent {
  constructor(
    private schoolService: SchoolService,
    private messageService: MessageService,
    private router: Router,
  ) {}

  amount: number = 1;

  form = new FormGroup({
    name: new FormControl('', [Validators.required, Validators.maxLength(255)]),
    adres: new FormControl('', [Validators.maxLength(500)]),
    contact: new FormControl('', [Validators.maxLength(500)]),
    description: new FormControl('', [Validators.maxLength(1000)]),
    subdomain: new FormControl('', [Validators.required, Validators.maxLength(255)]),
  });

  loading = false;

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    const rawValue = this.form.value as School;

    const school: Omit<School, 'id'> = {
      name: rawValue.name?.trim() ?? '',
      adres: rawValue.adres?.trim() ?? '',
      contact: rawValue.contact?.trim() ?? '',
      description: rawValue.description?.trim() ?? '',
      subdomain: rawValue.subdomain?.trim() ?? '',
    };

    this.schoolService.addSchool(school).subscribe({
      next: (school) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'School succesvol opgeslagen!',
          life: 3000,
        });
        this.router.navigate(['/campus/toevoegen', school.id]);
        this.form.reset();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Fout bij opslaan van de school.',
          life: 3003000,
        });

        console.error(err);
      },
      complete: () => (this.loading = false),
    });
  }
}
