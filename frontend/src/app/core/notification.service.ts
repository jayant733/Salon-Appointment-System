import { Injectable, signal } from '@angular/core';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';

export interface Notification {
  id: number;
  type: NotificationType;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private nextId = 1;
  readonly messages = signal<Notification[]>([]);

  success(message: string): void {
    this.show('success', message);
  }

  error(message: string): void {
    this.show('error', message, 7000);
  }

  warning(message: string): void {
    this.show('warning', message, 6500);
  }

  info(message: string): void {
    this.show('info', message);
  }

  remove(id: number): void {
    this.messages.update((items) => items.filter((item) => item.id !== id));
  }

  private show(type: NotificationType, message: string, duration = 5000): void {
    const notification: Notification = { id: this.nextId++, type, message };
    this.messages.update((items) => [...items, notification].slice(-4));
    window.setTimeout(() => this.remove(notification.id), duration);
  }
}
