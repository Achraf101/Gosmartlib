import { Location } from './location';

export interface AuthUser {
  userId: number;
  username: string;
  role: string;
  location: Location[];
  locationId: number;
  schoolId: number;
}
