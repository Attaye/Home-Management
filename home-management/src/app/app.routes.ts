import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent) },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard',   canActivate: [authGuard], loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
  { path: 'kunden',      canActivate: [authGuard], loadChildren: () => import('./features/customers/customers.routes').then(m => m.CUSTOMER_ROUTES) },
  { path: 'ablesungen',  canActivate: [authGuard], loadChildren: () => import('./features/readings/readings.routes').then(m => m.READING_ROUTES) },
  { path: 'reports',     canActivate: [authGuard], loadComponent: () => import('./features/reports/reports.component').then(m => m.ReportsComponent) },
  { path: 'auswertung',  canActivate: [authGuard], loadComponent: () => import('./features/auswertung/auswertung.component').then(m => m.AuswertungComponent) },
  { path: 'admin/users', canActivate: [authGuard], loadComponent: () => import('./features/admin/admin-users.component').then(m => m.AdminUsersComponent) },
  { path: '**', redirectTo: 'dashboard' },
];