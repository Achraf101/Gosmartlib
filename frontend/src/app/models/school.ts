import { Campus } from './campus';

export interface School {
  id: number;
  name: string;
  adres?: string;
  contact?: string;
  description?: string;
  subdomain: string;
  campuses?: Campus[];
}
