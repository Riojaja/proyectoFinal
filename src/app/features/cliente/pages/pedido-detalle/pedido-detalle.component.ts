import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';

import { environment } from '../../../../../environments/environment';
import {
  Disputa,
  DisputaMensaje,
  DisputaService,
  EstadoDisputa,
  RolEmisor
} from '../../../../core/services/disputa.service';

type DetalleItem = {
  idOrdenDetalle?: number;
  idVariante?: number;
  nombreProducto?: string;
  nombreVendedor?: string;
  metodoEnvio?: string;
  cantidad?: number;
  precioUnitarioSnapshot?: number;
  subtotal?: number;
  estadoEnvio?: string;
  numeroSeguimiento?: string;
  imagen?: string;
};

type PedidoDetalle = {
  idOrden: number;
  numeroOrden: string;
  total: number;
  subtotal?: number;
  impuesto?: number;
  fechaOrden?: string;
  estadoOrden?: string;
  colorEstado?: string;
  metodoPago?: string;
  nombreDestinatario?: string;
  direccionEntrega?: string;
  notas?: string;
  detalles?: DetalleItem[];
};

@Component({
  selector: 'app-pedido-detalle',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './pedido-detalle.component.html',
  styleUrls: ['./pedido-detalle.component.scss']
})
export class PedidoDetalleComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private disputaService = inject(DisputaService);
  private api = environment.apiUrl;

  cargando = true;
  pedido: PedidoDetalle | null = null;

  disputas: Disputa[] = [];
  disputaPorDetalle = new Map<number, Disputa>();

  showDisputa = false;
  cargandoDisputa = false;
  enviandoMensaje = false;

  selectedItem: DetalleItem | null = null;
  selectedDisputa: Disputa | null = null;
  mensajesDisputa: DisputaMensaje[] = [];

  disputaReply = {
    mensaje: '',
    adjuntoUrl: ''
  };

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!id || isNaN(id)) {
      this.cargando = false;
      return;
    }

    this.cargarDetalle(id);
  }

  cargarDetalle(id: number) {
    this.cargando = true;

    this.http.get<PedidoDetalle>(`${this.api}/ordenes/${id}`).subscribe({
      next: (res) => {
        this.pedido = res;
        this.cargando = false;
        this.cargarMisDisputas();
      },
      error: (err) => {
        this.cargando = false;

        const msg =
          typeof err?.error === 'string'
            ? err.error
            : err?.error?.message || 'No se pudo cargar el detalle del pedido.';

        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: msg
        });
      }
    });
  }

  cargarMisDisputas() {
    this.disputaService.misDisputas().subscribe({
      next: (res) => {
        this.disputas = Array.isArray(res) ? res : [];
        this.sincronizarDisputasPorDetalle();
      },
      error: () => {
        this.disputas = [];
        this.disputaPorDetalle.clear();
      }
    });
  }

  private sincronizarDisputasPorDetalle() {
    this.disputaPorDetalle.clear();

    const idsDetallePedido = new Set(
      (this.pedido?.detalles || [])
        .map(it => Number(it.idOrdenDetalle || 0))
        .filter(id => id > 0)
    );

    (this.disputas || []).forEach((d) => {
      const idDetalle = Number(d?.idOrdenDetalle || 0);
      if (!idDetalle || !idsDetallePedido.has(idDetalle)) return;

      const actual = this.disputaPorDetalle.get(idDetalle);
      if (!actual || Number(d.idDisputa) > Number(actual.idDisputa)) {
        this.disputaPorDetalle.set(idDetalle, d);
      }
    });
  }

  get items(): DetalleItem[] {
    return this.pedido?.detalles || [];
  }

  fechaTexto(): string {
    const fecha = this.pedido?.fechaOrden;
    if (!fecha) return '-';

    const f = new Date(fecha);
    if (isNaN(f.getTime())) return fecha;

    return f.toLocaleString('es-PE', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  estadoTexto(): string {
    return this.pedido?.estadoOrden || 'Confirmado';
  }

  metodoTexto(): string {
    return this.pedido?.metodoPago || 'No especificado';
  }

  totalTexto(): number {
    return Number(this.pedido?.total || 0);
  }

  subtotalTexto(): number {
    return Number(this.pedido?.subtotal || this.pedido?.total || 0);
  }

  itemNombre(it: DetalleItem): string {
    return it?.nombreProducto || 'Producto';
  }

  itemCantidad(it: DetalleItem): number {
    return Number(it?.cantidad || 0);
  }

  itemPrecio(it: DetalleItem): number {
    return Number(it?.precioUnitarioSnapshot || 0);
  }

  itemSubtotal(it: DetalleItem): number {
    if (it?.subtotal != null) return Number(it.subtotal);
    return this.itemCantidad(it) * this.itemPrecio(it);
  }

  img(it: DetalleItem) {
    const ruta = (it?.imagen || '').trim();

    if (!ruta) {
      return 'assets/img/no-image.png';
    }

    if (ruta.startsWith('http://') || ruta.startsWith('https://')) {
      return ruta;
    }

    return `${this.api}${ruta.startsWith('/') ? '' : '/'}${ruta}`;
  }

  cantidadTotal(): number {
    return this.items.reduce((acc, it) => acc + this.itemCantidad(it), 0);
  }

  estadoPasoActual(): number {
    const estado = this.estadoTexto().toUpperCase();

    if (estado.includes('ENTREGADO')) return 4;
    if (estado.includes('EN CAMINO') || estado.includes('ENVIADO')) return 3;
    if (estado.includes('PROCESANDO') || estado.includes('PREPARANDO')) return 2;
    return 1;
  }

  pasoActivo(paso: number): boolean {
    return this.estadoPasoActual() >= paso;
  }

  trackByItem(index: number, it: DetalleItem) {
    return it.idOrdenDetalle || it.idVariante || index;
  }

  puedeConfirmarRecepcion(): boolean {
    const estado = (this.estadoTexto() || '').toUpperCase();
    return estado.includes('ENVIADO') || estado.includes('EN CAMINO');
  }

  puedeResenar(): boolean {
    const estado = (this.estadoTexto() || '').toUpperCase();
    return estado.includes('ENTREGADO');
  }

  confirmarRecepcion() {
    if (!this.pedido?.idOrden) return;

    Swal.fire({
      icon: 'question',
      title: '¿Confirmas que recibiste tu pedido?',
      text: 'Esta acción marcará el pedido como entregado.',
      showCancelButton: true,
      confirmButtonText: 'Sí, lo recibí',
      cancelButtonText: 'Cancelar'
    }).then((r) => {
      if (!r.isConfirmed) return;

      this.http.put(`${this.api}/ordenes/${this.pedido!.idOrden}/estado?estado=ENTREGADO`, {})
        .subscribe({
          next: () => {
            Swal.fire({
              icon: 'success',
              title: 'Pedido entregado',
              text: 'Gracias por confirmar la recepción.'
            });
            this.cargarDetalle(this.pedido!.idOrden);
          },
          error: (err) => {
            const msg =
              typeof err?.error === 'string'
                ? err.error
                : err?.error?.message || 'No se pudo confirmar la entrega.';

            Swal.fire({
              icon: 'error',
              title: 'Error',
              text: msg
            });
          }
        });
    });
  }

  abrirResena() {
    let ratingSeleccionado = 0;

    Swal.fire({
      title: 'Deja tu reseña',
      html: `
      <div class="review-modal">
        <div class="review-stars" id="reviewStars">
          <button type="button" class="star-btn" data-value="1">★</button>
          <button type="button" class="star-btn" data-value="2">★</button>
          <button type="button" class="star-btn" data-value="3">★</button>
          <button type="button" class="star-btn" data-value="4">★</button>
          <button type="button" class="star-btn" data-value="5">★</button>
        </div>

        <div class="review-hint" id="reviewHint">Selecciona una calificación</div>

        <textarea
          id="comentario"
          class="review-textarea"
          placeholder="Cuéntanos qué te pareció el producto, la entrega o la experiencia..."
          maxlength="500"
        ></textarea>

        <div class="review-counter">
          <span id="reviewCounter">0</span>/500
        </div>
      </div>
    `,
      showCancelButton: true,
      confirmButtonText: 'Guardar reseña',
      cancelButtonText: 'Cancelar',
      customClass: {
        popup: 'review-popup',
        title: 'review-title',
        confirmButton: 'review-confirm-btn',
        cancelButton: 'review-cancel-btn'
      },
      buttonsStyling: false,
      didOpen: () => {
        const stars = Array.from(document.querySelectorAll('.star-btn')) as HTMLButtonElement[];
        const hint = document.getElementById('reviewHint') as HTMLElement | null;
        const comentario = document.getElementById('comentario') as HTMLTextAreaElement | null;
        const counter = document.getElementById('reviewCounter') as HTMLElement | null;

        const textos = [
          '',
          'Muy mala',
          'Mala',
          'Regular',
          'Buena',
          'Excelente'
        ];

        const pintar = (valor: number) => {
          stars.forEach((star, index) => {
            if (index < valor) {
              star.classList.add('active');
            } else {
              star.classList.remove('active');
            }
          });

          if (hint) {
            hint.textContent = valor > 0 ? `${valor}/5 - ${textos[valor]}` : 'Selecciona una calificación';
          }
        };

        stars.forEach((star) => {
          star.addEventListener('mouseenter', () => {
            const value = Number(star.dataset['value'] || 0);
            pintar(value);
          });

          star.addEventListener('click', () => {
            ratingSeleccionado = Number(star.dataset['value'] || 0);
            pintar(ratingSeleccionado);
          });
        });

        const contenedor = document.getElementById('reviewStars');
        contenedor?.addEventListener('mouseleave', () => pintar(ratingSeleccionado));

        comentario?.addEventListener('input', () => {
          if (counter && comentario) {
            counter.textContent = String(comentario.value.length);
          }
        });
      },
      preConfirm: () => {
        const comentario = (document.getElementById('comentario') as HTMLTextAreaElement | null)?.value?.trim() || '';

        if (ratingSeleccionado < 1 || ratingSeleccionado > 5) {
          Swal.showValidationMessage('Debes seleccionar una calificación de 1 a 5 estrellas.');
          return null;
        }

        if (comentario.length < 5) {
          Swal.showValidationMessage('Escribe un comentario un poco más detallado.');
          return null;
        }

        return {
          rating: ratingSeleccionado,
          comentario
        };
      }
    }).then((r) => {
      if (!r.isConfirmed || !r.value) return;

      const payload = {
        puntuacion: r.value.rating,
        comentario: r.value.comentario,
        idOrden: this.pedido?.idOrden
      };

      console.log('reseña a guardar', payload);

      Swal.fire({
        icon: 'success',
        title: 'Reseña registrada',
        text: 'Gracias por compartir tu opinión.'
      });
    });
  }

  obtenerDisputa(it: DetalleItem): Disputa | null {
    const idDetalle = Number(it?.idOrdenDetalle || 0);
    if (!idDetalle) return null;
    return this.disputaPorDetalle.get(idDetalle) || null;
  }

  puedeAbrirDisputa(it: DetalleItem): boolean {
    return this.puedeResenar() && !this.obtenerDisputa(it);
  }

  estadoDisputaTexto(estado: EstadoDisputa): string {
    switch (estado) {
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
        return estado;
    }
  }

  estadoDisputaClase(estado: EstadoDisputa): string {
    switch (estado) {
      case 'abierta':
        return 'text-bg-danger';
      case 'en_revision':
        return 'text-bg-warning';
      case 'en_negociacion':
        return 'text-bg-info';
      case 'resuelta_reembolso_total':
      case 'resuelta_reembolso_parcial':
        return 'text-bg-success';
      case 'resuelta_rechazada':
        return 'text-bg-secondary';
      case 'cerrada':
        return 'text-bg-dark';
      default:
        return 'text-bg-light';
    }
  }

  nombreRol(rol: RolEmisor): string {
    switch (rol) {
      case 'cliente':
        return 'Cliente';
      case 'vendedor':
        return 'Vendedor';
      case 'admin':
        return 'Admin';
      default:
        return rol;
    }
  }

  puedeResponderDisputa(disputa: Disputa | null): boolean {
    if (!disputa) return false;

    return ![
      'resuelta_reembolso_total',
      'resuelta_reembolso_parcial',
      'resuelta_rechazada',
      'cerrada'
    ].includes(disputa.estado);
  }

  abrirDisputa(it: DetalleItem) {
    if (!it?.idOrdenDetalle) return;

    Swal.fire({
      title: 'Abrir disputa',
      html: `
        <input id="motivo" class="swal2-input" placeholder="Motivo del reclamo">
        <textarea id="descripcion" class="swal2-textarea" placeholder="Describe qué ocurrió con el producto o pedido"></textarea>
        <input id="evidencia" class="swal2-input" placeholder="URLs de evidencia separadas por coma (opcional)">
      `,
      showCancelButton: true,
      confirmButtonText: 'Enviar disputa',
      cancelButtonText: 'Cancelar',
      preConfirm: () => {
        const motivo = (document.getElementById('motivo') as HTMLInputElement | null)?.value?.trim() || '';
        const descripcion = (document.getElementById('descripcion') as HTMLTextAreaElement | null)?.value?.trim() || '';
        const evidencia = (document.getElementById('evidencia') as HTMLInputElement | null)?.value?.trim() || '';

        if (!motivo) {
          Swal.showValidationMessage('Debes ingresar el motivo.');
          return null;
        }

        if (descripcion.length < 10) {
          Swal.showValidationMessage('La descripción debe tener al menos 10 caracteres.');
          return null;
        }

        const urls = evidencia
          ? evidencia.split(',').map(x => x.trim()).filter(Boolean)
          : [];

        return {
          idOrdenDetalle: it.idOrdenDetalle,
          motivo,
          descripcion,
          evidenciaJson: urls.length ? JSON.stringify(urls) : null
        };
      }
    }).then((r) => {
      if (!r.isConfirmed || !r.value) return;

      this.disputaService.abrir(r.value).subscribe({
        next: () => {
          Swal.fire({
            icon: 'success',
            title: 'Disputa creada',
            text: 'Tu reclamo fue enviado correctamente.'
          });
          this.cargarMisDisputas();
        },
        error: (err) => {
          const msg =
            typeof err?.error === 'string'
              ? err.error
              : err?.error?.message || 'No se pudo abrir la disputa.';

          Swal.fire({
            icon: 'error',
            title: 'Error',
            text: msg
          });
        }
      });
    });
  }

  verDisputa(it: DetalleItem) {
    const disputa = this.obtenerDisputa(it);
    if (!disputa) return;

    this.selectedItem = it;
    this.selectedDisputa = disputa;
    this.mensajesDisputa = [];
    this.disputaReply = { mensaje: '', adjuntoUrl: '' };
    this.showDisputa = true;
    this.cargandoDisputa = true;

    this.disputaService.obtener(disputa.idDisputa).subscribe({
      next: (detalle) => {
        this.selectedDisputa = detalle;

        this.disputaService.listarMensajes(detalle.idDisputa).subscribe({
          next: (mensajes) => {
            this.mensajesDisputa = mensajes || [];
            this.cargandoDisputa = false;
          },
          error: () => {
            this.mensajesDisputa = [];
            this.cargandoDisputa = false;
          }
        });
      },
      error: () => {
        this.cargandoDisputa = false;
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudo cargar la disputa.'
        });
      }
    });
  }

  cerrarDisputaModal() {
    this.showDisputa = false;
    this.cargandoDisputa = false;
    this.selectedItem = null;
    this.selectedDisputa = null;
    this.mensajesDisputa = [];
    this.disputaReply = { mensaje: '', adjuntoUrl: '' };
  }

  enviarMensajeDisputa() {
    if (!this.selectedDisputa || this.enviandoMensaje) return;

    const mensaje = this.disputaReply.mensaje.trim();
    if (!mensaje) return;

    const payload: { mensaje: string; adjuntoUrl?: string } = { mensaje };

    const adjuntoUrl = this.disputaReply.adjuntoUrl.trim();
    if (adjuntoUrl) {
      payload.adjuntoUrl = adjuntoUrl;
    }

    this.enviandoMensaje = true;

    this.disputaService.enviarMensaje(this.selectedDisputa.idDisputa, payload).subscribe({
      next: (msg) => {
        this.mensajesDisputa = [...this.mensajesDisputa, msg];
        this.disputaReply = { mensaje: '', adjuntoUrl: '' };
        this.enviandoMensaje = false;
      },
      error: (err) => {
        this.enviandoMensaje = false;

        const msg =
          typeof err?.error === 'string'
            ? err.error
            : err?.error?.message || 'No se pudo enviar el mensaje.';

        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: msg
        });
      }
    });
  }

  evidenciaItems(raw?: string | null): string[] {
    const valor = String(raw || '').trim();
    if (!valor) return [];

    try {
      const parsed = JSON.parse(valor);

      if (Array.isArray(parsed)) {
        return parsed.map((x) => String(x)).filter(Boolean);
      }

      if (parsed && typeof parsed === 'object') {
        if (Array.isArray((parsed as any).files)) {
          return (parsed as any).files.map((x: any) => String(x)).filter(Boolean);
        }
        if (Array.isArray((parsed as any).urls)) {
          return (parsed as any).urls.map((x: any) => String(x)).filter(Boolean);
        }
      }

      return [];
    } catch {
      return [];
    }
  }
}