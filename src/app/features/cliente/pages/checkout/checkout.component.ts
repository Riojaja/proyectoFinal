import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import {
  FormBuilder,
  FormsModule,
  ReactiveFormsModule,
  Validators,
  FormGroup
} from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import Swal from 'sweetalert2';

import { environment } from '../../../../../environments/environment';
import { CartService, CartItem } from '../../../../core/services/cart.service';
import { SessionService } from '../../../../core/services/session.service';

type Direccion = {
  idDireccion: number;
  nombreDestinatario: string;
  telefono: string;
  direccionLinea1: string;
  direccionLinea2?: string;
  ubigeo: {
    codigoUbigeo: string;
    departamento: string;
    provincia: string;
    distrito: string;
  };
  esPrincipal?: boolean;
};

type Ubigeo = {
  codigoUbigeo: string;
  departamento: string;
  provincia: string;
  distrito: string;
};

type MetodoPago = {
  idMetodoPago: number;
  nombre: string;
  descripcion?: string;
  tipo: string;
  activo: boolean;
};

type Orden = {
  idOrden: number;
  numeroOrden: string;
  total: number;
  subtotal: number;
  impuesto: number;
  descuento?: number;
  costoEnvio?: number;
  estadoOrden?: string;
  colorEstado?: string;
  nombreDestinatario?: string;
  direccionEntrega?: string;
  notas?: string;
  fechaOrden?: string;
  detalles?: any[];
  items?: any[];
};

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule],
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.scss']
})
export class CheckoutComponent implements OnInit {
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);
  private cart = inject(CartService);
  private session = inject(SessionService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  private api = environment.apiUrl;

  cargando = true;
  procesando = false;

  items: CartItem[] = [];
  idUsuario: number | null = null;

  direcciones: Direccion[] = [];
  direccionSel: number | null = null;
  creandoDireccion = false;

  departamentos: string[] = [];
  provincias: string[] = [];
  distritos: Ubigeo[] = [];

  orden: Orden | null = null;
  metodosPago: MetodoPago[] = [];
  pagoOk = false;

  modalYape = false;
  modalPlin = false;
  modalPaypal = false;
  modalTarjeta = false;
  modalTransferencia = false;

  formOrden!: FormGroup;
  formDireccion!: FormGroup;
  formPago!: FormGroup;

  formYape!: FormGroup;
  formTarjeta!: FormGroup;
  formPlin!: FormGroup;
  formTransferencia!: FormGroup;

  paypalEmail = '';

  constructor() {
    this.formOrden = this.fb.group({
      notas: ['']
    });

    this.formDireccion = this.fb.group({
      nombreDestinatario: ['', [Validators.required, Validators.maxLength(150)]],
      telefono: ['', [Validators.required, Validators.maxLength(20)]],
      direccionLinea1: ['', [Validators.required, Validators.maxLength(255)]],
      direccionLinea2: [''],
      departamento: ['', Validators.required],
      provincia: ['', Validators.required],
      codigoUbigeo: ['', Validators.required],
      esPrincipal: [true]
    });

    this.formPago = this.fb.group({
      idMetodo: [null, Validators.required],
      nroOperacion: ['']
    });

    this.formYape = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      telefono: ['', [Validators.required, Validators.minLength(9), Validators.maxLength(9)]],
      codigo: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]]
    });

    this.formPlin = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      telefono: ['', [Validators.required, Validators.minLength(9), Validators.maxLength(9)]],
      codigo: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]]
    });

    this.formTarjeta = this.fb.group({
      numero: ['', [Validators.required, Validators.minLength(13), Validators.maxLength(19)]],
      titular: ['', [Validators.required, Validators.maxLength(120)]],
      mes: ['', Validators.required],
      anio: ['', Validators.required],
      cvv: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(4)]],
      guardarTarjeta: [true]
    });

    this.formTransferencia = this.fb.group({
      banco: ['BCP', Validators.required],
      titular: ['', [Validators.required, Validators.maxLength(120)]],
      nroOperacion: ['', [Validators.required, Validators.minLength(6)]]
    });


  }

  ngOnInit(): void {
    this.items = this.cart.getCheckoutItems();

    if (!this.items || this.items.length === 0) {
      this.router.navigateByUrl('/cliente/carrito');
      return;
    }

    this.session.ensure().subscribe({
      next: (s) => {
        this.idUsuario = s?.idUsuario ?? null;

        if (!this.idUsuario) {
          this.router.navigateByUrl('/cliente/carrito');
          return;
        }

        this.cargarDirecciones();
        this.cargarUbigeosBase();
        this.cargarMetodosPago();
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.router.navigateByUrl('/cliente/carrito');
      }
    });
  }

  img(it: CartItem) {
    const fallback = 'assets/img/no-image.png';
    const imagen = (it?.imagen || '').trim();

    if (!imagen) return fallback;

    if (imagen.startsWith('http://') || imagen.startsWith('https://')) {
      return imagen;
    }

    const base = this.api.replace(/\/api$/, '');

    if (imagen.startsWith('/')) {
      return `${base}${imagen}`;
    }

    return `${base}/${imagen}`;
  }

  total() {
    return this.items.reduce((a, x) => {
      const precio = Number(x.precioOferta ?? x.precio ?? 0);
      return a + precio * Number(x.cantidad || 1);
    }, 0);
  }

  subtotal() {
    return this.total();
  }

  envio() {
    return 0;
  }

  totalFinal() {
    return this.subtotal() + this.envio();
  }

  cargarDirecciones() {
    if (!this.idUsuario) return;

    this.http.get<Direccion[]>(`${this.api}/direcciones/usuario/${this.idUsuario}`).subscribe({
      next: (d) => {
        this.direcciones = d || [];
        const principal = this.direcciones.find(x => x.esPrincipal) || this.direcciones[0];
        this.direccionSel = principal?.idDireccion ?? null;
        this.cdr.detectChanges();
      },
      error: () => {
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudieron cargar las direcciones.'
        });
      }
    });
  }

  toggleCrearDireccion() {
    this.creandoDireccion = !this.creandoDireccion;
  }

  guardarDireccion() {
    if (!this.idUsuario) return;

    if (this.formDireccion.invalid) {
      this.formDireccion.markAllAsTouched();
      Swal.fire({
        icon: 'warning',
        title: 'Campos incompletos',
        text: 'Completa la dirección.'
      });
      return;
    }

    const v: any = this.formDireccion.getRawValue();

    const body: any = {
      nombreDestinatario: v.nombreDestinatario,
      telefono: v.telefono,
      direccionLinea1: v.direccionLinea1,
      direccionLinea2: v.direccionLinea2,
      codigoUbigeo: v.codigoUbigeo,
      esPrincipal: !!v.esPrincipal
    };

    this.http.post(
      `${this.api}/direcciones/usuario/${this.idUsuario}/ubigeo/${v.codigoUbigeo}`,
      body
    ).subscribe({
      next: () => {
        this.creandoDireccion = false;
        this.formDireccion.reset({
          nombreDestinatario: '',
          telefono: '',
          direccionLinea1: '',
          direccionLinea2: '',
          departamento: '',
          provincia: '',
          codigoUbigeo: '',
          esPrincipal: true
        });
        this.provincias = [];
        this.distritos = [];
        this.cargarDirecciones();

        Swal.fire({
          icon: 'success',
          title: 'Dirección guardada',
          timer: 1500,
          showConfirmButton: false
        });
      },
      error: (err) => {
        const msg =
          typeof err?.error === 'string'
            ? err.error
            : err?.error?.message || 'No se pudo guardar la dirección';

        Swal.fire({
          icon: 'error',
          title: 'No se pudo guardar la dirección',
          text: msg
        });

        console.error('Error guardar dirección:', err);
      }
    });
  }

  cargarUbigeosBase() {
    this.http.get<string[]>(`${this.api}/ubigeos/departamentos`).subscribe({
      next: (d) => {
        this.departamentos = d || [];
      },
      error: () => {
        this.departamentos = [];
      }
    });
  }

  onDepartamento() {
    const dep = this.formDireccion.value.departamento;

    this.provincias = [];
    this.distritos = [];
    this.formDireccion.patchValue({
      provincia: '',
      codigoUbigeo: ''
    });

    if (!dep) return;

    this.http.get<string[]>(`${this.api}/ubigeos/provincias/${encodeURIComponent(dep)}`).subscribe({
      next: (p) => {
        this.provincias = p || [];
      },
      error: () => {
        this.provincias = [];
      }
    });
  }

  onProvincia() {
    const dep = this.formDireccion.value.departamento;
    const prov = this.formDireccion.value.provincia;

    this.distritos = [];
    this.formDireccion.patchValue({ codigoUbigeo: '' });

    if (!dep || !prov) return;

    this.http.get<Ubigeo[]>(
      `${this.api}/ubigeos/distritos/${encodeURIComponent(dep)}/${encodeURIComponent(prov)}`
    ).subscribe({
      next: (d) => {
        this.distritos = d || [];
      },
      error: () => {
        this.distritos = [];
      }
    });
  }

  cargarMetodosPago() {
    this.http.get<MetodoPago[]>(`${this.api}/pagos/metodos`).subscribe({
      next: (data) => {
        this.metodosPago = (data || []).filter(m => m?.activo !== false);

        if (this.metodosPago.length > 0) {
          const actual = Number(this.formPago.get('idMetodo')?.value);
          const existe = this.metodosPago.some(m => Number(m.idMetodoPago) === actual);

          if (!existe) {
            this.formPago.patchValue({ idMetodo: this.metodosPago[0].idMetodoPago });
          }
        }
      },
      error: () => {
        this.metodosPago = [];
        Swal.fire({
          icon: 'error',
          title: 'No se pudieron cargar los métodos de pago'
        });
      }
    });
  }

  private normalizarTexto(valor: string | undefined | null): string {
    return String(valor || '')
      .trim()
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }

  private esMetodo(m: MetodoPago, clave: string): boolean {
    const nombre = this.normalizarTexto(m.nombre);
    const tipo = this.normalizarTexto(m.tipo);
    const buscado = this.normalizarTexto(clave);

    return nombre === buscado || tipo === buscado;
  }

  private metodoActual(): MetodoPago | undefined {
    const id = Number(this.formPago.value.idMetodo);
    return this.metodosPago.find(m => Number(m.idMetodoPago) === id);
  }

  private generarNroOperacion(prefijo: string) {
    const random = Math.floor(Math.random() * 100000);
    return `${prefijo}-${Date.now()}-${random}`;
  }

  private guardarMetodoTemporal() {
    try {
      sessionStorage.setItem('checkout_metodo_pago', this.metodoPagoTexto());
    } catch {
      // ignorar
    }
  }

  nombreMetodo(tipo: string) {
    const t = this.normalizarTexto(tipo);

    if (t === 'billetera_digital') return 'Billetera digital';
    if (t === 'paypal') return 'PayPal';
    if (t === 'tarjeta') return 'Tarjeta';
    if (t === 'transferencia') return 'Transferencia';
    return tipo;
  }

  descripcionMetodo(tipo: string) {
    const t = this.normalizarTexto(tipo);

    if (t === 'billetera_digital') return 'Pago rápido desde tu celular';
    if (t === 'paypal') return 'Paga con tu cuenta PayPal';
    if (t === 'tarjeta') return 'Visa, Mastercard o American Express';
    if (t === 'transferencia') return 'BCP, BBVA, Interbank, Scotiabank';
    return 'Método disponible';
  }

  iconoMetodo(tipo: string, nombre?: string) {
    const t = this.normalizarTexto(tipo);
    const n = this.normalizarTexto(nombre);

    if (n === 'yape' || n === 'plin') return 'bi-phone';
    if (t === 'paypal' || n === 'paypal') return 'bi-paypal';
    if (t === 'tarjeta') return 'bi-credit-card';
    if (t === 'transferencia') return 'bi-bank';
    if (t === 'billetera_digital') return 'bi-wallet2';
    return 'bi-wallet2';
  }

  seleccionarMetodo(idMetodo: number) {
    this.formPago.patchValue({ idMetodo });
  }

  metodoSeleccionado(idMetodo: number) {
    return Number(this.formPago.get('idMetodo')?.value) === Number(idMetodo);
  }

  crearOrden() {
    if (this.procesando) return;

    if (!this.direccionSel) {
      Swal.fire({
        icon: 'warning',
        title: 'Selecciona una dirección'
      });
      return;
    }

    if (!this.formPago.value.idMetodo) {
      Swal.fire({
        icon: 'warning',
        title: 'Selecciona un método de pago'
      });
      return;
    }

    if (!this.items || this.items.length === 0) {
      Swal.fire({
        icon: 'warning',
        title: 'No hay productos para procesar'
      });
      return;
    }

    this.procesando = true;

    const payload: any = {
      idDireccion: this.direccionSel,
      notas: this.formOrden.value.notas || null,
      items: this.items.map(it => ({
        idVariante: it.idVariante,
        cantidad: it.cantidad
      }))
    };

    this.http.post<Orden>(`${this.api}/ordenes/checkout`, payload).subscribe({
      next: (o) => {
        this.orden = o;
        this.procesando = false;

        Swal.fire({
          icon: 'success',
          title: 'Orden creada',
          text: o.numeroOrden,
          timer: 1500,
          showConfirmButton: false
        });
      },
      error: (err) => {
        this.procesando = false;

        const msg =
          typeof err?.error === 'string'
            ? err.error
            : err?.error?.message || 'No se pudo crear la orden';

        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: msg
        });
      }
    });
  }

  abrirPago() {
    if (!this.orden) {
      Swal.fire({
        icon: 'warning',
        title: 'Primero crea la orden'
      });
      return;
    }

    const metodo = this.metodoActual();
    if (!metodo) {
      Swal.fire({
        icon: 'warning',
        title: 'Selecciona un método de pago'
      });
      return;
    }

    this.cerrarModales();

    if (this.esMetodo(metodo, 'yape')) {
      this.modalYape = true;
      return;
    }

    if (this.esMetodo(metodo, 'plin')) {
      this.modalPlin = true;
      return;
    }

    if (this.esMetodo(metodo, 'paypal')) {
      this.modalPaypal = true;
      return;
    }

    if (this.esMetodo(metodo, 'tarjeta')) {
      this.modalTarjeta = true;
      return;
    }

    if (this.esMetodo(metodo, 'transferencia')) {
      this.modalTransferencia = true;
      return;
    }
  }

  cerrarModales() {
    this.modalYape = false;
    this.modalPlin = false;
    this.modalPaypal = false;
    this.modalTarjeta = false;
    this.modalTransferencia = false;
  }

  pagarConYape() {
    if (this.formYape.invalid) {
      this.formYape.markAllAsTouched();
      return;
    }

    const codigo = String(this.formYape.value.codigo || '').trim();
    this.confirmarPagoBackend(codigo || this.generarNroOperacion('YAPE'));
  }

  pagarConPlin() {
    if (this.formPlin.invalid) {
      this.formPlin.markAllAsTouched();
      return;
    }

    const codigo = String(this.formPlin.value.codigo || '').trim();
    this.confirmarPagoBackend(codigo || this.generarNroOperacion('PLIN'));
  }

  pagarConTransferencia() {
    if (this.formTransferencia.invalid) {
      this.formTransferencia.markAllAsTouched();
      return;
    }

    const nro = String(this.formTransferencia.value.nroOperacion || '').trim();
    this.confirmarPagoBackend(nro || this.generarNroOperacion('TRANSFER'));
  }

  pagarConPaypal() {
    const nro = this.generarNroOperacion('PAYPAL');
    this.confirmarPagoBackend(nro);
  }

  pagarConTarjeta() {
    if (this.formTarjeta.invalid) {
      this.formTarjeta.markAllAsTouched();
      return;
    }

    const nro = String(this.formPago.value.nroOperacion || '').trim() || this.generarNroOperacion('TARJETA');
    this.confirmarPagoBackend(nro);
  }

  confirmarPagoBackend(nroOperacion: string) {
    if (!this.orden || this.procesando) return;

    this.procesando = true;

    const payload = {
      idOrden: String(this.orden.idOrden),
      idMetodo: String(this.formPago.value.idMetodo),
      nroOperacion: String(nroOperacion || '').trim()
    };

    this.http.post(`${this.api}/pagos/confirmar`, payload).subscribe({
      next: () => {
        this.procesando = false;
        this.pagoOk = true;
        this.cerrarModales();
        this.guardarMetodoTemporal();

        const direccionActual =
          this.direcciones.find(d => d.idDireccion === this.direccionSel) || null;

        const ordenConItems = {
          ...this.orden,
          items: this.items
        };

        this.cart.removePurchasedItems(this.items);
        this.cart.setCheckoutItems([]);

        Swal.fire({
          icon: 'success',
          title: 'Pago confirmado',
          timer: 1400,
          showConfirmButton: false
        }).then(() => {
          this.router.navigate(['/cliente/confirmacion', this.orden?.idOrden], {
            state: {
              orden: ordenConItems,
              direccion: direccionActual,
              metodo: this.metodoPagoTexto()
            }
          });
        });
      },
      error: (err) => {
        this.procesando = false;

        const msg =
          typeof err?.error === 'string'
            ? err.error
            : err?.error?.message || 'No se pudo confirmar el pago';

        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: msg
        });
      }
    });
  }

  metodoPagoTexto() {
    const id = Number(this.formPago.value.idMetodo);
    const metodo = this.metodosPago.find(m => Number(m.idMetodoPago) === id);
    return metodo?.nombre || 'Pago';
  }

  get yapeEmailInvalid() {
    const c = this.formYape.get('email');
    return !!(c && c.invalid && c.touched);
  }

  get yapeTelefonoInvalid() {
    const c = this.formYape.get('telefono');
    return !!(c && c.invalid && c.touched);
  }

  get yapeCodigoInvalid() {
    const c = this.formYape.get('codigo');
    return !!(c && c.invalid && c.touched);
  }

  get plinEmailInvalid() {
    const c = this.formPlin.get('email');
    return !!(c && c.invalid && c.touched);
  }

  get plinTelefonoInvalid() {
    const c = this.formPlin.get('telefono');
    return !!(c && c.invalid && c.touched);
  }

  get plinCodigoInvalid() {
    const c = this.formPlin.get('codigo');
    return !!(c && c.invalid && c.touched);
  }

  get transferenciaTitularInvalid() {
    const c = this.formTransferencia.get('titular');
    return !!(c && c.invalid && c.touched);
  }

  get transferenciaNroInvalid() {
    const c = this.formTransferencia.get('nroOperacion');
    return !!(c && c.invalid && c.touched);
  }

  get tarjetaNumeroInvalid() {
    const c = this.formTarjeta.get('numero');
    return !!(c && c.invalid && c.touched);
  }

  get tarjetaTitularInvalid() {
    const c = this.formTarjeta.get('titular');
    return !!(c && c.invalid && c.touched);
  }

  get tarjetaMesInvalid() {
    const c = this.formTarjeta.get('mes');
    return !!(c && c.invalid && c.touched);
  }

  get tarjetaAnioInvalid() {
    const c = this.formTarjeta.get('anio');
    return !!(c && c.invalid && c.touched);
  }

  get tarjetaCvvInvalid() {
    const c = this.formTarjeta.get('cvv');
    return !!(c && c.invalid && c.touched);
  }
}