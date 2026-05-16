import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';
import { environment } from '../../environments/environment';

interface AppUser { id: string; username: string; role: string; }

@Component({
    selector: 'app-admin-users',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './admin-users.component.html',
    styleUrls: ['./admin-users.component.scss'],
})
export class AdminUsersComponent implements OnInit {
    users:    AppUser[] = [];
    loading   = true;
    error     = '';
    success   = '';
    saving    = false;
    showForm  = false;
    // Passwort-Änderung
    changingPasswordFor: string | null = null;
    pwForm!: FormGroup;

    form: FormGroup;

    constructor(
        private fb:   FormBuilder,
        private http: HttpClient,
        private auth: AuthService,
    ) {
        this.form = this.fb.group({
            username: ['', [Validators.required, Validators.minLength(3)]],
            password: ['', [Validators.required, Validators.minLength(6)]],
            role:     ['USER', Validators.required],
        });

        this.pwForm = this.fb.group({
            password: ['', [Validators.required, Validators.minLength(6)]],
        });
    }

    ngOnInit(): void { this.loadUsers(); }

    // ── Alle Benutzer laden ────────────────────────────────────────────────

    loadUsers(): void {
        this.loading = true;
        const headers = this.authHeaders();
        this.http.get<AppUser[]>(`${environment.apiUrl}/admin/users`, { headers })
            .subscribe({
                next: (users) => { this.users = users; this.loading = false; },
                error: () => { this.error = 'Fehler beim Laden der Benutzer.'; this.loading = false; },
            });
    }

    // ── Benutzer anlegen ──────────────────────────────────────────────────

    createUser(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }
        this.saving = true; this.error = ''; this.success = '';

        this.http.post<AppUser>(
            `${environment.apiUrl}/admin/users`,
            this.form.value,
            { headers: this.authHeaders() }
        ).subscribe({
            next: (u) => {
                this.success = `✓ Benutzer "${u.username}" (${u.role}) angelegt.`;
                this.users.push(u);
                this.form.reset({ role: 'USER' });
                this.showForm = false;
                this.saving   = false;
            },
            error: (err) => {
                this.error  = err.error ?? 'Fehler beim Anlegen.';
                this.saving = false;
            },
        });
    }

    // ── Passwort ändern ───────────────────────────────────────────────────

    startPasswordChange(username: string): void {
        this.changingPasswordFor = username;
        this.pwForm.reset();
    }

    cancelPasswordChange(): void { this.changingPasswordFor = null; }

    savePassword(username: string): void {
        if (this.pwForm.invalid) { this.pwForm.markAllAsTouched(); return; }

        this.http.put(
            `${environment.apiUrl}/admin/users/${username}`,
            { password: this.pwForm.value.password },
            { headers: this.authHeaders(), responseType: 'text' }
        ).subscribe({
            next: () => {
                this.success = `✓ Passwort für "${username}" geändert.`;
                this.changingPasswordFor = null;
            },
            error: (err) => { this.error = err.error ?? 'Fehler beim Ändern.'; },
        });
    }

    // ── Benutzer löschen ─────────────────────────────────────────────────

    deleteUser(username: string): void {
        if (!confirm(`Benutzer "${username}" wirklich löschen?`)) return;

        this.http.delete(
            `${environment.apiUrl}/admin/users/${username}`,
            { headers: this.authHeaders(), responseType: 'text' }
        ).subscribe({
            next: () => {
                this.users   = this.users.filter(u => u.username !== username);
                this.success = `✓ Benutzer "${username}" gelöscht.`;
            },
            error: (err) => { this.error = err.error ?? 'Fehler beim Löschen.'; },
        });
    }

    private authHeaders(): HttpHeaders {
        return new HttpHeaders({
            'Content-Type':  'application/json',
            'Authorization': `Bearer ${this.auth.getToken()}`,
        });
    }

    isInvalid(form: FormGroup, field: string): boolean {
        const c = form.get(field);
        return !!(c?.invalid && c?.touched);
    }

    get isAdmin(): boolean { return this.auth.currentUser()?.role === 'ADMIN'; }
}