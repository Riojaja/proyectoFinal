import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import Swal from 'sweetalert2';

type PedidoRow = {
    idPedido: number;
    fechaOrden: string;
    nroOrden: string;
    producto: string;
    sku: string;
    cantidad: number;
    precioSnap: number;
    subtotalSnap: number;
    estadoPago: 'PENDIENTE' | 'PAGADO' | 'CANCELADO' | 'REEMBOLSADO';
    estadoEnvio: 'PROCESANDO' | 'ENVIADO' | 'ENTREGADO';
    clienteNombre: string;
    clienteTelefono: string;
    emailPublico: string;
    direccionEnvio: string;
    comisionPlataforma: number;
    montoPagarEstimado: number;
    numeroSeguimiento?: string;
    metodoEnvio?: string;
};

type MetodoEnvio = {
    idMetodoEnvio: number;
    nombre: string;
    descripcion?: string;
    tiempoEstimado?: string;
    estado?: boolean;
};

@Component({
    standalone: true,
    selector: 'app-vendedor-pedidos',
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './vendedor-pedidos.component.html',
    styleUrls: ['./vendedor-pedidos.component.scss'],
})
export class VendedorPedidosComponent implements OnInit {
    private http = inject(HttpClient);
    private fb = inject(FormBuilder);
    private cdr = inject(ChangeDetectorRef);
    private api = environment.apiUrl;

    loading = false;
    procesando = false; // Para prevenir doble clic en envío

    pedidos: PedidoRow[] = [];
    metodosEnvio: MetodoEnvio[] = [];

    filtros = this.fb.group({
        estadoPago: [''],
        estadoEnvio: [''],
        nroOrden: [''],
    });

    showDetalle = false;
    showPrepararEnvio = false;

    pedidoSel: PedidoRow | null = null;

    envioForm = this.fb.group({
        estadoOperativo: ['PROCESANDO'],
        numeroSeguimiento: [''],
        metodoEnvioId: [null as number | null],
    });

    ngOnInit(): void {
        this.cargarMetodosEnvio();
        this.cargarPedidos();
    }

