import { Component, OnInit, OnDestroy, HostListener, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Subject, takeUntil } from 'rxjs';
import Swal from 'sweetalert2';
import { FiltroService } from '../../../core/services/filtro';

import { CartService } from '../../../core/services/cart.service';
import { AuthService, RegisterPayload } from '../../../core/services/auth.service';
import { SessionService } from '../../../core/services/session.service';
import { UiModalService } from '../../../core/services/ui-modal.service';
import { environment } from '../../../../environments/environment';
import { BusquedaService } from '../../../core/services/busqueda';
import { FavoritoService } from '../../../core/services/favorito';

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
  favoritosCount = 0;

  logged = false;
  nombre = '';
  roles: string[] = [];

  search = '';
  searchReadonly = true;

  loginEmail = '';
  loginPass = '';
  loginShow = false;
  loginLoading = false;
  procesandoLogin = false;

  regLoading = false;
  procesandoRegistro = false;

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

  filtroHeader: FiltroHeader | null = 'mas-vendidos';
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
    public router: Router, // 👈 PUBLIC para usar en el HTML
    private route: ActivatedRoute,
    private http: HttpClient,
    private uiModal: UiModalService,
    private filtroService: FiltroService,
    private busquedaService: BusquedaService,
    private favoritoService: FavoritoService,
  ) { }

  ngOnInit(): void {
    this.totalItems = this.cart.getItems().length;

    this.cart.items$
      .pipe(takeUntil(this.destroy$))
      .subscribe(items => {
        this.totalItems = items.length;
        this.cdr.detectChanges();
      });

    this.favoritoService.favoritosCount$
      .pipe(takeUntil(this.destroy$))
      .subscribe(count => {
        this.favoritosCount = count;
        this.cdr.detectChanges();
      });

    this.session.session$
      .pipe(takeUntil(this.destroy$))
      .subscribe(s => {
        this.logged = !!s && this.auth.isLogged();
        this.nombre = s?.nombreCompleto || '';
        this.roles = s?.roles || [];
        this.cdr.detectChanges();

        if (this.logged) {
          this.favoritoService.actualizarContador();
        }
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
      this.favoritoService.actualizarContador();
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

  activarBuscador(): void {
    this.searchReadonly = false;
  }

  buscar(): void {
    if (this.procesandoLogin || this.procesandoRegistro) return;

    const termino = this.search.trim();
    if (!termino) {
      Swal.fire({
        icon: 'warning',
        title: 'Campo vacío',
        text: 'Ingresa un término de búsqueda',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

    window.location.href = `/cliente/buscar?q=${encodeURIComponent(termino)}`;
  }

  abrirLogin(): void {
    this.cerrarTodosLosModales();
    this.showLoginModal = true;
    document.body.classList.add('modal-open-custom');
  }

  abrirRegistro(): void {
    this.cerrarTodosLosModales();
    this.tipoRegistro = 'CLIENTE';
    this.showRegisterModal = true;
    document.body.classList.add('modal-open-custom');
  }

  cerrarTodosLosModales(): void {
    this.showLoginModal = false;
    this.showRegisterModal = false;
    document.body.classList.remove('modal-open-custom');
    this.cdr.detectChanges();
  }

  cambiarTipoRegistro(tipo: TipoRegistro): void {
    this.tipoRegistro = tipo;
  }

  salir(): void {
    if (this.procesandoLogin) return;
    this.procesandoLogin = true;

    this.auth.logout();
    this.logged = false;
    this.roles = [];
    this.nombre = '';
    this.totalItems = 0;
    this.favoritosCount = 0;
    this.router.navigateByUrl('/cliente', { replaceUrl: true }).then(() => {
      this.procesandoLogin = false;
      this.cdr.detectChanges();
    });
  }

  doLogin(): void {
    if (this.procesandoLogin) return;
    this.procesandoLogin = true;

    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');

    this.auth.login(this.loginEmail, this.loginPass)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (s) => {
          this.procesandoLogin = false;
          this.cerrarTodosLosModales();

          const roles = s?.roles || [];
          this.cart.refresh();
          this.favoritoService.actualizarContador();

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

          if (roles.includes('ADMIN')) {
            this.router.navigateByUrl('/admin');
          } else if (roles.includes('VENDEDOR')) {
            this.router.navigateByUrl('/vendedor');
          } else {
            this.router.navigateByUrl('/cliente');
          }
        },
        error: (e) => {
          this.procesandoLogin = false;
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

  registrarCuenta(): void {
    if (this.procesandoRegistro) return;
    this.procesandoRegistro = true;

    if (!this.reg.nombre || !this.reg.apellido || !this.reg.email || !this.reg.password || !this.reg.numeroDocumento) {
      this.procesandoRegistro = false;
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
      this.procesandoRegistro = false;
      Swal.fire({
        icon: 'warning',
        title: 'Campos incompletos',
        text: 'Completa el nombre de tienda y el RUC.',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

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
          this.procesandoRegistro = false;
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
          this.procesandoRegistro = false;
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

  cargarCategoriasHeader(): void {
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

  escucharQueryParams(): void {
    this.route.queryParams
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const filtro = params['filtro'];
        const categoria = params['categoria'];

        const isFavoritos = this.router.url === '/cliente/favoritos';

        if (isFavoritos) {
          this.filtroHeader = null;
          this.todasActiva = false;
          this.categoriaHeaderActiva = null;
          this.resetPreview();
          this.cdr.detectChanges();
          return;
        }

        if (categoria) {
          this.filtroHeader = 'categorias';

          if (categoria === 'todas') {
            this.todasActiva = true;
            this.categoriaHeaderActiva = null;
          } else {
            this.todasActiva = false;
            const encontrada = this.categoriasHeader.find(
              c => String(c.idCategoria) === String(categoria)
            );
            this.categoriaHeaderActiva = encontrada || null;
          }
          this.resetPreview();
          this.cdr.detectChanges();
          return;
        }

        if (filtro) {
          if (filtro === 'ofertas') {
            this.filtroHeader = 'ofertas';
          } else if (filtro === 'nuevos') {
            this.filtroHeader = 'nuevos';
          } else {
            this.filtroHeader = 'mas-vendidos';
          }
          this.todasActiva = false;
          this.categoriaHeaderActiva = null;
          this.resetPreview();
          this.cdr.detectChanges();
          return;
        }

        if (!isFavoritos) {
          this.filtroHeader = 'mas-vendidos';
          this.todasActiva = false;
          this.categoriaHeaderActiva = null;
          this.resetPreview();
          this.cdr.detectChanges();
        }
      });
  }

  irAFiltro(tipo: Exclude<FiltroHeader, 'categorias'>): void {
    if (this.procesandoLogin || this.procesandoRegistro) return;

    this.filtroHeader = tipo;
    this.mostrarMegaCategorias = false;
    this.todasActiva = false;
    this.categoriaHeaderActiva = null;
    this.resetPreview();
    this.cdr.detectChanges();

    this.filtroService.cambiarFiltro(tipo);

    this.router.navigate(['/cliente'], {
      queryParams: { filtro: tipo },
      queryParamsHandling: 'merge'
    });
  }

  irATodasCategorias(): void {
    if (this.procesandoLogin || this.procesandoRegistro) return;
    if (this.router.url === '/cliente/favoritos') return;

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

  toggleMegaCategorias(): void {
    // Solo bloquear si estamos en favoritos
    if (this.router.url === '/cliente/favoritos') {
      return;
    }

    // Alternar el mega menú
    this.mostrarMegaCategorias = !this.mostrarMegaCategorias;

    // Si se abre, resetear preview
    if (this.mostrarMegaCategorias) {
      this.resetPreview();
      this.filtroHeader = 'categorias';
    }

    this.cdr.detectChanges();
  }

  filtrarPorCategoriaHeader(cat: Categoria): void {
    if (this.procesandoLogin || this.procesandoRegistro) return;
    if (this.router.url === '/cliente/favoritos') return;

    this.todasActiva = false;
    this.categoriaHeaderActiva = cat;
    this.resetPreview();
    this.mostrarMegaCategorias = false;
    this.filtroHeader = 'categorias';
    this.cdr.detectChanges();

    this.router.navigate(['/cliente'], {
      queryParams: { categoria: cat.idCategoria },
    });
  }

  previewTodas(): void {
    this.todasPreview = true;
    this.categoriaPreview = null;
    this.cdr.detectChanges();
  }

  previewCategoria(cat: Categoria): void {
    this.todasPreview = false;
    this.categoriaPreview = cat;
    this.cdr.detectChanges();
  }

  resetPreview(): void {
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
  cerrarMegaCategoriasFuera(event: MouseEvent): void {
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