import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Person } from '../models/interfaces';

@Injectable({
  providedIn: 'root',
})
export class PersonService {
  
  // Communicate with the real backend.
  private apiUrl = `https://gosmartlib.tech//api/hello`;
  // Communicate with test backend.
  private testApiUrl = 'http://localhost:8080/api/hello';

  constructor(private http: HttpClient) {}

  getUsers(): Observable<Person[]> {
    return this.http.get<Person[]>(this.apiUrl);
  }
}
