import { Campus } from './campus';

export interface AuthUser {
  username: string;
  role: string;
  campus: Campus[];
}
