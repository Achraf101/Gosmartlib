export interface CreateLoanBookDTO {
  bookId: number;
  requestedAmount: number;
  receivedAmount: number;
  returnedAmount: number;
}

export interface LoanBookDTO {
  id: number;
  bookId: number;
  bookTitle: string;
  requestedAmount: number;
  receivedAmount: number;
  returnedAmount: number;
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED';
}
