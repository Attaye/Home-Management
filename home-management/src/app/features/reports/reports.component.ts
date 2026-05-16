import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { forkJoin, of } from 'rxjs';
import { CustomerService } from '../../core/services/customer.service';
import { ReadingService } from '../../core/services/reading.service';
import { AuthService } from '../../core/services/auth.service'; // ✅ NEU
import { Customer } from '../../core/models/customer.model';
import { Reading } from '../../core/models/reading.model';
import { environment } from '../../environments/environment';

type ExportFormat = 'JSON' | 'CSV' | 'XML';
type DataType     = 'customers' | 'readings' | 'all';
interface ImportRecord { name: string; date: string; status: 'success'|'error'; count: number; msg: string; }

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.scss'],
})
export class ReportsComponent {
  exporting = false; exportMsg = ''; exportErr = '';
  importing = false; importMsg = ''; importErr = '';
  importProgress = 0; dragOver = false;
  history: ImportRecord[] = [];

  exportRows: Array<{type: DataType; label: string; icon: string}> = [
    { type: 'customers', label: 'Kunden',     icon: '👥' },
    { type: 'readings',  label: 'Ablesungen', icon: '📊' },
    { type: 'all',       label: 'Alle Daten', icon: '💾' },
  ];

  constructor(
      private cSvc: CustomerService,
      private rSvc: ReadingService,
      private http: HttpClient,
      public authService: AuthService  // ✅ NEU: public damit HTML darauf zugreifen kann
  ) {}

  // ══════════════ EXPORT ════════════════════════════════════════════════════

  exportData(type: DataType, format: ExportFormat): void {
    this.exporting = true; this.exportMsg = ''; this.exportErr = '';
    forkJoin({
      customers: type !== 'readings'  ? this.cSvc.getAll()      : of([] as Customer[]),
      readings:  type !== 'customers' ? this.rSvc.getFiltered() : of([] as Reading[]),
    }).subscribe({
      next: ({ customers, readings }) => {
        const fn = `itp_${type}_${this.today()}.${format.toLowerCase()}`;
        try {
          if (format === 'JSON') this.dlJSON(type, customers, readings, fn);
          if (format === 'CSV')  this.dlCSV(type,  customers, readings, fn);
          if (format === 'XML')  this.dlXML(type,  customers, readings, fn);
          this.exportMsg = `✓ ${fn} heruntergeladen (${customers.length + readings.length} Datensätze).`;
        } catch { this.exportErr = 'Fehler beim Export.'; }
        this.exporting = false;
      },
      error: () => { this.exportErr = 'Fehler beim Laden.'; this.exporting = false; },
    });
  }

  private dlJSON(t: DataType, c: Customer[], r: Reading[], fn: string): void {
    const d = t === 'customers' ? c
        : t === 'readings'  ? r.map(x => this.re(x))
            : { customers: c, readings: r.map(x => this.re(x)) };
    this.dl(new Blob([JSON.stringify(d, null, 2)], { type: 'application/json' }), fn);
  }

