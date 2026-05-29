import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LocationStateService {
  private readonly KEY = 'selected_location';

  get locationId(): number | null {
    const val = localStorage.getItem(this.KEY);
    return val ? Number(val) : null;
  }

  set(id: number | null): void {
    if (id === null) {
      localStorage.removeItem(this.KEY);
    } else {
      localStorage.setItem(this.KEY, String(id));
    }
  }
}
