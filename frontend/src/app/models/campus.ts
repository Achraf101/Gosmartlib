import { School } from './school';

export interface Campus {
  id: number;
  name: string;
  adres?: string;
  borrowLimit: number;
  schoolId: number;
}
