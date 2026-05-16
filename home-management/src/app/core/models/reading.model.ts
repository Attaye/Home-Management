import { Customer } from './customer.model';

// ─── KindOfMeter Enum ─────────────────────────────────────────────────────────
export type KindOfMeter = 'HEIZUNG' | 'STROM' | 'WASSER' | 'UNBEKANNT';

export const KIND_LABELS: Record<KindOfMeter, string> = {
  HEIZUNG:   'Heizung',
  STROM:     'Strom',
  WASSER:    'Wasser',
  UNBEKANNT: 'Unbekannt',
};

export const KIND_UNITS: Record<KindOfMeter, string> = {
  HEIZUNG:   'kWh',
  STROM:     'kWh',
  WASSER:    'm³',
  UNBEKANNT: '',
};

// ─── Reading ──────────────────────────────────────────────────────────────────
export interface Reading {
  id: string | null;
  customer: Customer | null;
  dateOfReading: string;   // ISO date yyyy-MM-dd
  meterCount: number;
  meterId: string;
  substitute: boolean;
  kindOfMeter: KindOfMeter;
  comment: string | null;
}

// ─── Wrapper types ────────────────────────────────────────────────────────────
export interface ReadingResponse  { reading: Reading; }
export interface ReadingsResponse { readings: Reading[]; }

export function emptyReading(): Reading {
  return {
    id: null,
    customer: null,
    dateOfReading: new Date().toISOString().split('T')[0],
    meterCount: 0,
    meterId: '',
    substitute: false,
    kindOfMeter: 'STROM',
    comment: null,
  };
}

// ─── Filter params ────────────────────────────────────────────────────────────
export interface ReadingFilter {
  customer?: string;
  start?: string;
  end?: string;
  kindOfMeter?: KindOfMeter | '';
}
