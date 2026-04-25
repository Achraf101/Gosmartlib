export interface CreateLoanBookDTO {
  bookId: number;
  requestedAmount: number;
  receivedAmount: number;
  returnedAmount: number;
}

export interface LoanBookDTO {
  bookId: number;
  bookTitle: string;
  requestedAmount: number;
  receivedAmount: number;
  returnedAmount: number;
}
