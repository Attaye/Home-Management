import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Reading, ReadingFilter } from '../models/reading.model';
import { environment } from '../../environments/environment';

// Wie das Backend eine Ablesung speichert (customerId statt Customer-Objekt)
interface BackendReading {
  id:            string;
  dateOfReading: string;
  meterCount:    number;
  meterId:       string;
  comment:       string | null;
  kindOfMeter:   string;
  customerId:    string | null;
  substitute:    boolean;
}

@Injectable({ providedIn: 'root' })
export class ReadingService {
  private readonly base     = `${environment.apiUrl}/readings`;
  private readonly filtered = `${environment.apiUrl}/readingfiltered`;
  private readonly headers  = new HttpHeaders({ 'Content-Type': 'application/json' });

  constructor(private http: HttpClient) {}

  // Ohne Filter → GET /readings (alle)
  // Mit Filter  → GET /readingfiltered (customer optional, kind/datum optional)
  getFiltered(filter: ReadingFilter = {}): Observable<Reading[]> {
    const hasFilter = filter.customer || filter.start || filter.end || filter.kindOfMeter;

    if (hasFilter) {
      let params = new HttpParams();
      if (filter.customer)    params = params.set('customer',    filter.customer);
      if (filter.start)       params = params.set('start',       filter.start);
      if (filter.end)         params = params.set('end',         filter.end);
      if (filter.kindOfMeter) params = params.set('kindOfMeter', filter.kindOfMeter);

      // /readingfiltered gibt Reading-Objekte DIREKT zurück (kein Wrapper)
      return this.http.get<BackendReading[]>(this.filtered, { params }).pipe(
          map(arr => arr.map(r => this.mapToFrontend(r)))
      );
    }

    // /readings gibt [{reading: {...}}, ...] zurück
    return this.http.get<Array<any>>(this.base).pipe(
        map(arr => arr.map(item => {
          const raw: BackendReading = item.reading ?? item;
          return this.mapToFrontend(raw);
        }))
    );
  }

  // GET /readings/{uuid} → gibt { reading: {...} } zurück
  getById(id: string): Observable<Reading> {
    return this.http.get<{ reading: BackendReading }>(`${this.base}/${id}`).pipe(
        map(r => this.mapToFrontend(r.reading))
    );
  }

  // POST /readings – Body: { reading: { ..., customerId: uuid } }
  create(reading: Reading): Observable<Reading> {
    return this.http.post<BackendReading>(
        this.base,
        { reading: this.mapToBackend(reading) },
        { headers: this.headers }
    ).pipe(map(r => this.mapToFrontend(r)));
  }

  // PUT /readings – Body: Reading DIREKT (kein Wrapper!)
  update(reading: Reading): Observable<string> {
    return this.http.put(
        this.base,
        this.mapToBackend(reading),   // ← kein { reading: ... } Wrapper!
        { headers: this.headers, responseType: 'text' }
    );
  }

  // DELETE /readings/{uuid}
  delete(id: string): Observable<unknown> {
    return this.http.delete(`${this.base}/${id}`);
  }

  resetDatabase(): Observable<unknown> {
    return this.http.delete(`${environment.apiUrl}/setupDB`);
  }

  // ─── Backend → Frontend ──────────────────────────────────────────────────
  // Backend hat nur customerId, Frontend braucht customer-Objekt
  private mapToFrontend(r: BackendReading): Reading {
    return {
      id:            r.id,
      dateOfReading: r.dateOfReading,
      meterCount:    r.meterCount,
      meterId:       r.meterId,
      comment:       r.comment,
      kindOfMeter:   r.kindOfMeter as any,
      substitute:    r.substitute,
      customer:      r.customerId
          ? { id: r.customerId, firstName: '', lastName: '', gender: 'U', birthDate: null }
          : null,
    };
  }

  // ─── Frontend → Backend ──────────────────────────────────────────────────
  // Frontend hat customer-Objekt, Backend erwartet customerId
  private mapToBackend(r: Reading): BackendReading {
    return {
      id:            r.id ?? '',
      dateOfReading: r.dateOfReading,
      meterCount:    r.meterCount,
      meterId:       r.meterId,
      comment:       r.comment,
      kindOfMeter:   r.kindOfMeter,
      substitute:    r.substitute,
      customerId:    r.customer?.id ?? null,
    };
  }
}