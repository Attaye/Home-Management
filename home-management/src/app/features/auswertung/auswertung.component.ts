import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Chart, registerables } from 'chart.js';
import { ReadingService } from '../../core/services/reading.service';
import { CustomerService } from '../../core/services/customer.service';
import { Reading } from '../../core/models/reading.model';
import { Customer, getFullName } from '../../core/models/customer.model';

Chart.register(...registerables);

@Component({
    selector: 'app-auswertung',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    templateUrl: './auswertung.component.html',
    styleUrls: ['./auswertung.component.scss'],
})
export class AuswertungComponent implements OnInit, AfterViewInit {

    @ViewChild('chartVerteilung') chartVerteilungRef!: ElementRef;
    @ViewChild('chartVerlauf')    chartVerlaufRef!:    ElementRef;
    @ViewChild('chartKunden')     chartKundenRef!:     ElementRef;

    loading    = true;
    error      = '';
    readings:  Reading[]  = [];
    customers: Customer[] = [];

    selectedCustomer = '';
    selectedKind: 'STROM' | 'WASSER' | 'HEIZUNG' | '' = '';

    private charts: Chart[] = [];

    // Statistiken
    stats = { strom: 0, wasser: 0, heizung: 0, unbekannt: 0, schaetzungen: 0 };

    constructor(
        private rSvc: ReadingService,
        private cSvc: CustomerService,
    ) {}

    ngOnInit(): void {
        forkJoin({
            readings:  this.rSvc.getFiltered(),
            customers: this.cSvc.getAll(),
        }).subscribe({
            next: ({ readings, customers }) => {
                this.readings  = readings;
                this.customers = customers;
                this.calcStats();
                this.loading = false;
            },
            error: () => { this.error = 'Fehler beim Laden.'; this.loading = false; },
        });
    }

    ngAfterViewInit(): void {}

    ngAfterViewChecked(): void {
        if (!this.loading && this.charts.length === 0 && this.readings.length > 0) {
            setTimeout(() => this.buildCharts(), 100);
        }
    }

    private calcStats(): void {
        this.stats = {
            strom:       this.readings.filter(r => r.kindOfMeter === 'STROM').length,
            wasser:      this.readings.filter(r => r.kindOfMeter === 'WASSER').length,
            heizung:     this.readings.filter(r => r.kindOfMeter === 'HEIZUNG').length,
            unbekannt:   this.readings.filter(r => r.kindOfMeter === 'UNBEKANNT').length,
            schaetzungen: this.readings.filter(r => r.substitute).length,
        };
    }

    private buildCharts(): void {
        if (!this.chartVerteilungRef || !this.chartVerlaufRef || !this.chartKundenRef) return;
        this.destroyCharts();

        this.buildVerteilungChart();
        this.buildVerlaufChart();
        this.buildKundenChart();
    }

    // ── Chart 1: Verteilung (Donut) ───────────────────────────────────────
    private buildVerteilungChart(): void {
        const ctx = this.chartVerteilungRef.nativeElement.getContext('2d');
        this.charts.push(new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Strom', 'Wasser', 'Heizung', 'Unbekannt'],
                datasets: [{
                    data: [this.stats.strom, this.stats.wasser, this.stats.heizung, this.stats.unbekannt],
                    backgroundColor: ['#f59e0b', '#06b6d4', '#ef4444', '#8b95a8'],
                    borderColor: '#161b22', borderWidth: 3,
                }],
            },
            options: {
                responsive: true, maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom', labels: { color: '#e8edf4', padding: 16, font: { size: 12 } } },
                    title:  { display: true, text: 'Ablesungen nach Zählerart', color: '#e8edf4', font: { size: 14 } },
                },
            },
        }));
    }

    // ── Chart 2: Verlauf über Zeit (Line) ────────────────────────────────
    private buildVerlaufChart(): void {
        const ctx = this.chartVerlaufRef.nativeElement.getContext('2d');

        const filterKind = this.selectedKind || 'STROM';
        let filtered = this.readings.filter(r => r.kindOfMeter === filterKind);
        if (this.selectedCustomer) {
            filtered = filtered.filter(r => r.customer?.id === this.selectedCustomer);
        }

        const sorted = filtered
            .slice().sort((a, b) => a.dateOfReading.localeCompare(b.dateOfReading))
            .slice(-24); // letzte 24 Einträge

        const unit = filterKind === 'WASSER' ? 'm³' : 'kWh';
        const color = filterKind === 'STROM' ? '#f59e0b' : filterKind === 'WASSER' ? '#06b6d4' : '#ef4444';

        this.charts.push(new Chart(ctx, {
            type: 'line',
            data: {
                labels: sorted.map(r => r.dateOfReading),
                datasets: [{
                    label: `Zählerstand (${unit})`,
                    data: sorted.map(r => r.meterCount),
                    borderColor: color,
                    backgroundColor: color + '22',
                    fill: true, tension: 0.3,
                    pointBackgroundColor: color, pointRadius: 4,
                }],
            },
            options: {
                responsive: true, maintainAspectRatio: false,
                plugins: {
                    legend: { labels: { color: '#e8edf4' } },
                    title:  { display: true, text: `${filterKind} – Verlauf`, color: '#e8edf4', font: { size: 14 } },
                },
                scales: {
                    x: { ticks: { color: '#8b95a8', maxTicksLimit: 8 }, grid: { color: '#2a3040' } },
                    y: { ticks: { color: '#8b95a8' }, grid: { color: '#2a3040' } },
                },
            },
        }));
    }

    // ── Chart 3: Top Kunden nach Ablesungen (Bar) ─────────────────────────
    private buildKundenChart(): void {
        const ctx = this.chartKundenRef.nativeElement.getContext('2d');
        const customerMap = new Map(this.customers.map(c => [c.id, c]));

        const counts = new Map<string, number>();
        this.readings.forEach(r => {
            if (r.customer?.id) counts.set(r.customer.id, (counts.get(r.customer.id) ?? 0) + 1);
        });

        const top10 = [...counts.entries()]
            .sort((a, b) => b[1] - a[1]).slice(0, 10);

        const labels = top10.map(([id]) => {
            const c = customerMap.get(id);
            return c ? `${c.firstName} ${c.lastName}` : id.substring(0, 8);
        });

        this.charts.push(new Chart(ctx, {
            type: 'bar',
            data: {
                labels,
                datasets: [{
                    label: 'Anzahl Ablesungen',
                    data: top10.map(([, count]) => count),
                    backgroundColor: '#3b82f6aa',
                    borderColor: '#3b82f6',
                    borderWidth: 1,
                }],
            },
            options: {
                responsive: true, maintainAspectRatio: false,
                indexAxis: 'y',
                plugins: {
                    legend: { labels: { color: '#e8edf4' } },
                    title:  { display: true, text: 'Top 10 Kunden nach Ablesungen', color: '#e8edf4', font: { size: 14 } },
                },
                scales: {
                    x: { ticks: { color: '#8b95a8' }, grid: { color: '#2a3040' } },
                    y: { ticks: { color: '#e8edf4', font: { size: 11 } }, grid: { color: '#2a3040' } },
                },
            },
        }));
    }

    onFilterChange(): void {
        this.destroyCharts();
        setTimeout(() => this.buildCharts(), 50);
    }

    private destroyCharts(): void {
        this.charts.forEach(c => c.destroy());
        this.charts = [];
    }

    getCustomerName(c: Customer): string { return getFullName(c); }
}