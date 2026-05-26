<template>
  <div class="appointment-booking-card">
    <header class="booking-header">
      <h2>Book Salon Appointment (Vue 3 Client)</h2>
      <p class="subtitle">Consumes SalonPlan Spring Boot REST API</p>
    </header>

    <div v-if="error" class="alert alert-error">
      {{ error }}
    </div>

    <form @submit.prevent="handleBooking" class="booking-form">
      <!-- Date Picker -->
      <div class="form-group">
        <label for="booking-date">Select Date</label>
        <input
          id="booking-date"
          type="date"
          v-model="bookingDate"
          :min="todayDate"
          @change="onDateOrServiceChange"
          required
        />
      </div>

      <!-- Employee Selection -->
      <div class="form-group">
        <label for="employee-select">Select Stylist</label>
        <select
          id="employee-select"
          v-model="selectedEmployeeId"
          @change="onDateOrServiceChange"
          required
        >
          <option :value="null" disabled>Choose a stylist...</option>
          <option v-for="emp in employees" :key="emp.id" :value="emp.id">
            {{ emp.firstName }} {{ emp.lastName }}
          </option>
        </select>
      </div>

      <!-- Available 15-Minute Slot Grid -->
      <div class="form-group" v-if="availableSlots.length > 0">
        <label>Available Slots (15-min intervals)</label>
        <div class="slots-grid">
          <button
            type="button"
            v-for="slot in availableSlots"
            :key="slot.startTime"
            :class="['slot-chip', { active: selectedSlot === slot.startTime }]"
            :disabled="!slot.available"
            @click="selectedSlot = slot.startTime"
          >
            {{ slot.startTime }}
          </button>
        </div>
      </div>

      <!-- Booking Action -->
      <button
        type="submit"
        class="btn-submit"
        :disabled="loading || !selectedSlot || !selectedEmployeeId"
      >
        <span v-if="loading">Processing...</span>
        <span v-else>Confirm Appointment</span>
      </button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useAppointments } from './useAppointments';

interface Employee {
  id: number;
  firstName: string;
  lastName: string;
}

const props = defineProps<{
  customerId: number;
  serviceId: number;
  employees: Employee[];
}>();

const emit = defineEmits<{
  (e: 'booked', appointment: any): void;
}>();

const {
  loading,
  error,
  availableSlots,
  selectedSlot,
  fetchAvailability,
  bookAppointment,
} = useAppointments();

const bookingDate = ref<string>(new Date().toISOString().split('T')[0]);
const selectedEmployeeId = ref<number | null>(null);

const todayDate = computed(() => new Date().toISOString().split('T')[0]);

function onDateOrServiceChange() {
  if (selectedEmployeeId.value && bookingDate.value && props.serviceId) {
    fetchAvailability(selectedEmployeeId.value, bookingDate.value, props.serviceId);
  }
}

async function handleBooking() {
  if (!selectedEmployeeId.value || !selectedSlot.value) return;

  const appointment = await bookAppointment({
    customerId: props.customerId,
    employeeId: selectedEmployeeId.value,
    serviceId: props.serviceId,
    startTime: `${bookingDate.value}T${selectedSlot.value}:00`,
  });

  emit('booked', appointment);
}
</script>

<style scoped>
.appointment-booking-card {
  max-width: 600px;
  margin: 1.5rem auto;
  padding: 1.5rem;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.booking-header h2 {
  margin: 0;
  font-size: 1.4rem;
  color: #1a202c;
}
.subtitle {
  color: #718096;
  font-size: 0.9rem;
  margin-top: 0.25rem;
}
.form-group {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
}
.form-group label {
  font-weight: 600;
  margin-bottom: 0.4rem;
  font-size: 0.88rem;
}
.slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 0.5rem;
  margin-top: 0.5rem;
}
.slot-chip {
  padding: 0.4rem 0.6rem;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
  background: #f8fafc;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.2s;
}
.slot-chip.active {
  background: #3182ce;
  color: #ffffff;
  border-color: #3182ce;
}
.slot-chip:disabled {
  background: #e2e8f0;
  color: #a0aec0;
  cursor: not-allowed;
}
.btn-submit {
  margin-top: 1.5rem;
  width: 100%;
  padding: 0.75rem;
  background: #2b6cb0;
  color: #ffffff;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
}
.btn-submit:disabled {
  background: #a0aec0;
  cursor: not-allowed;
}
.alert-error {
  margin-top: 1rem;
  padding: 0.75rem;
  border-radius: 4px;
  background: #fed7d7;
  color: #c53030;
  font-size: 0.9rem;
}
</style>
