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
import { ActivatedRoute, RouterLink } from '@angular/router';

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
    RouterLink,
  ],
  templateUrl: './campus.html',
  styleUrl: './campus.css',
})
export class CampusComponent {
  constructor(
    private route: ActivatedRoute,
    private campusService: CampusService,
    private schoolService: SchoolService,
    private messageService: MessageService,
  ) {
    this.schoolId = Number(this.route.snapshot.paramMap.get('schoolId'));
  }
  schoolId: number;
  school: School | null = null;

  ngOnInit() {
    this.schoolService.getById(this.schoolId).subscribe({
      next: (school) => (this.school = school),
      error: (err) => console.log(err),
    });
  }

  form = new FormGroup({
    name: new FormControl('', [Validators.required, Validators.maxLength(255)]),
    adres: new FormControl('', [Validators.maxLength(500)]),
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
      borrow_period: Number(rawValue.borrowPeriod),
      extend_limit: Number(rawValue.extendLimit),
      extend_period: Number(rawValue.extendPeriod),
      schoolId: Number(this.schoolId),
    };
    console.log('Sending to API:', campus);

    this.campusService.createCampus(campus).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Campus succesvol opgeslagen!',
          life: 3000,
        });

        this.form.reset();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Fout bij opslaan van de campus.',
          life: 3000,
        });

        console.error(err);
      },
      complete: () => (this.loading = false),
    });
  }
}
