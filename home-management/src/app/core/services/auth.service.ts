import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { LoginRequest, LoginResponse, AuthUser } from '../models/auth.model';
import { environment } from '../../environments/environment';

const TOKEN_KEY = 'itp_token';
const USER_KEY  = 'itp_user';

@Injectable({ providedIn: 'root' })
export class AuthService {

    // Reaktives Signal – Komponenten können direkt lesen
    readonly currentUser = signal<AuthUser | null>(this.loadUser());

    constructor(private http: HttpClient, private router: Router) {}

    login(req: LoginRequest): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(
            `${environment.apiUrl}/auth/login`, req,
            { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
        ).pipe(
            tap(res => {
                localStorage.setItem(TOKEN_KEY, res.token);
                const user: AuthUser = { username: res.username, role: res.role };
                localStorage.setItem(USER_KEY, JSON.stringify(user));
                this.currentUser.set(user);
            })
        );
    }

    logout(): void {
        const token = this.getToken();
        if (token) {
            this.http.post(
                `${environment.apiUrl}/auth/logout`, {},
                { headers: { Authorization: `Bearer ${token}` }, responseType: 'text' }
            ).subscribe({ error: () => {} });
        }
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        this.currentUser.set(null);
        this.router.navigate(['/login']);
    }

    getToken(): string | null {
        return localStorage.getItem(TOKEN_KEY);
    }

    isLoggedIn(): boolean {
        return !!this.getToken();
    }

    //  NEU: Gibt true zurück wenn der User NUR lesen darf
    isReadonly(): boolean {
        return this.currentUser()?.role === 'READONLY';
    }

    //  NEU: Gibt true zurück wenn der User schreiben/ändern darf (USER oder ADMIN)
    canWrite(): boolean {
        return !this.isReadonly();
    }

    private loadUser(): AuthUser | null {
        const raw = localStorage.getItem(USER_KEY);
        if (!raw) return null;
        try { return JSON.parse(raw) as AuthUser; } catch { return null; }
    }
}