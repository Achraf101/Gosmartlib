import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { IftaLabel } from 'primeng/iftalabel';
import { InputTextModule } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { Textarea } from 'primeng/textarea';
import { TooltipModule } from 'primeng/tooltip';
import { CharCounterComponent } from '../char-counter/char-counter';
import { SelectModule } from 'primeng/select';
import { School } from '../../models/school';
import { CampusService } from '../../services/campus';
import { Campus } from '../../models/campus';
import { SchoolService } from '../../services/school';

@Component({
  selector: 'app-campus',
  imports: [
    ReactiveFormsModule,
    InputTextModule,
    TooltipModule,
    IftaLabel,
    Textarea,
    Message,
    Button,
    CharCounterComponent,
    SelectModule,
  ],
  templateUrl: './campus.html',
  styleUrl: './campus.css',
})
export class CampusComponent {
  constructor(
    private campusService: CampusService,
    private schoolService: SchoolService,
    private messageService: MessageService,
  ) {}

  schools: School[] = [];

  ngOnInit(): void {
    this.schoolService.getAll().subscribe({
      next: (schools) => (this.schools = schools),
      error: (err) => console.log(err),
    });
  }

  form = new FormGroup({
    name: new FormControl('', [Validators.required, Validators.maxLength(255)]),
    adres: new FormControl('', [Validators.maxLength(500)]),
    borrowLimit: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
    schoolId: new FormControl<number | null>(null, [Validators.required]),
  });

  loading = false;

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    const rawValue = this.form.value;

    const campus: Omit<Campus, 'id'> = {
      name: rawValue.name?.trim() ?? '',
      adres: rawValue.adres?.trim() ?? '',
      // Only convert if there is a value; otherwise, keep it as null or undefined
      borrowLimit: Number(rawValue.borrowLimit),
      schoolId: Number(rawValue.schoolId),
    };
    console.log('Sending to API:', campus);

    this.campusService.createCampus(campus).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Campus succesvol opgeslagen!',
          life: 3000,
        });

        this.form.reset();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Fout bij opslaan van de campus.',
          life: 3003000,
        });

        console.error(err);
      },
      complete: () => (this.loading = false),
    });
  }
}
