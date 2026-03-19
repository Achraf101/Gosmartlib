import { Campus } from './campus';

export interface School {
  id: number;
  name: string;
  adres?: string;
  contact?: string;
  description?: string;
  campuses?: Campus[];
}
