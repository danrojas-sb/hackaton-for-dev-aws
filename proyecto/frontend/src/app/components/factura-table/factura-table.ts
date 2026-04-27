import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { Factura, PageResponse } from '../../models/factura.model';

/**
 * Tabla paginada de facturas con badges de color por estado.
 */
@Component({
  selector: 'app-factura-table',
  templateUrl: './factura-table.html',
  styleUrl: './factura-table.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DecimalPipe],
})
export class FacturaTableComponent {

  page = input.required<PageResponse<Factura>>();
  pageChange = output<number>();

  /** Retorna la clase CSS del badge según el estado. */
  badgeClass(estado: string): string {
    const map: Record<string, string> = {
      PENDIENTE: 'badge-pendiente',
      PROCESO: 'badge-proceso',
      TERMINADO: 'badge-terminado',
      ERROR: 'badge-error',
    };
    return map[estado] ?? '';
  }

  /** Navega a la página anterior. */
  prevPage(): void {
    const current = this.page().number;
    if (current > 0) {
      this.pageChange.emit(current - 1);
    }
  }

  /** Navega a la página siguiente. */
  nextPage(): void {
    const p = this.page();
    if (p.number < p.totalPages - 1) {
      this.pageChange.emit(p.number + 1);
    }
  }
}
