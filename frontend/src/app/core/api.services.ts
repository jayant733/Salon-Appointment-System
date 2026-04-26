import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Appointment,
  AppointmentStatus,
  CreateAppointmentRequest,
  Customer,
  CustomerRequest,
  Employee,
  EmployeeRequest,
  Page,
  RescheduleAppointmentRequest,
  SalonService,
  SalonServiceRequest,
  UserAccount,
  UserRole,
} from './models';

@Injectable({ providedIn: 'root' })
export class CustomerApi {
  private readonly http = inject(HttpClient);

  list(search = '', page = 0, size = 100): Observable<Page<Customer>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'lastName,asc');
    if (search.trim()) {
      params = params.set('search', search.trim());
    }
    return this.http.get<Page<Customer>>('/api/customers', { params });
  }

  get(id: number): Observable<Customer> {
    return this.http.get<Customer>(`/api/customers/${id}`);
  }

  create(request: CustomerRequest): Observable<Customer> {
    return this.http.post<Customer>('/api/customers', request);
  }

  update(id: number, request: CustomerRequest): Observable<Customer> {
    return this.http.put<Customer>(`/api/customers/${id}`, request);
  }

  changeActive(id: number, active: boolean): Observable<Customer> {
    return this.http.patch<Customer>(
      `/api/customers/${id}/active`,
      {},
      {
        params: { value: active },
      },
    );
  }
}

@Injectable({ providedIn: 'root' })
export class EmployeeApi {
  private readonly http = inject(HttpClient);

  list(activeOnly = false): Observable<Employee[]> {
    return this.http.get<Employee[]>('/api/employees', { params: { activeOnly } });
  }

  get(id: number): Observable<Employee> {
    return this.http.get<Employee>(`/api/employees/${id}`);
  }

  create(request: EmployeeRequest): Observable<Employee> {
    return this.http.post<Employee>('/api/employees', request);
  }

  update(id: number, request: EmployeeRequest): Observable<Employee> {
    return this.http.put<Employee>(`/api/employees/${id}`, request);
  }

  changeActive(id: number, active: boolean): Observable<Employee> {
    return this.http.patch<Employee>(
      `/api/employees/${id}/active`,
      {},
      {
        params: { value: active },
      },
    );
  }
}

@Injectable({ providedIn: 'root' })
export class SalonServiceApi {
  private readonly http = inject(HttpClient);

  list(activeOnly = false): Observable<SalonService[]> {
    return this.http.get<SalonService[]>('/api/services', { params: { activeOnly } });
  }

  get(id: number): Observable<SalonService> {
    return this.http.get<SalonService>(`/api/services/${id}`);
  }

  create(request: SalonServiceRequest): Observable<SalonService> {
    return this.http.post<SalonService>('/api/services', request);
  }

  update(id: number, request: SalonServiceRequest): Observable<SalonService> {
    return this.http.put<SalonService>(`/api/services/${id}`, request);
  }

  changeActive(id: number, active: boolean): Observable<SalonService> {
    return this.http.patch<SalonService>(
      `/api/services/${id}/active`,
      {},
      {
        params: { value: active },
      },
    );
  }
}

@Injectable({ providedIn: 'root' })
export class UserApi {
  private readonly http = inject(HttpClient);

  list(page = 0, size = 100): Observable<Page<UserAccount>> {
    return this.http.get<Page<UserAccount>>('/api/users', {
      params: { page, size, sort: 'email,asc' },
    });
  }

  changeRole(id: number, role: UserRole): Observable<UserAccount> {
    return this.http.patch<UserAccount>(`/api/users/${id}/role`, { role });
  }

  changeEnabled(id: number, enabled: boolean): Observable<UserAccount> {
    return this.http.patch<UserAccount>(`/api/users/${id}/enabled`, { enabled });
  }
}

@Injectable({ providedIn: 'root' })
export class AppointmentApi {
  private readonly http = inject(HttpClient);

  search(options: {
    from: string;
    to: string;
    employeeId?: number;
    status?: AppointmentStatus;
    page?: number;
    size?: number;
  }): Observable<Page<Appointment>> {
    let params = new HttpParams()
      .set('from', options.from)
      .set('to', options.to)
      .set('page', options.page ?? 0)
      .set('size', options.size ?? 200)
      .set('sort', 'startTime,asc');

    if (options.employeeId) {
      params = params.set('employeeId', options.employeeId);
    }
    if (options.status) {
      params = params.set('status', options.status);
    }

    return this.http.get<Page<Appointment>>('/api/appointments', { params });
  }

  get(id: number): Observable<Appointment> {
    return this.http.get<Appointment>(`/api/appointments/${id}`);
  }

  create(request: CreateAppointmentRequest): Observable<Appointment> {
    return this.http.post<Appointment>('/api/appointments', request);
  }

  reschedule(id: number, request: RescheduleAppointmentRequest): Observable<Appointment> {
    return this.http.patch<Appointment>(`/api/appointments/${id}/reschedule`, request);
  }

  confirm(id: number): Observable<Appointment> {
    return this.http.patch<Appointment>(`/api/appointments/${id}/confirm`, {});
  }

  cancel(id: number, reason?: string): Observable<Appointment> {
    return this.http.patch<Appointment>(`/api/appointments/${id}/cancel`, {
      reason: reason?.trim() || null,
    });
  }

  complete(id: number): Observable<Appointment> {
    return this.http.patch<Appointment>(`/api/appointments/${id}/complete`, {});
  }

  changeNote(id: number, note: string | null): Observable<Appointment> {
    return this.http.patch<Appointment>(`/api/appointments/${id}/note`, { note });
  }

  availableStarts(employeeId: number, serviceId: number, date: string): Observable<string[]> {
    return this.http.get<string[]>('/api/availability', {
      params: { employeeId, serviceId, date },
    });
  }
}
