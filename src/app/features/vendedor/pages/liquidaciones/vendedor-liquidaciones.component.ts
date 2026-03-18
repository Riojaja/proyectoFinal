import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import Swal from 'sweetalert2';

type LiquidacionRow = {
    idLiquidacion: number;
    fechaCorte: string;
    montoTotalVentas: number;
    comisionRetenida: number;
    montoAPagar: number;
    estado: 'pagado' | 'pendiente' | 'procesando';
    comprobanteTransferencia?: string | null;
};

@Component({
    standalone: true,
    selector: 'app-vendedor-liquidaciones',
    imports: [CommonModule],
    templateUrl: './vendedor-liquidaciones.component.html',
    styleUrls: ['./vendedor-liquidaciones.component.scss'],
})
export class VendedorLiquidacionesComponent implements OnInit {
    private http = inject(HttpClient);
    private cdr = inject(ChangeDetectorRef);
    private api = environment.apiUrl;

    loading = false;
    liquidaciones: LiquidacionRow[] = [];

    ngOnInit(): void {
        this.cargar();
    }

    cargar() {
        if (this.loading) return; // Prevención de doble clic
        this.loading = true;
        this.cdr.detectChanges(); // Mostrar spinner inmediatamente

        this.http.get<LiquidacionRow[]>(`${this.api}/liquidaciones/mis-liquidaciones`).subscribe({
            next: (res) => {
                this.liquidaciones = res || [];
                this.loading = false;
                this.cdr.detectChanges(); // Actualizar vista con los datos
            },
            error: (err) => {
                console.error(err);
                this.liquidaciones = [];
                this.loading = false;
                this.cdr.detectChanges();
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: 'No se pudieron cargar tus liquidaciones.',
                    timer: 3000,
                    showConfirmButton: false,
                });
            }
        });
    }

    refrescar() {
        this.cargar();
    }

    get ventasGlobales(): number {
        return this.liquidaciones.reduce((a, x) => a + Number(x.montoTotalVentas || 0), 0);
    }

    get comisionTotal(): number {
        return this.liquidaciones.reduce((a, x) => a + Number(x.comisionRetenida || 0), 0);
    }

    get proximoPago(): string {
        const p = this.liquidaciones.find(x => x.estado !== 'pagado');
        return p ? `Estimado ${p.fechaCorte}` : 'Sin pagos pendientes';
    }

    badgeEstado(e: string) {
        const s = (e || '').toLowerCase();
        if (s === 'pagado') return 'badge bg-success-subtle text-success';
        if (s === 'procesando') return 'badge bg-primary-subtle text-primary';
        return 'badge bg-warning-subtle text-warning';
    }

    descargar(liq: LiquidacionRow) {
        if (!liq.comprobanteTransferencia) {
            Swal.fire({
                icon: 'info',
                title: 'Sin comprobante',
                text: 'Esta liquidación no tiene comprobante disponible.',
                timer: 2000,
                showConfirmButton: false,
            });
            return;
        }
        window.open(liq.comprobanteTransferencia, '_blank');
    }

    verDocumento(liq: LiquidacionRow) {
        if (!liq.comprobanteTransferencia) {
            Swal.fire({
                icon: 'info',
                title: 'Sin documento',
                text: 'No hay documento disponible para esta liquidación.',
                timer: 2000,
                showConfirmButton: false,
            });
            return;
        }
        window.open(liq.comprobanteTransferencia, '_blank');
    }
}