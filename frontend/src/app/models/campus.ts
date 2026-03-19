export interface Campus {
  id: number;
  name: string;
  adres?: string;
  borrowLimit: number;
  borrow_period: number;
  extend_period: number;
  extend_limit: number;
  schoolId: number;
}
