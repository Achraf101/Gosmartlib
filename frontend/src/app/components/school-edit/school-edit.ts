import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { SchoolService } from '../../services/school';
import { AuthService } from '../../services/auth';
import { School } from '../../models/school';
import { InputTextModule } from 'primeng/inputtext';
import { TooltipModule } from 'primeng/tooltip';
import { IftaLabel } from 'primeng/iftalabel';
import { Textarea } from 'primeng/textarea';
import { Message } from 'primeng/message';
import { Button } from 'primeng/button';
import { CharCounterComponent } from '../char-counter/char-counter';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-school-edit',
  imports: [
    ReactiveFormsModule,
    InputTextModule,
    TooltipModule,
    IftaLabel,
    Textarea,
    Message,
    Button,
    CharCounterComponent,
    NavBarComponent,
    ToastModule,
  ],
  providers: [MessageService],
  templateUrl: './school-edit.html',
  styleUrl: './school-edit.css',
})
export class SchoolEditComponent implements OnInit {
  schoolId!: number;
  loading = false;
  loadingData = true;

  form = new FormGroup({
    name: new FormControl('', [Validators.required, Validators.maxLength(255)]),
    adres: new FormControl('', [Validators.maxLength(500)]),
    contact: new FormControl('', [Validators.maxLength(500)]),
    description: new FormControl('', [Validators.maxLength(1000)]),
    subdomain: new FormControl('', [Validators.required, Validators.maxLength(255)]),
    borrowLimit: new FormControl<number | null>(null, [
      Validators.required,
      Validators.min(1),
      Validators.max(999),
    ]),
    borrowPeriod: new FormControl<number | null>(null, [
      Validators.required,
      Validators.min(1),
      Validators.max(999),
    ]),
    extendPeriod: new FormControl<number | null>(null, [
      Validators.required,
      Validators.min(1),
      Validators.max(999),
    ]),
    extendLimit: new FormControl<number | null>(null, [
      Validators.required,
      Validators.min(1),
      Validators.max(10),
    ]),
  });

  constructor(
    private schoolService: SchoolService,
    private authService: AuthService,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.schoolId = this.authService.currentUser!.schoolId;

    this.schoolService.getById(this.schoolId).subscribe({
      next: (school) => {
        this.form.patchValue({
          name: school.name,
          adres: school.adres ?? '',
          contact: school.contact ?? '',
          description: school.description ?? '',
          subdomain: school.subdomain,
          borrowLimit: school.borrowLimit,
          borrowPeriod: school.borrowPeriod,
          extendPeriod: school.extendPeriod,
          extendLimit: school.extendLimit,
        });
        this.loadingData = false;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Kon schoolgegevens niet laden.',
          life: 3000,
        });
        this.loadingData = false;
      },
    });
  }

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
      borrowLimit: Number(rawValue.borrowLimit),
      borrowPeriod: Number(rawValue.borrowPeriod),
      extendLimit: Number(rawValue.extendLimit),
      extendPeriod: Number(rawValue.extendPeriod),
    };

    this.schoolService.updateSchool(this.schoolId, school).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Schoolgegevens succesvol bijgewerkt!',
          life: 3000,
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Fout bij opslaan van de schoolgegevens.',
          life: 3000,
        });
      },
      complete: () => (this.loading = false),
    });
  }
}