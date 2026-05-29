import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { LoanService } from '../../services/loan';
import { Card } from 'primeng/card';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { LoanDTO, LoanStatus } from '../../models/loan';
import { Message } from 'primeng/message';
import { LocationBookService } from '../../services/locationbook';

@Component({
  selector: 'app-accept-decline-reservations-page',
  imports: [NavBarComponent, Card, Button, Dialog, FormsModule, CommonModule, TableModule, Message],
  templateUrl: './accept-decline-reservations-page.html',
  styleUrl: './accept-decline-reservations-page.css',
})
export class AcceptDeclineReservationsPageComponent implements OnInit {
  loans: LoanDTO[] = [];
  groupedLoans: (LoanDTO | LoanDTO[])[] = [];
  expandedGroups: Set<string> = new Set();
  loading = false;
  selectedLoan: LoanDTO | null = null;
  selectedGroupLoans: LoanDTO[] = [];
  noteDialogVisible = false;
  note = '';
  infoDialogVisible = false;
  groupInfoDialogVisible = false;
  scanInput = '';

  constructor(
    private loanService: LoanService,
    private messageService: MessageService,
    private locationBookService: LocationBookService,
  ) {}

  ngOnInit(): void {
    this.loadPending();
  }

  loadPending(): void {
    this.loading = true;
    this.loanService.getRequested().subscribe({
      next: (data) => {
        this.loans = data;
        this.groupLoans(data);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  groupLoans(loans: LoanDTO[]): void {
    const groups = new Map<string, LoanDTO[]>();
    const result: (LoanDTO | LoanDTO[])[] = [];

    for (const loan of loans) {
      if (loan.groupId) {
        if (!groups.has(loan.groupId)) {
          groups.set(loan.groupId, []);
          result.push(groups.get(loan.groupId)!);
        }
        groups.get(loan.groupId)!.push(loan);
      } else {
        result.push(loan);
      }
    }

    this.groupedLoans = result;
  }

  isGroup(item: LoanDTO | LoanDTO[]): boolean {
    return Array.isArray(item);
  }

  asGroup(item: LoanDTO | LoanDTO[]): LoanDTO[] {
    return item as LoanDTO[];
  }

  asLoan(item: LoanDTO | LoanDTO[]): LoanDTO {
    return item as LoanDTO;
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
        this.selectedGroupLoans = this.selectedGroupLoans.filter(l => l.id !== loan.id);
        if (this.selectedGroupLoans.length === 0) {
          this.groupInfoDialogVisible = false;
        }
        this.loadPending();
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
        this.selectedGroupLoans = this.selectedGroupLoans.filter(l => l.id !== loan.id);
        if (this.selectedGroupLoans.length === 0) {
          this.groupInfoDialogVisible = false;
        }
        this.loadPending();
      },
    });
  }

  acceptGroup(group: LoanDTO[]): void {
    let completed = 0;
    for (const loan of group) {
      this.loanService.changeStatus(loan.id, LoanStatus.ACCEPTED).subscribe({
        next: () => {
          completed++;
          if (completed === group.length) {
            this.messageService.add({
              severity: 'success',
              summary: 'Succes',
              detail: 'Alle ontleningen geaccepteerd!',
              life: 3000,
            });
            this.groupInfoDialogVisible = false;
            this.loadPending();
          }
        },
      });
    }
  }

  declineGroup(group: LoanDTO[]): void {
    let completed = 0;
    for (const loan of group) {
      this.loanService.changeStatus(loan.id, LoanStatus.DECLINED).subscribe({
        next: () => {
          completed++;
          if (completed === group.length) {
            this.messageService.add({
              severity: 'success',
              summary: 'Succes',
              detail: 'Alle ontleningen geweigerd!',
              life: 3000,
            });
            this.groupInfoDialogVisible = false;
            this.loadPending();
          }
        },
      });
    }
  }

  openGroupDialog(group: LoanDTO[]): void {
    this.selectedGroupLoans = group;
    this.groupInfoDialogVisible = true;
  }

  getTotalBooks(group: LoanDTO[]): number {
    return group.reduce((sum, loan) => sum + (loan.books[0]?.requestedAmount ?? 0), 0);
  }

  scanAndFind(): void {
    const id = this.scanInput.trim().toUpperCase();
    this.scanInput = '';
    if (!id) return;

    this.locationBookService.getCopyByAccessionId(id).subscribe({
      next: (copy) => {
        const loan = this.loans.find((l) => l.books.some((b) => b.bookId === copy.book_id));
        if (!loan) {
          this.messageService.add({
            severity: 'warn',
            summary: 'Niet gevonden',
            detail: `Geen aanvraag gevonden voor exemplaar ${id}.`,
            life: 3000,
          });
          return;
        }
        if (loan.groupId) {
          const group = this.loans.filter((l) => l.groupId === loan.groupId);
          this.openGroupDialog(group);
        } else {
          this.selectedLoan = loan;
          this.infoDialogVisible = true;
        }
      },
      error: () => {
        this.messageService.add({
          severity: 'warn',
          summary: 'Niet gevonden',
          detail: `Exemplaar ${id} niet gevonden.`,
          life: 3000,
        });
      },
    });
  }
}
