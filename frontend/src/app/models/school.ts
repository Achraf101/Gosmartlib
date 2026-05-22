import { Location } from './location';

export interface School {
  id: number;
  name: string;
  adres?: string;
  contact?: string;
  description?: string;
  subdomain: string;
  borrowLimit: number;
  borrowPeriod: number;
  extendPeriod: number;
  extendLimit: number;
  locations?: Location[];
}

export interface CreateSchool {
  id: number;
  name: string;
  adres?: string;
  contact?: string;
  description?: string;
  subdomain: string;
  borrowLimit: number;
  borrowPeriod: number;
  extendPeriod: number;
  extendLimit: number;
  clientId: string;
  clientSecret: string;
}
