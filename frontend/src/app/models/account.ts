export interface Account {
  name: string;
  role: 'ADMIN' | 'BEHEERDER' | 'LEERLING';
}

export interface Password {
  current_password: string;
  new_password: string;
}
