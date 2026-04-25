import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { LoanService } from '../../services/loan';
import { Toast } from 'primeng/toast';
import { Card } from 'primeng/card';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { BadgeModule } from 'primeng/badge';
import { LoanDTO, LoanStatus } from '../../models/loan';

@Component({
  selector: 'app-accept-decline-reservations-page',
  imports: [
    NavBarComponent,
    Toast,
    Card,
    Button,
    Dialog,
    FormsModule,
    CommonModule,
    TableModule,
    BadgeModule,
  ],
  templateUrl: './accept-decline-reservations-page.html',
  styleUrl: './accept-decline-reservations-page.css',
})
export class AcceptDeclineReservationsPageComponent implements OnInit {
  loans: LoanDTO[] = [];
  loading = false;
  selectedLoan: LoanDTO | null = null;
  noteDialogVisible = false;
  note = '';
  infoDialogVisible = false;

  constructor(
    private loanService: LoanService,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.loadPending();
  }

  loadPending(): void {
    this.loading = true;
    this.loanService.getRequested().subscribe({
      next: (data) => {
        this.loans = data;
        this.loading = false;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Ontleenverzoeken konden niet worden geladen.',
        });
        this.loading = false;
      },
    });
  }

  openNoteDialog(loan: LoanDTO): void {
    this.selectedLoan = loan;
    this.note = loan.note;
    this.noteDialogVisible = true;
  }

  addOrChangeNote(): void {
    if (!this.selectedLoan) return;
    this.loanService.updateNote(this.selectedLoan.id, this.note).subscribe({
      next: (updatedLoan) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Notitie opgeslagen.',
          life: 3000,
        });
        this.selectedLoan!.note = updatedLoan.note;
        this.noteDialogVisible = false;
        this.loading = false;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Aanpassen van de notitie is mislukt.',
        });
        this.loading = false;
      },
    });
  }

  acceptLoan(loan: LoanDTO) {
    if (!loan) return;
    this.loanService.changeStatus(loan.id, LoanStatus.ACCEPTED).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Uitlening geaccepteerd!',
          life: 3000,
        });
        this.loading = false;
        this.loadPending();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Accepteren van deze uitlening is mislukt, probeer opnieuw.',
          life: 3000,
        });
        this.loading = false;
      },
    });
  }

  declineLoan(loan: LoanDTO) {
    if (!loan) return;
    this.loanService.changeStatus(loan.id, LoanStatus.DECLINED).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Uitlening geweigerd!',
          life: 3000,
        });
        this.loadPending();
        this.loading = false;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Weigeren van deze uitlening is mislukt, probeer opnieuw.',
          life: 3000,
        });
        this.loading = false;
      },
    });
  }
}
