import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Factura, PageResponse, EstadoResumen } from '../models/factura.model';

/**
 * Servicio para consumir los endpoints REST del microservicio Centralizador.
 */
@Injectable({ providedIn: 'root' })
export class FacturaService {

  private http = inject(HttpClient);
  private readonly baseUrl = '/api/facturas';

  /**
   * Lista facturas de forma paginada.
   * @param page número de página (0-indexed)
   * @param size cantidad de registros por página
   * @returns Observable con la página de facturas
   */
  listar(page = 0, size = 20): Observable<PageResponse<Factura>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<PageResponse<Factura>>(this.baseUrl, { params });
  }

  /**
   * Obtiene el resumen de conteo de facturas por estado.
   * @returns Observable con mapa {PENDIENTE: N, PROCESO: N, ...}
   */
  resumenEstados(): Observable<EstadoResumen> {
    return this.http.get<EstadoResumen>(`${this.baseUrl}/estados/resumen`);
  }

  /**
   * Crea facturas en lote.
   * @param facturas lista de facturas a crear
   * @returns Observable con las facturas creadas
   */
  crearEnLote(facturas: Partial<Factura>[]): Observable<Factura[]> {
    return this.http.post<Factura[]>(`${this.baseUrl}/bulk`, facturas);
  }
}
