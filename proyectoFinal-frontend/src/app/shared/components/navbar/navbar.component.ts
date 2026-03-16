import { Component, OnInit, OnDestroy, HostListener, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Subject, takeUntil } from 'rxjs';
import Swal from 'sweetalert2';

import { CartService } from '../../../core/services/cart.service';
import { AuthService, RegisterPayload } from '../../../core/services/auth.service';
import { SessionService } from '../../../core/services/session.service';
import { UiModalService } from '../../../core/services/ui-modal.service';
import { environment } from '../../../../environments/environment';

type TipoRegistro = 'CLIENTE' | 'VENDEDOR';
type FiltroHeader = 'mas-vendidos' | 'ofertas' | 'nuevos' | 'categorias';

type Categoria = {
  idCategoria: number;
  nombre: string;
};

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
})
export class NavbarComponent implements OnInit, OnDestroy {
  private api = environment.apiUrl;
  private destroy$ = new Subject<void>();
  private cdr = inject(ChangeDetectorRef);

  totalItems = 0;

  logged = false;
  nombre = '';
  roles: string[] = [];

  search = '';
  searchReadonly = true;

  loginEmail = '';
  loginPass = '';
  loginShow = false;
  loginLoading = false;

  regLoading = false;

  showLoginModal = false;
  showRegisterModal = false;

  tipoRegistro: TipoRegistro = 'CLIENTE';

  reg = {
    nombre: '',
    apellido: '',
    email: '',
    password: '',
    tipoDocumento: 'DNI',
    numeroDocumento: '',
    telefono: '',
    nombreTienda: '',
    ruc: '',
  };

  filtroHeader: FiltroHeader = 'mas-vendidos';
  mostrarMegaCategorias = false;

  categoriasHeader: Categoria[] = [];

  categoriaHeaderActiva: Categoria | null = null;
  todasActiva = false;

  categoriaPreview: Categoria | null = null;
  todasPreview = false;

  constructor(
    private cart: CartService,
    private auth: AuthService,
    private session: SessionService,
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient,
    private uiModal: UiModalService,
  ) { }

