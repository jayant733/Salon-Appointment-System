export type UserRole = 'EMPLOYEE' | 'ADMIN';
export type AppointmentStatus = 'BOOKED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresAt: string;
  email: string;
  role: UserRole;
}

export interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string | null;
  active: boolean;
  createdAt: string;
}

export interface CustomerRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone: string | null;
}

export interface Employee {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  active: boolean;
  userId: number | null;
  createdAt: string;
}

export interface EmployeeRequest {
  firstName: string;
  lastName: string;
  email: string;
}

export interface SalonService {
  id: number;
  name: string;
  description: string | null;
  durationMinutes: number;
  price: number;
  active: boolean;
  createdAt: string;
}

export interface SalonServiceRequest {
  name: string;
  description: string | null;
  durationMinutes: number;
  price: number;
}

export interface UserAccount {
  id: number;
  email: string;
  role: UserRole;
  enabled: boolean;
  createdAt: string;
}

export interface Appointment {
  id: number;
  customerId: number;
  customerName: string;
  employeeId: number;
  employeeName: string;
  serviceId: number;
  serviceName: string;
  price: number;
  startTime: string;
  endTime: string;
  status: AppointmentStatus;
  note: string | null;
  version: number;
  createdAt: string;
  updatedAt: string;
  cancelReason?: string | null;
  cancelledAt?: string | null;
}

export interface CreateAppointmentRequest {
  customerId: number;
  employeeId: number;
  serviceId: number;
  startTime: string;
  note?: string;
}

export interface RescheduleAppointmentRequest {
  employeeId: number;
  serviceId: number;
  startTime: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  numberOfElements?: number;
  first?: boolean;
  last?: boolean;
  empty?: boolean;
}

export interface ApiError {
  timestamp?: string;
  status?: number;
  error?: string;
  code?: string;
  message?: string;
  path?: string;
  fieldErrors?: Record<string, string> | Array<{ field: string; message: string }>;
}
