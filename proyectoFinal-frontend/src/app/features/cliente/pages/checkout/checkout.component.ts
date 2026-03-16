import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators, FormGroup } from '@angular/forms';
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
  ubigeo: { codigoUbigeo: string; departamento: string; provincia: string; distrito: string };
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
  estado?: { idEstado: number; nombre: string };
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
  procesando = false; // para prevenir doble clic

  items: CartItem[] = [];

  idUsuario: number | null = null;

  direcciones: Direccion[] = [];
  direccionSel: number | null = null;
  creandoDireccion = false;

  departamentos: string[] = [];
  provincias: string[] = [];
  distritos: Ubigeo[] = [];

  creandoOrden = false;
  orden: Orden | null = null;

  metodosPago: MetodoPago[] = [];
  confirmandoPago = false;
  pagoOk = false;

  formOrden!: FormGroup;
  formDireccion!: FormGroup;
  formPago!: FormGroup;

  constructor() {
    this.formOrden = this.fb.group({
      notas: [''],
    });

    this.formDireccion = this.fb.group({
      nombreDestinatario: ['', [Validators.required, Validators.maxLength(150)]],
      telefono: ['', [Validators.required, Validators.maxLength(20)]],
      direccionLinea1: ['', [Validators.required, Validators.maxLength(255)]],
      direccionLinea2: [''],
      departamento: ['', Validators.required],
      provincia: ['', Validators.required],
      codigoUbigeo: ['', Validators.required],
      esPrincipal: [true],
    });

    this.formPago = this.fb.group({
      idMetodo: [null, Validators.required],
      nroOperacion: [''],
    });
  }

  ngOnInit(): void {
    this.items = this.cart.getItems();
    this.cart.items$.subscribe(x => {
      this.items = x;
      this.cdr.detectChanges();
    });

    if (!this.items.length) {
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

    this.cargando = true;
    this.cdr.detectChanges();

    this.http.get<Direccion[]>(`${this.api}/direcciones/usuario/${this.idUsuario}`).subscribe({
      next: (d) => {
        this.direcciones = d || [];
        const principal = this.direcciones.find(x => x.esPrincipal) || this.direcciones[0];
        this.direccionSel = principal?.idDireccion ?? null;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.direcciones = [];
        this.direccionSel = null;
        this.cargando = false;
        this.cdr.detectChanges();
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudieron cargar las direcciones.',
          timer: 3000,
          showConfirmButton: false,
        });
      }
    });
  }

  toggleCrearDireccion() {
    this.creandoDireccion = !this.creandoDireccion;
  }

  guardarDireccion() {
    if (this.procesando) return;
    if (!this.idUsuario) return;

    if (this.formDireccion.invalid) {
      this.formDireccion.markAllAsTouched();
      Swal.fire({
        icon: 'warning',
        title: 'Campos incompletos',
        text: 'Completa todos los campos obligatorios de la dirección.',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

    this.procesando = true;

    const v: any = this.formDireccion.getRawValue();

    const body: any = {
      nombreDestinatario: v.nombreDestinatario,
      telefono: v.telefono,
      direccionLinea1: v.direccionLinea1,
      direccionLinea2: v.direccionLinea2,
      esPrincipal: !!v.esPrincipal,
    };

    this.http.post(`${this.api}/direcciones/usuario/${this.idUsuario}/ubigeo/${v.codigoUbigeo}`, body).subscribe({
      next: () => {
        this.procesando = false;
        this.creandoDireccion = false;
        this.cargarDirecciones();
        Swal.fire({
          icon: 'success',
          title: 'Dirección guardada',
          text: 'La dirección se ha guardado correctamente.',
          timer: 2000,
          showConfirmButton: false,
        });
      },
      error: (e) => {
        this.procesando = false;
        this.cdr.detectChanges();
        const msg = typeof e?.error === 'string' ? e.error : 'No se pudo guardar la dirección.';
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: msg,
          timer: 3000,
          showConfirmButton: false,
        });
      }
    });
  }

  cargarUbigeosBase() {
    this.http.get<string[]>(`${this.api}/ubigeos/departamentos`).subscribe({
      next: (d) => {
        this.departamentos = d || [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.departamentos = [];
        this.cdr.detectChanges();
      },
    });
  }

  onDepartamento() {
    const v: any = this.formDireccion.getRawValue();
    const dep = v.departamento;

    this.provincias = [];
    this.distritos = [];
    this.formDireccion.patchValue({ provincia: '', codigoUbigeo: '' });

    if (!dep) return;

    this.http.get<string[]>(`${this.api}/ubigeos/provincias/${encodeURIComponent(dep)}`).subscribe({
      next: (p) => {
        this.provincias = p || [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.provincias = [];
        this.cdr.detectChanges();
      },
    });
  }

  onProvincia() {
    const v: any = this.formDireccion.getRawValue();
    const dep = v.departamento;
    const prov = v.provincia;

    this.distritos = [];
    this.formDireccion.patchValue({ codigoUbigeo: '' });

    if (!dep || !prov) return;

    this.http.get<Ubigeo[]>(
      `${this.api}/ubigeos/distritos/${encodeURIComponent(dep)}/${encodeURIComponent(prov)}`
    ).subscribe({
      next: (d) => {
        this.distritos = d || [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.distritos = [];
        this.cdr.detectChanges();
      },
    });
  }

  cargarMetodosPago() {
    this.http.get<MetodoPago[]>(`${this.api}/pagos/metodos`).subscribe({
      next: (m) => {
        this.metodosPago = (m || []).filter(x => x.activo);

        if (!this.metodosPago.length) {
          this.metodosPago = [
            { idMetodoPago: 1, nombre: 'Transferencia bancaria', tipo: 'TRANSFERENCIA', activo: true },
            { idMetodoPago: 2, nombre: 'Contra entrega', tipo: 'CONTRA_ENTREGA', activo: true },
            { idMetodoPago: 3, nombre: 'PayPal Sandbox', tipo: 'PAYPAL_SANDBOX', activo: true }
          ];
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.metodosPago = [
          { idMetodoPago: 1, nombre: 'Transferencia bancaria', tipo: 'TRANSFERENCIA', activo: true },
          { idMetodoPago: 2, nombre: 'Contra entrega', tipo: 'CONTRA_ENTREGA', activo: true },
          { idMetodoPago: 3, nombre: 'PayPal Sandbox', tipo: 'PAYPAL_SANDBOX', activo: true }
        ];
        this.cdr.detectChanges();
      },
    });
  }

  seleccionarMetodo(idMetodo: number) {
    this.formPago.patchValue({ idMetodo });
  }

  metodoSeleccionado(idMetodo: number) {
    return Number(this.formPago.get('idMetodo')?.value) === Number(idMetodo);
  }

  nombreMetodo(tipo: string) {
    if (tipo === 'TRANSFERENCIA') return 'Transferencia bancaria';
    if (tipo === 'CONTRA_ENTREGA') return 'Contra entrega';
    if (tipo === 'PAYPAL_SANDBOX') return 'PayPal Sandbox';
    return tipo;
  }

  descripcionMetodo(tipo: string) {
    if (tipo === 'TRANSFERENCIA') return 'Realiza el pago por transferencia y registra tu operación.';
    if (tipo === 'CONTRA_ENTREGA') return 'Paga al momento de recibir tu pedido.';
    if (tipo === 'PAYPAL_SANDBOX') return 'Modo de prueba para simular pago con PayPal.';
    return 'Método disponible';
  }

  iconoMetodo(tipo: string) {
    if (tipo === 'TRANSFERENCIA') return 'bi-bank';
    if (tipo === 'CONTRA_ENTREGA') return 'bi-truck';
    if (tipo === 'PAYPAL_SANDBOX') return 'bi-paypal';
    return 'bi-credit-card';
  }

  crearOrden() {
    if (this.procesando) return;

    if (!this.direccionSel) {
      Swal.fire({
        icon: 'warning',
        title: 'Dirección requerida',
        text: 'Selecciona una dirección de envío.',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

    if (this.formPago.invalid) {
      this.formPago.markAllAsTouched();
      Swal.fire({
        icon: 'warning',
        title: 'Método de pago requerido',
        text: 'Selecciona un método de pago.',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

    this.procesando = true;
    this.creandoOrden = true;
    this.cdr.detectChanges();

    const payload: any = {
      idDireccion: this.direccionSel,
      notas: (this.formOrden.getRawValue() as any).notas || null,
    };

    this.http.post<Orden>(`${this.api}/ordenes/checkout`, payload).subscribe({
      next: (o) => {
        this.orden = o;
        this.creandoOrden = false;
        this.procesando = false;
        this.cart.refresh();
        this.cdr.detectChanges();
        Swal.fire({
          icon: 'success',
          title: 'Orden creada',
          text: `Orden ${o?.numeroOrden} creada correctamente.`,
          timer: 2000,
          showConfirmButton: false,
        });
      },
      error: (e) => {
        this.creandoOrden = false;
        this.procesando = false;
        this.cdr.detectChanges();
        const msg = typeof e?.error === 'string' ? e.error : 'No se pudo crear la orden.';
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: msg,
          timer: 3000,
          showConfirmButton: false,
        });
      }
    });
  }

  confirmarPago() {
    if (this.procesando) return;

    if (!this.orden) {
      Swal.fire({
        icon: 'warning',
        title: 'Orden no encontrada',
        text: 'Primero crea la orden.',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

    if (this.formPago.invalid) {
      this.formPago.markAllAsTouched();
      Swal.fire({
        icon: 'warning',
        title: 'Método de pago requerido',
        text: 'Selecciona un método de pago.',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

    this.procesando = true;
    this.confirmandoPago = true;
    this.cdr.detectChanges();

    const v: any = this.formPago.getRawValue();
    const payload: any = {
      idOrden: String(this.orden.idOrden),
      idMetodo: String(v.idMetodo),
      nroOperacion: (v.nroOperacion || '').trim(),
    };

    this.http.post(`${this.api}/pagos/confirmar`, payload).subscribe({
      next: () => {
        this.pagoOk = true;
        this.confirmandoPago = false;
        this.procesando = false;
        this.cdr.detectChanges();
        Swal.fire({
          icon: 'success',
          title: 'Pago confirmado',
          text: 'El pago se ha registrado correctamente.',
          timer: 2000,
          showConfirmButton: false,
        });
      },
      error: (e) => {
        this.confirmandoPago = false;
        this.procesando = false;
        this.cdr.detectChanges();
        const msg = typeof e?.error === 'string' ? e.error : 'No se pudo confirmar el pago.';
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: msg,
          timer: 3000,
          showConfirmButton: false,
        });
      }
    });
  } 
}