  private dlCSV(t: DataType, c: Customer[], r: Reading[], fn: string): void {
    let csv = '';
    if (t !== 'readings') {
      csv += '# KUNDEN\nid;firstName;lastName;gender;birthDate\n';
      csv += c.map(x => `${x.id ?? ''};${x.firstName};${x.lastName};${x.gender};${x.birthDate ?? ''}`).join('\n') + '\n\n';
    }
    if (t !== 'customers') {
      csv += '# ABLESUNGEN\nid;customerId;dateOfReading;kindOfMeter;meterCount;meterId;substitute;comment\n';
      csv += r.map(x => { const e = this.re(x);
        return `${e.id};${e.customerId ?? ''};${e.dateOfReading};${e.kindOfMeter};${e.meterCount};${e.meterId};${e.substitute};${e.comment ?? ''}`;
      }).join('\n');
    }
    this.dl(new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' }), fn);
  }

  private dlXML(t: DataType, c: Customer[], r: Reading[], fn: string): void {
    let x = '<?xml version="1.0" encoding="UTF-8"?>\n<export>\n';
    if (t !== 'readings') {
      x += '  <customers>\n';
      c.forEach(v => { x += `    <customer><id>${v.id ?? ''}</id><firstName>${this.esc(v.firstName)}</firstName><lastName>${this.esc(v.lastName)}</lastName><gender>${v.gender}</gender><birthDate>${v.birthDate ?? ''}</birthDate></customer>\n`; });
      x += '  </customers>\n';
    }
    if (t !== 'customers') {
      x += '  <readings>\n';
      r.forEach(v => { const e = this.re(v); x += `    <reading><id>${e.id}</id><customerId>${e.customerId ?? ''}</customerId><dateOfReading>${e.dateOfReading}</dateOfReading><kindOfMeter>${e.kindOfMeter}</kindOfMeter><meterCount>${e.meterCount}</meterCount><meterId>${this.esc(e.meterId)}</meterId><substitute>${e.substitute}</substitute><comment>${this.esc(e.comment ?? '')}</comment></reading>\n`; });
      x += '  </readings>\n';
    }
    x += '</export>';
    this.dl(new Blob([x], { type: 'application/xml' }), fn);
  }

  // ══════════════ IMPORT ════════════════════════════════════════════════════

  onDragOver(e: DragEvent): void  { e.preventDefault(); this.dragOver = true; }
  onDragLeave(): void              { this.dragOver = false; }
  onDrop(e: DragEvent): void       { e.preventDefault(); this.dragOver = false; this.handleFiles(e.dataTransfer?.files); }
  onFileSelect(ev: Event): void    {
    this.handleFiles((ev.target as HTMLInputElement).files);
    (ev.target as HTMLInputElement).value = '';
  }

  handleFiles(files?: FileList | null): void {
    if (!files?.length) return;
    const file = files[0];
    const ext  = file.name.split('.').pop()?.toLowerCase() ?? '';

    if (!['json', 'xml', 'csv', 'pdf'].includes(ext)) {
      this.importErr = `.${ext} nicht unterstützt. Erlaubt: JSON, CSV, XML, PDF.`;
      return;
    }

    this.importing = true; this.importMsg = ''; this.importErr = ''; this.importProgress = 10;

    if (ext === 'pdf') { this.sendToBackend(file); return; }

    if (ext === 'csv') {
      const preview = new FileReader();
      preview.onload = (e) => {
        const sample = ((e.target?.result as string) ?? '').replace(/^[\s\uFEFF]+/, '');
        const firstLine = sample.split('\n')[0] ?? '';
        const isSprintFormat = firstLine.toLowerCase().includes('kunde') && !firstLine.startsWith('#');
        if (isSprintFormat) { this.sendToBackend(file); }
        else { this.parseAndImport(file, 'csv'); }
      };
      preview.readAsText(file, 'UTF-8');
      return;
    }

    this.parseAndImport(file, ext);
  }

  private parseAndImport(file: File, ext: string): void {
    const reader = new FileReader();
    reader.onload = async (ev) => {
      const text = ev.target?.result as string;
      try {
        let customers: Partial<Customer>[] = [];
        let readings: any[] = [];
        if (ext === 'json') ({ customers, readings } = this.parseJSON(text));
        if (ext === 'xml')  ({ customers, readings } = this.parseXML(text));
        if (ext === 'csv')  ({ customers, readings } = this.parseCSV(text));

        const total = customers.length + readings.length;
        if (!total) {
          this.importErr = 'Keine Daten gefunden. Exportierte Dateien vom Reports-Bereich verwenden.';
          this.importing = false;
          return;
        }

        let saved = 0, skipped = 0, errors = 0;
        for (const c of customers) {
          try { await this.cSvc.create(c as Customer).toPromise(); saved++; }
          catch (err: any) { if (err?.status === 409) skipped++; else errors++; }
          this.importProgress = Math.round(10 + (saved + skipped + errors) / total * 90);
        }
        for (const r of readings) {
          const readingForApi: Reading = {
            ...r,
            customer: r.customerId
                ? { id: r.customerId, firstName: '', lastName: '', gender: 'U', birthDate: null }
                : null,
          };
          try { await this.rSvc.create(readingForApi).toPromise(); saved++; }
          catch (err: any) { if (err?.status === 409) skipped++; else errors++; }
          this.importProgress = Math.round(10 + (saved + skipped + errors) / total * 90);
        }

        let msg = `✓ ${saved} neu gespeichert`;
        if (skipped > 0) msg += `, ${skipped} bereits vorhanden`;
        if (errors > 0)  msg += `, ${errors} Fehler`;
        const status: 'success'|'error' = errors > 0 ? 'error' : 'success';
        this.importMsg = msg;
        this.history.unshift({ name: file.name, date: this.today(), status, count: saved, msg });
      } catch (e: any) {
        this.importErr = `Parse-Fehler: ${e?.message ?? e}`;
        this.history.unshift({ name: file.name, date: this.today(), status: 'error', count: 0, msg: this.importErr });
      }
      this.importing = false;
    };
    reader.readAsText(file, 'UTF-8');
  }

  private sendToBackend(file: File): void {
    const formData = new FormData();
    formData.append('file', file);
    this.http.post(`${environment.apiUrl}/import`, formData, { responseType: 'text' }).subscribe({
      next: (msg) => {
        this.importMsg = `✓ ${msg}`;
        this.importProgress = 100;
        this.history.unshift({ name: file.name, date: this.today(), status: 'success', count: 0, msg: this.importMsg });
        this.importing = false;
      },
      error: (err) => {
        const errMsg = err.error ?? err.message ?? 'Unbekannter Fehler';
        this.importErr = `Fehler: ${errMsg}`;
        this.history.unshift({ name: file.name, date: this.today(), status: 'error', count: 0, msg: this.importErr });
        this.importing = false;
      },
    });
  }

  private parseJSON(text: string): { customers: Partial<Customer>[]; readings: any[] } {
    const d = JSON.parse(text);
    if (Array.isArray(d) && d.length > 0) {
      const first = d[0];
      if ('firstName' in first || 'lastName' in first) return { customers: d, readings: [] };
      if ('meterCount' in first || 'kindOfMeter' in first) return { customers: [], readings: d };
    }
    if (d && typeof d === 'object' && !Array.isArray(d)) {
      return { customers: Array.isArray(d.customers) ? d.customers : [], readings: Array.isArray(d.readings) ? d.readings : [] };
    }
    return { customers: [], readings: [] };
  }

  private parseCSV(text: string): { customers: Partial<Customer>[]; readings: any[] } {
    const customers: Partial<Customer>[] = [];
    const readings: any[] = [];
    let mode: 'customer'|'reading'|null = null;
    let headers: string[] = [];
    const lines = text.replace(/^[\s\uFEFF\xA0]+/, '').split('\n').map(l => l.replace(/\r$/, '').trim()).filter(l => l.length > 0);
    for (const line of lines) {
      if (line.charAt(0) === '#') { mode = line.toUpperCase().includes('KUNDEN') ? 'customer' : 'reading'; headers = []; continue; }
      if (headers.length === 0 && line.includes(';')) { headers = line.split(';').map(h => h.trim()); continue; }
      if (headers.length > 0 && line.includes(';')) {
        const vals = line.split(';');
        const obj: Record<string, string> = {};
        headers.forEach((h, i) => obj[h] = (vals[i] ?? '').trim());
        if (mode === 'customer') customers.push({ id: obj['id'] || null, firstName: obj['firstName'] || '', lastName: obj['lastName'] || '', gender: (obj['gender'] || 'U') as any, birthDate: obj['birthDate'] || null });
        else if (mode === 'reading') readings.push({ id: obj['id'] || null, customerId: obj['customerId'] || null, dateOfReading: obj['dateOfReading'] || '', kindOfMeter: obj['kindOfMeter'] || 'UNBEKANNT', meterCount: parseFloat(obj['meterCount']) || 0, meterId: obj['meterId'] || '', substitute: obj['substitute'] === 'true', comment: obj['comment'] || null });
      }
    }
    return { customers, readings };
  }

  private parseXML(text: string): { customers: Partial<Customer>[]; readings: any[] } {
    const doc = new DOMParser().parseFromString(text, 'application/xml');
    if (doc.querySelector('parsererror')) throw new Error('Ungültiges XML-Format');
    const t = (el: Element, tag: string) => el.querySelector(tag)?.textContent ?? '';
    const customers: Partial<Customer>[] = [];
    const readings: any[] = [];
    doc.querySelectorAll('customer').forEach(el => customers.push({ id: t(el,'id') || null, firstName: t(el,'firstName') || '', lastName: t(el,'lastName') || '', gender: (t(el,'gender') || 'U') as any, birthDate: t(el,'birthDate') || null }));
    doc.querySelectorAll('reading').forEach(el => readings.push({ id: t(el,'id') || null, customerId: t(el,'customerId') || null, dateOfReading: t(el,'dateOfReading') || '', kindOfMeter: t(el,'kindOfMeter') || 'UNBEKANNT', meterCount: parseFloat(t(el,'meterCount')) || 0, meterId: t(el,'meterId') || '', substitute: t(el,'substitute') === 'true', comment: t(el,'comment') || null }));
    return { customers, readings };
  }

  resetDb(): void {
    if (!confirm('Datenbank wirklich zurücksetzen?')) return;
    this.rSvc.resetDatabase().subscribe({
      next: () => this.exportMsg = '✓ Datenbank zurückgesetzt.',
      error: () => this.exportErr = 'Fehler.',
    });
  }

  private re(r: Reading): any {
    return { id: r.id, customerId: r.customer?.id ?? null, dateOfReading: r.dateOfReading, kindOfMeter: r.kindOfMeter, meterCount: r.meterCount, meterId: r.meterId, substitute: r.substitute, comment: r.comment };
  }
  private dl(b: Blob, fn: string): void {
    const a = Object.assign(document.createElement('a'), { href: URL.createObjectURL(b), download: fn });
    a.click(); URL.revokeObjectURL(a.href);
  }
  private esc(s: string): string { return (s ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;'); }
  private today(): string { return new Date().toISOString().split('T')[0]; }
}