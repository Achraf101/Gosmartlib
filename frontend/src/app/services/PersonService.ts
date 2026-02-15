import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Person } from '../models/interfaces';

@Injectable({
  providedIn: 'root',
})
export class PersonService {
  
  private apiUrl = `/api/hello`;

  constructor(private http: HttpClient) {}

  getUsers(): Observable<Person[]> {
    return this.http.get<Person[]>(this.apiUrl);
  }
}
