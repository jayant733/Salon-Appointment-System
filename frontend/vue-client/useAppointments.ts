import { ref, computed } from 'vue';

export interface ServiceItem {
  id: number;
  name: string;
  durationMinutes: number;
  price: number;
}

export interface AvailableSlot {
  startTime: string;
  endTime: string;
  available: boolean;
}

export interface CreateAppointmentPayload {
  customerId: number;
  employeeId: number;
  serviceId: number;
  startTime: string;
}

/**
 * Vue 3 Composable for SalonPlan Appointment Scheduling.
 * Parallels the Angular AppointmentService and AvailabilityService.
 */
export function useAppointments(apiBaseUrl = '/api') {
  const loading = ref(false);
  const error = ref<string | null>(null);
  const availableSlots = ref<AvailableSlot[]>([]);
  const selectedService = ref<ServiceItem | null>(null);
  const selectedSlot = ref<string | null>(null);

  const isSlotSelected = computed(() => !!selectedSlot.value);

  async function fetchAvailability(employeeId: number, date: string, serviceId: number) {
    loading.value = true;
    error.value = null;
    try {
      const token = localStorage.getItem('access_token');
      const response = await fetch(
        `${apiBaseUrl}/availability?employeeId=${employeeId}&date=${date}&serviceId=${serviceId}`,
        {
          headers: {
            Authorization: token ? `Bearer ${token}` : '',
            'Content-Type': 'application/json',
          },
        }
      );
      if (!response.ok) throw new Error('Failed to fetch slot availability');
      availableSlots.value = await response.json();
    } catch (err: any) {
      error.value = err.message || 'An error occurred';
    } finally {
      loading.value = false;
    }
  }

  async function bookAppointment(payload: CreateAppointmentPayload) {
    loading.value = true;
    error.value = null;
    try {
      const token = localStorage.getItem('access_token');
      const response = await fetch(`${apiBaseUrl}/appointments`, {
        method: 'POST',
        headers: {
          Authorization: token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });
      if (!response.ok) throw new Error('Failed to book appointment');
      return await response.json();
    } catch (err: any) {
      error.value = err.message || 'Failed to book appointment';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  return {
    loading,
    error,
    availableSlots,
    selectedService,
    selectedSlot,
    isSlotSelected,
    fetchAvailability,
    bookAppointment,
  };
}
