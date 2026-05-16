import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Customer } from '../models/customer.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly base    = `${environment.apiUrl}/customers`;
  private readonly headers = new HttpHeaders({ 'Content-Type': 'application/json' });

  constructor(private http: HttpClient) {}

  // GET /customers → gibt [ { customer: {...} }, ... ] zurück
  getAll(): Observable<Customer[]> {
    return this.http.get<Array<{ customer: Customer }>>(this.base).pipe(
        map(arr => arr.map(item => item.customer))
    );
  }

  // GET /customers/{uuid} → gibt { customer: {...} } zurück
  getById(id: string): Observable<Customer> {
    return this.http.get<{ customer: Customer }>(`${this.base}/${id}`).pipe(
        map(r => r.customer)
    );
  }

  // POST /customers – Body: { customer: {...} }  → gibt Customer direkt zurück
  create(customer: Customer): Observable<Customer> {
    return this.http.post<Customer>(
        this.base,
        { customer },
        { headers: this.headers }
    );
  }

  // PUT /customers – Body: Customer DIREKT (kein Wrapper!) → gibt String zurück
  update(customer: Customer): Observable<string> {
    return this.http.put(
        this.base,
        customer,          // ← kein { customer } Wrapper!
        { headers: this.headers, responseType: 'text' }
    );
  }

  // DELETE /customers/{uuid}
  delete(id: string): Observable<unknown> {
    return this.http.delete(`${this.base}/${id}`);
  }
}