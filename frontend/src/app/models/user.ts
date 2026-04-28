export interface User {
  role: 'ADMIN' | 'BIBLIOTHEEKBEHEERDER' | 'LEERKRACHT' | 'STUDENT';
  name: string;
}
