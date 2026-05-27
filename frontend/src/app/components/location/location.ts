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
import { LocationService } from '../../services/location';
import { Location } from '../../models/location';
import { SchoolService } from '../../services/school';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-location',
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
  templateUrl: './location.html',
  styleUrl: './location.css',
})
export class LocationComponent {
  constructor(
    private route: ActivatedRoute,
    private locationService: LocationService,
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
    });
  }

  form = new FormGroup({
    name: new FormControl('', [Validators.required, Validators.maxLength(255)]),
    adres: new FormControl('', [Validators.maxLength(500)]),
  });

  loading = false;

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    const rawValue = this.form.value;

    const location: Omit<Location, 'id'> = {
      name: rawValue.name?.trim() ?? '',
      adres: rawValue.adres?.trim() ?? '',
      schoolId: Number(this.schoolId),
    };

    this.locationService.createLocation(location).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Locatie succesvol opgeslagen!',
          life: 3000,
        });

        this.form.reset();
      },
      complete: () => (this.loading = false),
    });
  }
}