    cargarMetodosEnvio() {
        this.http.get<MetodoEnvio[]>(`${this.api}/metodos-envio/activos`).subscribe({
            next: (res) => {
                this.metodosEnvio = res || [];
                if (this.metodosEnvio.length > 0 && !this.envioForm.value.metodoEnvioId) {
                    this.envioForm.patchValue({
                        metodoEnvioId: this.metodosEnvio[0].idMetodoEnvio
                    });
                }
                this.cdr.detectChanges(); // Forzar actualización de la vista
            },
            error: (err) => {
                console.error(err);
                this.metodosEnvio = [];
                this.cdr.detectChanges();
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: 'No se pudieron cargar los métodos de envío.',
                    timer: 3000,
                    showConfirmButton: false,
                });
            }
        });
    }

    cargarPedidos() {
        this.loading = true;
        this.cdr.detectChanges(); // Mostrar spinner inmediatamente

        this.http.get<PedidoRow[]>(`${this.api}/vendedores/pedidos`).subscribe({
            next: (res) => {
                this.pedidos = res || [];
                this.loading = false;
                this.cdr.detectChanges(); // Actualizar vista con los datos
            },
            error: (err) => {
                console.error(err);
                this.pedidos = [];
                this.loading = false;
                this.cdr.detectChanges();
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: 'No se pudo cargar los pedidos del vendedor.',
                    timer: 3000,
                    showConfirmButton: false,
                });
            }
        });
    }

    get pedidosFiltrados(): PedidoRow[] {
        const f = this.filtros.getRawValue();
        const pago = (f.estadoPago || '').toUpperCase();
        const envio = (f.estadoEnvio || '').toUpperCase();
        const nro = (f.nroOrden || '').toLowerCase().trim();

        return (this.pedidos || []).filter(p => {
            const okPago = !pago || p.estadoPago === pago;
            const okEnvio = !envio || p.estadoEnvio === envio;
            const okNro = !nro || p.nroOrden.toLowerCase().includes(nro);
            return okPago && okEnvio && okNro;
        });
    }

    get pedidosHoy(): number {
        const hoy = new Date();
        const dd = String(hoy.getDate()).padStart(2, '0');
        const mm = String(hoy.getMonth() + 1).padStart(2, '0');
        const yyyy = hoy.getFullYear();
        const hoyStr = `${dd}/${mm}/${yyyy}`;

        return this.pedidos.filter(p => p.fechaOrden === hoyStr).length;
    }

    get pendientesEnvio(): number {
        return this.pedidosFiltrados.filter(p => p.estadoEnvio === 'PROCESANDO').length;
    }

    get montoVentasHoy(): number {
        const hoy = new Date();
        const dd = String(hoy.getDate()).padStart(2, '0');
        const mm = String(hoy.getMonth() + 1).padStart(2, '0');
        const yyyy = hoy.getFullYear();
        const hoyStr = `${dd}/${mm}/${yyyy}`;

        return this.pedidos
            .filter(p => p.fechaOrden === hoyStr)
            .reduce((acc, p) => acc + Number(p.montoPagarEstimado || 0), 0);
    }

    badgePago(estado: string) {
        const e = (estado || '').toUpperCase();
        if (e === 'PAGADO') return 'badge bg-success-subtle text-success';
        if (e === 'PENDIENTE') return 'badge bg-secondary-subtle text-secondary';
        if (e === 'CANCELADO') return 'badge bg-danger-subtle text-danger';
        if (e === 'REEMBOLSADO') return 'badge bg-dark-subtle text-dark';
        return 'badge bg-secondary-subtle text-secondary';
    }

    badgeEnvio(estado: string) {
        const e = (estado || '').toUpperCase();
        if (e === 'PROCESANDO') return 'badge bg-warning-subtle text-warning';
        if (e === 'ENVIADO') return 'badge bg-primary-subtle text-primary';
        if (e === 'ENTREGADO') return 'badge bg-success-subtle text-success';
        return 'badge bg-secondary-subtle text-secondary';
    }

    verDetalle(p: PedidoRow) {
        this.pedidoSel = p;
        this.showDetalle = true;
        this.cdr.detectChanges();
    }

    cerrarDetalle() {
        this.showDetalle = false;
        this.pedidoSel = null;
        this.cdr.detectChanges();
    }

    prepararEnvio(p: PedidoRow) {
        if (this.procesando) return;

        if (p.estadoPago !== 'PAGADO') {
            Swal.fire({
                icon: 'warning',
                title: 'Pago pendiente',
                text: 'No puedes preparar el envío hasta que el pedido esté pagado.'
            });
            return;
        }

        this.pedidoSel = p;

        const metodoEncontrado = this.metodosEnvio.find(
            m => m.nombre === (p.metodoEnvio || '')
        );

        this.envioForm.reset({
            estadoOperativo: p.estadoEnvio || 'PROCESANDO',
            numeroSeguimiento: p.numeroSeguimiento || '',
            metodoEnvioId: metodoEncontrado
                ? metodoEncontrado.idMetodoEnvio
                : (this.metodosEnvio[0]?.idMetodoEnvio ?? null)
        });

        this.showPrepararEnvio = true;
        this.cdr.detectChanges();
    }

    cerrarPrepararEnvio() {
        this.showPrepararEnvio = false;
        this.cdr.detectChanges();
    }

    imprimirEtiqueta() {
        window.print();
    }

    marcarComoEnviado() {
        if (this.procesando) return;
        if (!this.pedidoSel) return;

        const v = this.envioForm.getRawValue();

        if (!v.metodoEnvioId) {
            Swal.fire({
                icon: 'warning',
                title: 'Método de envío requerido',
                text: 'Selecciona un método de envío.',
                timer: 2000,
                showConfirmButton: false,
            });
            return;
        }

        this.procesando = true;

        const payload = {
            estadoOperativo: v.estadoOperativo || 'PROCESANDO',
            numeroSeguimiento: v.numeroSeguimiento || '',
            metodoEnvioId: Number(v.metodoEnvioId),
        };

        this.http.put<any>(`${this.api}/vendedores/pedidos/${this.pedidoSel.idPedido}/envio`, payload).subscribe({
            next: (res) => {
                const metodo = this.metodosEnvio.find(
                    m => m.idMetodoEnvio === Number(payload.metodoEnvioId)
                );

                this.pedidoSel!.estadoEnvio = (res?.estadoEnvio || payload.estadoOperativo) as any;
                this.pedidoSel!.numeroSeguimiento = res?.numeroSeguimiento || payload.numeroSeguimiento;
                this.pedidoSel!.metodoEnvio = res?.metodoEnvio || metodo?.nombre || '';

                this.procesando = false;
                this.cerrarPrepararEnvio();
                this.cdr.detectChanges();

                Swal.fire({
                    icon: 'success',
                    title: 'Éxito',
                    text: 'Envío actualizado correctamente.',
                    timer: 2000,
                    showConfirmButton: false,
                });
            },
            error: (err) => {
                console.error(err);
                this.procesando = false;
                this.cdr.detectChanges();
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: 'No se pudo actualizar el envío.',
                    timer: 3000,
                    showConfirmButton: false,
                });
            }
        });
    }
}   