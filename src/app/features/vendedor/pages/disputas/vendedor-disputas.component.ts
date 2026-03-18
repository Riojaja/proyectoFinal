import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';
import Swal from 'sweetalert2';


import {
    Disputa,
    DisputaMensaje,
    DisputaService,
    EstadoDisputa,
    RolEmisor,
} from '../../../../core/services/disputa.service';

@Component({
    standalone: true,
    selector: 'app-vendedor-disputas',
    imports: [CommonModule, FormsModule],
    templateUrl: './vendedor-disputas.component.html',
    styleUrls: ['./vendedor-disputas.component.scss'],
})
export class VendedorDisputasComponent implements OnInit {
    private disputaService = inject(DisputaService);
    private cdr = inject(ChangeDetectorRef);

    loading = false;
    loadingDetalle = false;
    enviando = false;
    errorDetalle: string | null = null;

    q = '';

    disputas: Disputa[] = [];
    mensajes: DisputaMensaje[] = [];

    selected: Disputa | null = null;
    showDetalle = false;

    draft = {
        mensaje: '',
        adjuntoUrl: '',
    };

    ngOnInit(): void {
        console.log('VendedorDisputasComponent inicializado');
        console.log('Servicio de disputas:', this.disputaService);
        this.cargar();
    }

    cargar() {
        console.log('Cargando disputas...');
        this.loading = true;

        this.disputaService.misDisputas().pipe(
            timeout(10000), // Timeout de 10 segundos
            catchError(err => {
                console.error('Error en misDisputas():', err);
                return of([]);
            })
        ).subscribe({
            next: (res) => {
                console.log('Respuesta de misDisputas:', res);

                if (Array.isArray(res)) {
                    this.disputas = res.sort(
                        (a, b) => Number(b.idDisputa) - Number(a.idDisputa)
                    );
                    console.log(`${this.disputas.length} disputas cargadas`);
                } else {
                    console.warn('La respuesta no es un array:', res);
                    this.disputas = [];
                }

                this.loading = false;
            },
            error: (err) => {
                console.error('Error en suscripción:', err);
                this.loading = false;

                let mensajeError = 'No se pudieron cargar las disputas.';

                if (err.status === 401) {
                    mensajeError = 'No autorizado. Por favor, inicia sesión nuevamente.';
                } else if (err.status === 403) {
                    mensajeError = 'No tienes permisos para ver las disputas.';
                } else if (err.status === 0) {
                    mensajeError = 'Error de conexión. Verifica tu conexión a internet.';
                }

                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: mensajeError,
                    footer: err.message || ''
                });
            },
        });
    }

    get filtered(): Disputa[] {
        const s = this.q.trim().toLowerCase();
        if (!s) return this.disputas;

        return this.disputas.filter((r) => {
            const campos = [
                r.idDisputa?.toString(),
                r.idOrden?.toString(),
                r.numeroOrden,
                r.idOrdenDetalle?.toString(),
                r.nombreProducto,
                r.sku,
                r.clienteNombre,
                r.usuarioEmail,
                r.motivo,
                r.descripcion,
                r.estado,
            ];

            return campos.some((v) => v && String(v).toLowerCase().includes(s));
        });
    }

    get abiertasCount() {
        return this.disputas.filter((x) => x.estado === 'abierta').length;
    }

    get revisionCount() {
        return this.disputas.filter((x) => x.estado === 'en_revision').length;
    }

    get negociacionCount() {
        return this.disputas.filter((x) => x.estado === 'en_negociacion').length;
    }

    get cerradasCount() {
        return this.disputas.filter(
            (x) =>
                x.estado === 'resuelta_reembolso_total' ||
                x.estado === 'resuelta_reembolso_parcial' ||
                x.estado === 'resuelta_rechazada' ||
                x.estado === 'cerrada'
        ).length;
    }

    badgeClass(e: EstadoDisputa) {
        switch (e) {
            case 'abierta':
                return 'badge bg-danger-subtle text-danger';
            case 'en_revision':
                return 'badge bg-warning-subtle text-warning';
            case 'en_negociacion':
                return 'badge bg-info-subtle text-info';
            case 'resuelta_reembolso_total':
            case 'resuelta_reembolso_parcial':
                return 'badge bg-success-subtle text-success';
            case 'resuelta_rechazada':
                return 'badge bg-secondary-subtle text-secondary';
            case 'cerrada':
                return 'badge bg-dark-subtle text-dark';
            default:
                return 'badge bg-light text-dark';
        }
    }

    estadoLabel(e: EstadoDisputa) {
        switch (e) {
            case 'abierta':
                return 'Abierta';
            case 'en_revision':
                return 'En revisión';
            case 'en_negociacion':
                return 'En negociación';
            case 'resuelta_reembolso_total':
                return 'Reembolso total';
            case 'resuelta_reembolso_parcial':
                return 'Reembolso parcial';
            case 'resuelta_rechazada':
                return 'Rechazada';
            case 'cerrada':
                return 'Cerrada';
            default:
                return e;
        }
    }

    nombreRol(rol: RolEmisor) {
        switch (rol) {
            case 'admin':
                return 'Admin';
            case 'cliente':
                return 'Cliente';
            case 'vendedor':
                return 'Vendedor';
            default:
                return rol;
        }
    }

    evidenciaItems(raw?: string | null): string[] {
        if (!raw) return [];

        try {
            // Si es string vacío
            if (raw.trim() === '') return [];

            // Intentar parsear JSON
            const parsed = JSON.parse(raw);

            if (Array.isArray(parsed)) {
                return parsed.filter(item => item && typeof item === 'string');
            }

            if (parsed && typeof parsed === 'object') {
                // Si tiene propiedad files
                if (Array.isArray(parsed.files)) {
                    return parsed.files.filter((f: any) => f && typeof f === 'string');
                }
                // Si tiene propiedad urls
                if (Array.isArray(parsed.urls)) {
                    return parsed.urls.filter((u: any) => u && typeof u === 'string');
                }
            }

            return [];
        } catch (e) {
            console.warn('Error parseando evidenciaJson:', e);
            // Si no es JSON válido, tratar como string
            if (typeof raw === 'string' && raw.startsWith('http')) {
                return [raw];
            }
            return [];
        }
    }

    abrirDetalle(row: Disputa) {
        console.log('===== ABRIR DETALLE ====');
        console.log('Datos de la fila:', row);
        console.log('ID de disputa:', row?.idDisputa);

        if (!row || !row.idDisputa) {
            console.error('Error: La disputa no tiene ID');
            Swal.fire('Error', 'La disputa no tiene un ID válido', 'error');
            return;
        }

        this.showDetalle = true;
        this.selected = { ...row };
        this.mensajes = [];
        this.draft = { mensaje: '', adjuntoUrl: '' };
        this.loadingDetalle = true;
        this.errorDetalle = null;
        this.cdr.detectChanges();

        console.log('Cargando detalle para disputa ID:', row.idDisputa);

        this.disputaService.obtener(row.idDisputa).pipe(
            timeout(10000),
            catchError(err => {
                console.error('Error en obtener():', err);
                return of(null);
            })
        ).subscribe({
            next: (detalle) => {
                console.log('Respuesta de obtener():', detalle);

                if (detalle && detalle.idDisputa) {
                    console.log('Detalle obtenido correctamente');
                    this.selected = detalle;

                    this.disputaService.listarMensajes(row.idDisputa).pipe(
                        timeout(10000),
                        catchError(err => {
                            console.error('Error en listarMensajes():', err);
                            return of([]);
                        })
                    ).subscribe({
                        next: (mensajes) => {
                            console.log('Respuesta de listarMensajes():', mensajes);

                            this.mensajes = Array.isArray(mensajes) ? mensajes : [];
                            this.loadingDetalle = false;
                            this.errorDetalle = null;

                            console.log('Proceso completado exitosamente');
                            this.cdr.detectChanges();
                        },
                        error: (err) => {
                            console.error('Error en suscripción de mensajes:', err);
                            this.mensajes = [];
                            this.loadingDetalle = false;
                            this.errorDetalle = 'Error al cargar los mensajes';
                            this.cdr.detectChanges();

                            Swal.fire({
                                icon: 'warning',
                                title: 'Atención',
                                text: 'Se cargó el detalle pero no los mensajes',
                                timer: 3000
                            });
                        }
                    });
                } else {
                    console.error('No se recibió detalle válido');
                    this.errorDetalle = 'No se pudo cargar el detalle de la disputa';
                    this.loadingDetalle = false;
                    this.cdr.detectChanges();

                    Swal.fire({
                        icon: 'error',
                        title: 'Error',
                        text: 'No se pudo cargar el detalle de la disputa'
                    });
                }
            },
            error: (err) => {
                console.error('Error en suscripción de detalle:', err);
                this.errorDetalle = 'Error al cargar la información de la disputa';
                this.loadingDetalle = false;
                this.cdr.detectChanges();

                let mensajeError = 'Error al cargar la disputa';
                if (err.status === 404) {
                    mensajeError = 'La disputa no existe';
                } else if (err.status === 403) {
                    mensajeError = 'No tienes permiso para ver esta disputa';
                }

                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: mensajeError
                });
            }
        });
    }

    cerrarDetalle() {
        this.showDetalle = false;
        this.selected = null;
        this.mensajes = [];
        this.loadingDetalle = false;
        this.errorDetalle = null;
        this.draft = { mensaje: '', adjuntoUrl: '' };
    }

    enviarMensaje() {
        if (!this.selected || this.enviando) return;

        const mensaje = this.draft.mensaje.trim();
        if (!mensaje) return;

        this.enviando = true;

        const payload: { mensaje: string; adjuntoUrl?: string } = {
            mensaje,
        };

        const adjuntoUrl = this.draft.adjuntoUrl.trim();
        if (adjuntoUrl) {
            payload.adjuntoUrl = adjuntoUrl;
        }

        console.log('Enviando mensaje:', payload);

        this.disputaService.enviarMensaje(this.selected.idDisputa, payload).pipe(
            timeout(10000),
            catchError(err => {
                console.error('Error al enviar mensaje:', err);
                return of(null);
            })
        ).subscribe({
            next: (msg) => {
                if (msg) {
                    console.log('Mensaje enviado:', msg);
                    this.mensajes = [...this.mensajes, msg];
                    this.draft = { mensaje: '', adjuntoUrl: '' };
                } else {
                    console.error('No se pudo enviar el mensaje');
                    Swal.fire('Error', 'No se pudo enviar el mensaje', 'error');
                }
                this.enviando = false;
            },
            error: (err) => {
                console.error(' Error en suscripción:', err);
                this.enviando = false;
                Swal.fire('Error', 'No se pudo enviar el mensaje', 'error');
            },
        });
    }
}