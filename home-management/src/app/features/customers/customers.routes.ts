import { Routes } from '@angular/router';

export const CUSTOMER_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./customer-list/customer-list.component').then(m => m.CustomerListComponent),
  },
  {
    path: 'neu',
    loadComponent: () =>
      import('./customer-form/customer-form.component').then(m => m.CustomerFormComponent),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./customer-detail/customer-detail.component').then(m => m.CustomerDetailComponent),
  },
  {
    path: ':id/bearbeiten',
    loadComponent: () =>
      import('./customer-form/customer-form.component').then(m => m.CustomerFormComponent),
  },
];
