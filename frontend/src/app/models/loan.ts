import { CreateLoanBookDTO, LoanBookDTO } from './loanBook';

export enum LoanStatus {
  REQUESTED = 'REQUESTED',
  RECEIVED = 'RECEIVED',
  RETURNED = 'RETURNED',
  ACCEPTED = 'ACCEPTED',
  DECLINED = 'DECLINED',
}

export interface CreateLoanDTO {
  userId: number;
  locationId: number;
  extended: number;
  start: string;
  end: string;
  note: string;
  status: LoanStatus;
  closed: boolean;
  books: CreateLoanBookDTO[];
}

export interface LoanDTO {
  id: number;
  username: String;
  locationId: number;
  extended: number;
  start: string;
  end: string;
  note: string;
  status: LoanStatus;
  closed: boolean;
  created: string;
  books: LoanBookDTO[];
  groupId?: string;
  extendPeriod: number;
}