  ngOnInit(): void {
    this.totalItems = this.cart.getItems().length;

    this.cart.items$
      .pipe(takeUntil(this.destroy$))
      .subscribe(items => {
        this.totalItems = items.length;
        this.cdr.detectChanges();
      });

    this.session.session$
      .pipe(takeUntil(this.destroy$))
      .subscribe(s => {
        this.logged = !!s && this.auth.isLogged();
        this.nombre = s?.nombreCompleto || '';
        this.roles = s?.roles || [];
        this.cdr.detectChanges();
      });

    this.uiModal.loginModal$
      .pipe(takeUntil(this.destroy$))
      .subscribe(open => {
        if (open) {
          this.abrirLogin();
          this.uiModal.cerrarLogin();
        }
      });

    if (this.auth.isLogged()) {
      this.session.ensure().pipe(takeUntil(this.destroy$)).subscribe();
      this.cart.refresh();
    }

    this.cargarCategoriasHeader();
    this.escucharQueryParams();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  tiene(role: string): boolean {
    return this.roles.includes(role);
  }

  get esVendedor(): boolean {
    return this.tiene('VENDEDOR');
  }

  get esAdmin(): boolean {
    return this.tiene('ADMIN');
  }

  get esRegistroVendedor(): boolean {
    return this.tipoRegistro === 'VENDEDOR';
  }

  get mostrarBadgeCarrito(): boolean {
    return this.totalItems > 0;
  }

  activarBuscador() {
    this.searchReadonly = false;
  }

  abrirLogin() {
    this.cerrarTodosLosModales();
    this.showLoginModal = true;
    document.body.classList.add('modal-open-custom');
  }

  abrirRegistro() {
    this.cerrarTodosLosModales();
    this.tipoRegistro = 'CLIENTE';
    this.showRegisterModal = true;
    document.body.classList.add('modal-open-custom');
  }

  cerrarTodosLosModales() {
    this.showLoginModal = false;
    this.showRegisterModal = false;
    document.body.classList.remove('modal-open-custom');
    this.cdr.detectChanges();
  }

  cambiarTipoRegistro(tipo: TipoRegistro) {
    this.tipoRegistro = tipo;
  }

  salir() {
    this.auth.logout();
    this.logged = false;
    this.roles = [];
    this.nombre = '';
    this.totalItems = 0;
    this.router.navigateByUrl('/cliente', { replaceUrl: true });
  }

  doLogin() {
    if (this.loginLoading) return;
    this.loginLoading = true;

    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');

    this.auth.login(this.loginEmail, this.loginPass)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (s) => {
          this.loginLoading = false;
          this.cerrarTodosLosModales();

          const roles = s?.roles || [];
          this.cart.refresh();

          // Redirección inteligente
          if (returnUrl) {
            if (returnUrl.startsWith('/admin') && roles.includes('ADMIN')) {
              this.router.navigateByUrl('/admin');
              return;
            }
            if (returnUrl.startsWith('/vendedor') && roles.includes('VENDEDOR')) {
              this.router.navigateByUrl('/vendedor');
              return;
            }
            if (returnUrl.startsWith('/cliente')) {
              this.router.navigateByUrl('/cliente');
              return;
            }
          }

          // Redirección por defecto según rol
          if (roles.includes('ADMIN')) {
            this.router.navigateByUrl('/admin');
          } else if (roles.includes('VENDEDOR')) {
            this.router.navigateByUrl('/vendedor');
          } else {
            this.router.navigateByUrl('/cliente');
          }
        },
        error: (e) => {
          this.loginLoading = false;
          this.cdr.detectChanges();

          let mensaje = 'No se pudo iniciar sesión.';
          if (e.status === 0) mensaje = 'No se pudo conectar con el servidor.';
          else if (e.status === 401) mensaje = 'Credenciales inválidas.';
          else if (e.error?.message) mensaje = e.error.message;

          Swal.fire({
            icon: 'error',
            title: 'Error',
            text: mensaje,
            timer: 3000,
            showConfirmButton: false,
          });
        }
      });
  }

  registrarCuenta() {
    if (this.regLoading) return;

    if (!this.reg.nombre || !this.reg.apellido || !this.reg.email || !this.reg.password || !this.reg.numeroDocumento) {
      Swal.fire({
        icon: 'warning',
        title: 'Campos incompletos',
        text: 'Completa los campos obligatorios.',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

    if (this.esRegistroVendedor && (!this.reg.nombreTienda || !this.reg.ruc)) {
      Swal.fire({
        icon: 'warning',
        title: 'Campos incompletos',
        text: 'Completa el nombre de tienda y el RUC.',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

    this.regLoading = true;

    const payload: RegisterPayload = {
      nombre: this.reg.nombre,
      apellido: this.reg.apellido,
      email: this.reg.email,
      password: this.reg.password,
      tipoDocumento: this.reg.tipoDocumento,
      numeroDocumento: this.reg.numeroDocumento,
      telefono: this.reg.telefono,
      rol: this.esRegistroVendedor ? 'VENDEDOR' : 'CLIENTE',
      nombreTienda: this.esRegistroVendedor ? this.reg.nombreTienda : undefined,
      ruc: this.esRegistroVendedor ? this.reg.ruc : undefined,
    };

    this.auth.register(payload)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.regLoading = false;
          this.cerrarTodosLosModales();
          Swal.fire({
            icon: 'success',
            title: 'Registro exitoso',
            text: 'Tu cuenta ha sido creada. Por favor inicia sesión.',
            timer: 2000,
            showConfirmButton: false,
          });
        },
        error: (e) => {
          this.regLoading = false;
          this.cdr.detectChanges();
          const msg = typeof e?.error === 'string' ? e.error : 'No se pudo registrar la cuenta.';
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

  cargarCategoriasHeader() {
    this.http.get<Categoria[]>(`${this.api}/categorias`)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.categoriasHeader = data || [];
          this.cdr.detectChanges();
        },
        error: () => {
          this.categoriasHeader = [];
          this.categoriaHeaderActiva = null;
          this.categoriaPreview = null;
          this.todasActiva = false;
          this.todasPreview = false;
          this.cdr.detectChanges();
        }
      });
  }

  escucharQueryParams() {
    this.route.queryParams
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const filtro = params['filtro'];
        const categoria = params['categoria'];

        if (categoria) {
          this.filtroHeader = 'categorias';

          if (categoria === 'todas') {
            this.todasActiva = true;
            this.categoriaHeaderActiva = null;
            this.resetPreview();
            this.cdr.detectChanges();
            return;
          }

          this.todasActiva = false;

          const encontrada = this.categoriasHeader.find(
            c => String(c.idCategoria) === String(categoria)
          );

          this.categoriaHeaderActiva = encontrada || null;
          this.resetPreview();
          this.cdr.detectChanges();
          return;
        }

        this.todasActiva = false;
        this.categoriaHeaderActiva = null;
        this.resetPreview();

        if (filtro === 'ofertas') {
          this.filtroHeader = 'ofertas';
        } else if (filtro === 'nuevos') {
          this.filtroHeader = 'nuevos';
        } else {
          this.filtroHeader = 'mas-vendidos';
        }
        this.cdr.detectChanges();
      });
  }

  irAFiltro(tipo: Exclude<FiltroHeader, 'categorias'>) {
    this.filtroHeader = tipo;
    this.mostrarMegaCategorias = false;
    this.todasActiva = false;
    this.categoriaHeaderActiva = null;
    this.resetPreview();
    this.cdr.detectChanges();

    this.router.navigate(['/cliente'], {
      queryParams: { filtro: tipo },
    });
  }

  irATodasCategorias() {
    this.filtroHeader = 'categorias';
    this.todasActiva = true;
    this.categoriaHeaderActiva = null;
    this.resetPreview();
    this.mostrarMegaCategorias = false;
    this.cdr.detectChanges();

    this.router.navigate(['/cliente'], {
      queryParams: { categoria: 'todas' },
    });
  }

  toggleMegaCategorias() {
    this.filtroHeader = 'categorias';
    this.mostrarMegaCategorias = !this.mostrarMegaCategorias;

    if (this.mostrarMegaCategorias) {
      this.resetPreview();
    }
    this.cdr.detectChanges();
  }

  filtrarPorCategoriaHeader(cat: Categoria) {
    this.todasActiva = false;
    this.categoriaHeaderActiva = cat;
    this.resetPreview();
    this.mostrarMegaCategorias = false;
    this.cdr.detectChanges();

    this.router.navigate(['/cliente'], {
      queryParams: { categoria: cat.idCategoria },
    });
  }

  previewTodas() {
    this.todasPreview = true;
    this.categoriaPreview = null;
    this.cdr.detectChanges();
  }

  previewCategoria(cat: Categoria) {
    this.todasPreview = false;
    this.categoriaPreview = cat;
    this.cdr.detectChanges();
  }

  resetPreview() {
    this.todasPreview = false;
    this.categoriaPreview = null;
    this.cdr.detectChanges();
  }

  get tituloMega(): string {
    if (this.todasPreview) return 'Todas';
    if (this.categoriaPreview) return this.categoriaPreview.nombre;
    if (this.todasActiva) return 'Todas';
    if (this.categoriaHeaderActiva) return this.categoriaHeaderActiva.nombre;
    return 'Categorías';
  }

  get notaMega(): string {
    if (this.todasPreview || this.todasActiva) {
      return 'Selecciona esta opción para ver todos los productos.';
    }
    return 'Selecciona una categoría para ver sus productos.';
  }

  @HostListener('document:click', ['$event'])
  cerrarMegaCategoriasFuera(event: MouseEvent) {
    const target = event.target as HTMLElement;
    const dentroSubnav = target.closest('.pm-subnav-wrap');
    const dentroMega = target.closest('.pm-mega-categorias');

    if (!dentroSubnav && !dentroMega) {
      this.mostrarMegaCategorias = false;
      this.resetPreview();
      this.cdr.detectChanges();
    }
  }
}