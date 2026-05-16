import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

// Hängt automatisch den Bearer-Token an jeden HTTP-Request
export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const token = inject(AuthService).getToken();
    if (!token) return next(req);
    const authReq = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` },
    });
    return next(authReq);
};