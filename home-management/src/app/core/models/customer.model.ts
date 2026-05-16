// ─── Gender Enum ─────────────────────────────────────────────────────────────
export type Gender = 'D' | 'M' | 'U' | 'W';

export const GENDER_LABELS: Record<Gender, string> = {
  D: 'Divers',
  M: 'Männlich',
  U: 'Unbekannt',
  W: 'Weiblich',
};

// ─── Customer ────────────────────────────────────────────────────────────────
export interface Customer {
  id: string | null;
  firstName: string;
  lastName: string;
  gender: Gender;
  birthDate: string | null; // ISO date string yyyy-MM-dd
}

export function getInitials(c: Customer): string {
  return (c.firstName.charAt(0) + c.lastName.charAt(0)).toUpperCase();
}

export function getFullName(c: Customer): string {
  return `${c.firstName} ${c.lastName}`;
}

// ─── Wrapper types (matches backend JSON schema) ─────────────────────────────
export interface CustomerResponse { customer: Customer; }
export interface CustomersResponse { customers: Customer[]; }

export function emptyCustomer(): Customer {
  return { id: null, firstName: '', lastName: '', gender: 'M', birthDate: null };
}
