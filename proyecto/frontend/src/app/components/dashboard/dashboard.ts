import { Component, ChangeDetectionStrategy, inject, signal, OnInit } from '@angular/core';
import { FacturaService } from '../../services/factura.service';
import { Factura, PageResponse, EstadoResumen } from '../../models/factura.model';
import { FacturaTableComponent } from '../factura-table/factura-table';
import { EstadosChartComponent } from '../estados-chart/estados-chart';
import { CargaFacturasComponent } from '../carga-facturas/carga-facturas';

/**
 * Dashboard principal que integra tabla de facturas, diagrama de torta y carga CSV.
 */
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FacturaTableComponent, EstadosChartComponent, CargaFacturasComponent],
})
export class DashboardComponent implements OnInit {

  private facturaService = inject(FacturaService);

  page = signal<PageResponse<Factura>>({
    content: [], totalElements: 0, totalPages: 0, number: 0, size: 20
  });

  resumen = signal<EstadoResumen>({
    PENDIENTE: 0, PROCESO: 0, TERMINADO: 0, ERROR: 0
  });

  ngOnInit(): void {
    this.cargarDatos();
  }

  /** Carga tabla y resumen de estados. */
  cargarDatos(page = 0): void {
    this.facturaService.listar(page).subscribe(p => this.page.set(p));
    this.facturaService.resumenEstados().subscribe(r => this.resumen.set(r));
  }

  /** Callback cuando cambia la página de la tabla. */
  onPageChange(page: number): void {
    this.facturaService.listar(page).subscribe(p => this.page.set(p));
  }

  /** Callback cuando se carga un lote exitosamente. */
  onCargaExitosa(): void {
    this.cargarDatos(this.page().number);
  }
}
