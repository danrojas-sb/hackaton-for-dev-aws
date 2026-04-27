import { Component, ChangeDetectionStrategy, inject, signal, output } from '@angular/core';
import { FacturaService } from '../../services/factura.service';
import { Factura } from '../../models/factura.model';

/**
 * Componente para cargar facturas desde un archivo CSV.
 * Parsea el CSV a JSON y envía POST /api/facturas/bulk.
 */
@Component({
  selector: 'app-carga-facturas',
  templateUrl: './carga-facturas.html',
  styleUrl: './carga-facturas.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CargaFacturasComponent {

  cargaExitosa = output<void>();

  private facturaService = inject(FacturaService);

  fileName = signal('');
  isLoading = signal(false);
  mensaje = signal('');
  isError = signal(false);

  private facturasParseadas: Partial<Factura>[] = [];

  /** Maneja la selección de archivo CSV. */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.fileName.set(file.name);
    this.mensaje.set('');

    const reader = new FileReader();
    reader.onload = () => {
      this.facturasParseadas = this.parseCsv(reader.result as string);
      this.mensaje.set(`${this.facturasParseadas.length} facturas listas para cargar`);
      this.isError.set(false);
    };
    reader.readAsText(file);
  }

  /** Envía las facturas parseadas al backend. */
  cargar(): void {
    if (this.facturasParseadas.length === 0) return;

    this.isLoading.set(true);
    this.mensaje.set('');

    this.facturaService.crearEnLote(this.facturasParseadas).subscribe({
      next: (result) => {
        this.mensaje.set(`${result.length} facturas cargadas exitosamente`);
        this.isError.set(false);
        this.isLoading.set(false);
        this.facturasParseadas = [];
        this.fileName.set('');
        this.cargaExitosa.emit();
      },
      error: (err) => {
        this.mensaje.set(`Error: ${JSON.stringify(err.error)}`);
        this.isError.set(true);
        this.isLoading.set(false);
      }
    });
  }

  /** Parsea un string CSV a un array de objetos. */
  private parseCsv(csv: string): Partial<Factura>[] {
    const lines = csv.trim().split('\n');
    if (lines.length < 2) return [];

    const headers = lines[0].split(',').map(h => h.trim());

    return lines.slice(1).map(line => {
      const values = line.split(',').map(v => v.trim());
      const obj: Record<string, string> = {};
      headers.forEach((header, i) => {
        obj[header] = values[i] ?? '';
      });
      return obj as unknown as Partial<Factura>;
    });
  }
}
