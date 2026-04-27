import { Component, ChangeDetectionStrategy, input, computed } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';
import { EstadoResumen } from '../../models/factura.model';

/**
 * Diagrama de torta con el indicador de estados de facturas.
 */
@Component({
  selector: 'app-estados-chart',
  templateUrl: './estados-chart.html',
  styleUrl: './estados-chart.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [BaseChartDirective],
})
export class EstadosChartComponent {

  resumen = input.required<EstadoResumen>();

  private readonly colores: Record<string, string> = {
    PENDIENTE: '#FFC107',
    PROCESO: '#2196F3',
    TERMINADO: '#4CAF50',
    ERROR: '#F44336',
  };

  chartData = computed<ChartData<'pie'>>(() => {
    const data = this.resumen();
    const labels = Object.keys(data);
    const values = Object.values(data);
    return {
      labels,
      datasets: [{
        data: values,
        backgroundColor: labels.map(l => this.colores[l] ?? '#999'),
      }],
    };
  });

  chartOptions: ChartOptions<'pie'> = {
    responsive: true,
    plugins: {
      legend: { position: 'bottom' },
      tooltip: {
        callbacks: {
          label: (ctx) => {
            const total = (ctx.dataset.data as number[]).reduce((a, b) => a + b, 0);
            const value = ctx.parsed;
            const pct = total > 0 ? ((value / total) * 100).toFixed(1) : '0';
            return `${ctx.label}: ${value} (${pct}%)`;
          }
        }
      }
    }
  };
}
