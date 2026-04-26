import { Routes } from '@angular/router';
import { adminGuard } from './core/admin.guard';
import { authGuard, guestGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    title: 'Anmelden | SalonPlan',
    loadComponent: () => import('./features/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'appointments',
    canActivate: [authGuard],
    title: 'Termine | SalonPlan',
    loadComponent: () =>
      import('./features/appointments/appointment-list.component').then(
        (m) => m.AppointmentListComponent,
      ),
  },
  {
    path: 'appointments/new',
    canActivate: [authGuard],
    title: 'Neuer Termin | SalonPlan',
    loadComponent: () =>
      import('./features/appointments/appointment-form.component').then(
        (m) => m.AppointmentFormComponent,
      ),
  },
  {
    path: 'appointments/:id/edit',
    canActivate: [authGuard],
    title: 'Termin verschieben | SalonPlan',
    loadComponent: () =>
      import('./features/appointments/appointment-form.component').then(
        (m) => m.AppointmentFormComponent,
      ),
  },
  {
    path: 'customers',
    canActivate: [authGuard],
    title: 'Kunden | SalonPlan',
    loadComponent: () =>
      import('./features/customers/customer.component').then((m) => m.CustomerComponent),
  },
  {
    path: 'employees',
    canActivate: [authGuard, adminGuard],
    title: 'Mitarbeiter | SalonPlan',
    loadComponent: () =>
      import('./features/employees/employee.component').then((m) => m.EmployeeComponent),
  },
  {
    path: 'services',
    canActivate: [authGuard, adminGuard],
    title: 'Leistungen | SalonPlan',
    loadComponent: () =>
      import('./features/services/salon-service.component').then((m) => m.SalonServiceComponent),
  },
  {
    path: 'users',
    canActivate: [authGuard, adminGuard],
    title: 'Benutzerkonten | SalonPlan',
    loadComponent: () => import('./features/users/user.component').then((m) => m.UserComponent),
  },
  { path: '', pathMatch: 'full', redirectTo: 'appointments' },
  { path: '**', redirectTo: 'appointments' },
];
