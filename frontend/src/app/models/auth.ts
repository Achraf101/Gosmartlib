export interface AuthUser {
  userId: number;
  roles: string[];
  schoolId: number | null;
  firstName: string | null;
  lastName: string | null;
  email: string | null;
  username: string | null;
}
