export interface Campus {
  id: number;
  name: string;
  adres?: string;
  borrowLimit: number;
  borrowPeriod: number;
  extendPeriod: number;
  extendLimit: number;
  schoolId: number;
}
