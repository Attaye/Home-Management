import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {Customer} from '../models/customer'

@Injectable({
  providedIn: 'root',
})
export class CustomerService {

  private baseUrl: string= "http://localhost:8080";

  constructor(private httpClient: HttpClient) { }

  getCustomerList(): Observable<Customer[]>{
    return this.httpClient.get<Customer[]>(`${this.baseUrl}/customers`);
  }
}
