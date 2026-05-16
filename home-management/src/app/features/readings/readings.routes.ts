import { Routes } from '@angular/router';

export const READING_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./reading-list/reading-list.component').then(m => m.ReadingListComponent),
  },
  {
    path: 'neu',
    loadComponent: () =>
      import('./reading-form/reading-form.component').then(m => m.ReadingFormComponent),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./reading-detail/reading-detail.component').then(m => m.ReadingDetailComponent),
  },
  {
    path: ':id/bearbeiten',
    loadComponent: () =>
      import('./reading-form/reading-form.component').then(m => m.ReadingFormComponent),
  },
];
