import { Location } from './location';

export interface AuthUser {
  userId: number;
  role: string;
  schoolId: number | null;
  locationId: number | null;
  firstName: string | null;
  lastName: string | null;
  email: string | null;
  username: string | null;
  location: Location[];
}
