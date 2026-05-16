import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
    form: FormGroup;
    loading = false;
    error   = '';

    constructor(
        private fb:     FormBuilder,
        private auth:   AuthService,
        private router: Router,
    ) {
        if (this.auth.isLoggedIn()) this.router.navigate(['/dashboard']);
        this.form = this.fb.group({
            username: ['', Validators.required],
            password: ['', Validators.required],
        });
    }

    submit(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }
        this.loading = true;
        this.error   = '';

        this.auth.login(this.form.value).subscribe({
            next: () => this.router.navigate(['/dashboard']),
            error: (err) => {
                this.error   = err.status === 401
                    ? 'Benutzername oder Passwort falsch.'
                    : 'Verbindung zum Server fehlgeschlagen.';
                this.loading = false;
            },
        });
    }

    isInvalid(field: string): boolean {
        const c = this.form.get(field);
        return !!(c?.invalid && c?.touched);
    }
}