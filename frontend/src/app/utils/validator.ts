import { AbstractControl, ValidationErrors } from '@angular/forms';

export function maxEntries(max: number) {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as any[];

    if (!value) return null;

    return value.length > max ? { maxArrayLength: true } : null;
  };
}

export function isbnValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;

  const value = control.value.replace(/[-\s]/g, '');

  if (/^\d{10}$/.test(value)) return validateIsbn10(value);
  if (/^\d{13}$/.test(value)) return validateIsbn13(value);

  return { isbn: true };
}

function validateIsbn10(isbn: string): ValidationErrors | null {
  let sum = 0;

  for (let i = 0; i < 10; i++) {
    sum += (10 - i) * Number(isbn[i]);
  }

  return sum % 11 === 0 ? null : { isbn: true };
}

function validateIsbn13(isbn: string): ValidationErrors | null {
  let sum = 0;

  for (let i = 0; i < 12; i++) {
    sum += Number(isbn[i]) * (i % 2 === 0 ? 1 : 3);
  }

  const check = (10 - (sum % 10)) % 10;

  return check === Number(isbn[12]) ? null : { isbn: true };
}
