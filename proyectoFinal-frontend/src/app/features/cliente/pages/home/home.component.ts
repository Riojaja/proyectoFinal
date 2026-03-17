import { Component, OnDestroy, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { FiltroService } from '../../../../core/services/filtro';
import Swal from 'sweetalert2';
import { Subject, takeUntil } from 'rxjs';

import { ProductoService } from '../../../../core/services/producto.service';
import { CategoriaService } from '../../../../core/services/categoria.service';
import { CartService, CartItem } from '../../../../core/services/cart.service';
import { BackgroundImageDirective } from '../../../../shared/directives/background-image';
// 👇 IMPORTAR LOS NUEVOS COMPONENTES
import { FavoritoButtonComponent } from '../../../../shared/components/favorito-button/favorito-button';
import { StarRatingComponent } from '../../../../shared/components/star-rating/star-rating';

type FiltroHome = 'mas-vendidos' | 'nuevos' | 'ofertas';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    BackgroundImageDirective,
    FavoritoButtonComponent, //  AÑADIDO
    StarRatingComponent      //  AÑADIDO
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.scss']
})
export class HomeComponent implements OnInit, OnDestroy {
  private productoService = inject(ProductoService);
  private categoriaService = inject(CategoriaService);
  private cart = inject(CartService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private filtroService = inject(FiltroService);
  private destroy$ = new Subject<void>();

  private titulosPorTipo = {
    'mas-vendidos': ['🔥 Más vendido', '⭐ Top ventas', '🏆 Lo más popular', '📈 Éxito de ventas'],
    'nuevos': ['✨ Lo más nuevo', '🌟 Recién llegado', '🎁 Novedad', '🚀 Último modelo'],
    'ofertas': ['🎯 Oferta especial', '⚡ Descuento', '💰 Precio rebajado', '🏷️ Promoción']
  };

  private botonesPorTipo = {
    'mas-vendidos': ['Ver más vendidos', 'Explorar top', 'Ver populares', 'Descubrir'],
    'nuevos': ['Ver novedades', 'Explorar nuevos', 'Ver productos nuevos', 'Conocer más'],
    'ofertas': ['Ver ofertas', 'Aprovechar descuento', 'Ver promociones', 'Comprar ahora']
  };

  productos: any[] = [];
  categorias: any[] = [];
  promosCategorias: any[] = [];
  heroSlides: any[] = [];

  cargando = false;
  procesandoCarrito = false;

  categoriaActual: number | null = null;
  filtroActual: FiltroHome = 'mas-vendidos';
  tituloSeccion = 'Más vendidos';

  heroIndex = 0;

  productoAgregado: any = null;
  mostrarCartModal = false;
  itemsCarrito: CartItem[] = [];

  private rotadorSub?: Subscription;
  private querySub?: Subscription;
  private cartSub?: Subscription;
  private modalTimer: any;

  private heroRotacionInterval: any;
  private heroRefreshInterval: any;

  ngOnInit(): void {
    this.cargarCategorias();
    this.cargarPromosCategorias();
    this.cargarHeroDinamico();
    this.iniciarRotacionHero();
    
    // 👇 CARGA INMEDIATA DE PRODUCTOS POR DEFECTO (más vendidos)
    this.filtroActual = 'mas-vendidos';
    this.tituloSeccion = this.getTituloFiltro('mas-vendidos');
    this.cargarProductosHome();

    this.itemsCarrito = this.cart.getItems();
    this.cartSub = this.cart.items$.subscribe(items => {
      this.itemsCarrito = items || [];
      this.cdr.detectChanges();
    });

    // Escuchar cambios de filtro desde el navbar
    this.filtroService.filtro$
      .pipe(takeUntil(this.destroy$))
      .subscribe(filtro => {
        if (filtro && filtro !== this.filtroActual) {
          this.filtroActual = filtro as FiltroHome;
          this.categoriaActual = null;
          this.tituloSeccion = this.getTituloFiltro(this.filtroActual);
          this.cargarProductosHome();
        }
      });

    // Escuchar cambios en queryParams
    this.querySub = this.route.queryParams.subscribe(params => {
      const categoria = params['categoria'];
      const filtro = params['filtro'];

      if (categoria && categoria !== 'todas') {
        this.categoriaActual = Number(categoria);
        this.tituloSeccion = 'Productos por categoría';
        this.cargarProductosPorCategoria();
        return;
      }

      this.categoriaActual = null;

      if (filtro === 'nuevos' || filtro === 'ofertas' || filtro === 'mas-vendidos') {
        if (this.filtroActual !== filtro) {
          this.filtroActual = filtro;
          this.tituloSeccion = this.getTituloFiltro(this.filtroActual);
          this.cargarProductosHome();
        }
      } else {
        if (this.filtroActual !== 'mas-vendidos') {
          this.filtroActual = 'mas-vendidos';
          this.tituloSeccion = this.getTituloFiltro('mas-vendidos');
          this.cargarProductosHome();
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.rotadorSub?.unsubscribe();
    this.querySub?.unsubscribe();
    this.cartSub?.unsubscribe();
    this.destroy$.next();
    this.destroy$.complete();
    clearTimeout(this.modalTimer);

    if (this.heroRotacionInterval) clearInterval(this.heroRotacionInterval);
    if (this.heroRefreshInterval) clearInterval(this.heroRefreshInterval);
  }

  cargarCategorias() {
    this.categoriaService.listarPrincipales().subscribe({
      next: (data) => {
        this.categorias = data || [];
        if (this.categoriaActual) this.actualizarTituloCategoria(this.categoriaActual);
        this.cdr.detectChanges();
      },
      error: () => {
        this.categorias = [];
        this.cdr.detectChanges();
      }
    });
  }

  cargarPromosCategorias() {
    this.categoriaService.listarPromos(3).subscribe({
      next: (data) => {
        this.promosCategorias = data || [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.promosCategorias = [];
        this.cdr.detectChanges();
      }
    });
  }

  cargarHeroDinamico() {
    this.heroSlides = [];

    this.productoService.listarMasVendidos(10).subscribe({
      next: (data) => {
        const productos = data || [];
        if (productos.length > 0) {
          const randomIndex = Math.floor(Math.random() * productos.length);
          const p = productos[randomIndex];
          this.agregarSlide(p, 'mas-vendidos');
        }
      },
      error: () => { }
    });

    this.productoService.listarNuevos(10).subscribe({
      next: (data) => {
        const productos = data || [];
        if (productos.length > 0) {
          const randomIndex = Math.floor(Math.random() * productos.length);
          const p = productos[randomIndex];
          this.agregarSlide(p, 'nuevos');
        }
      },
      error: () => { }
    });

    this.productoService.listarOfertas(10).subscribe({
      next: (data) => {
        const productos = data || [];
        if (productos.length > 0) {
          const randomIndex = Math.floor(Math.random() * productos.length);
          const p = productos[randomIndex];
          this.agregarSlide(p, 'ofertas');
        }
      },
      error: () => { }
    });
  }

  agregarSlide(producto: any, filtro: FiltroHome) {
    if (!producto) return;

    let imagen = this.imgHero(producto);
    if (!imagen) return;

    if (!imagen.startsWith('http')) {
      const baseUrl = 'http://localhost:8090';
      imagen = `${baseUrl}${imagen.startsWith('/') ? '' : '/'}${imagen}`;
    }

    const titulos = this.titulosPorTipo[filtro];
    const botones = this.botonesPorTipo[filtro];
    const tituloElegido = titulos[Math.floor(Math.random() * titulos.length)];
    const botonElegido = botones[Math.floor(Math.random() * botones.length)];

    const slide = {
      titulo: tituloElegido,
      subtitulo: producto.nombre,
      boton: botonElegido,
      filtro: filtro,
      imagen: imagen,
      id: producto.idProducto,
      timestamp: Date.now()
    };

    const idx = this.heroSlides.findIndex(s => s.filtro === filtro);
    if (idx >= 0) {
      this.heroSlides[idx] = slide;
    } else {
      this.heroSlides.push(slide);
    }

    if (this.heroIndex >= this.heroSlides.length) this.heroIndex = 0;
    this.cdr.detectChanges();
  }

  cargarProductosHome() {
    this.cargando = true;
    this.cdr.detectChanges();

    this.productoService.listar({
      tipo: this.filtroActual,
      limit: 10
    }).subscribe({
      next: (data) => {
        this.productos = data || [];
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.productos = [];
        this.cargando = false;
        this.cdr.detectChanges();
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudieron cargar los productos.',
          timer: 3000,
          showConfirmButton: false,
        });
      }
    });
  }

  cargarProductosPorCategoria() {
    this.cargando = true;
    this.cdr.detectChanges();

    this.productoService.listar({ categoria: this.categoriaActual, limit: 10 }).subscribe({
      next: (data) => {
        this.productos = data || [];
        this.cargando = false;
        this.cdr.detectChanges();
        if (this.categoriaActual) this.actualizarTituloCategoria(this.categoriaActual);
      },
      error: () => {
        this.productos = [];
        this.cargando = false;
        this.cdr.detectChanges();
        Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudieron cargar los productos.', timer: 3000, showConfirmButton: false });
      }
    });
  }

  cambiarFiltro(filtro: FiltroHome) {
    this.filtroActual = filtro;
    this.categoriaActual = null;
    this.tituloSeccion = this.getTituloFiltro(filtro);
    this.cargarProductosHome();

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { filtro, categoria: null },
      queryParamsHandling: 'merge'
    });
  }

  irACategoria(idCategoria: number) {
    this.router.navigate([], { relativeTo: this.route, queryParams: { categoria: idCategoria, filtro: null }, queryParamsHandling: 'merge' });
  }

  volverAlInicio() {
    this.router.navigate([], { relativeTo: this.route, queryParams: { categoria: null, filtro: 'mas-vendidos' }, queryParamsHandling: 'merge' });
  }

  actualizarTituloCategoria(idCategoria: number) {
    const cat = this.categorias.find(c => c.idCategoria === idCategoria);
    this.tituloSeccion = cat ? `Productos de ${cat.nombre}` : 'Productos por categoría';
    this.cdr.detectChanges();
  }

  iniciarRotacionHero() {
    if (this.heroRotacionInterval) clearInterval(this.heroRotacionInterval);
    if (this.heroRefreshInterval) clearInterval(this.heroRefreshInterval);

    this.heroRotacionInterval = setInterval(() => {
      if (this.heroSlides.length > 1) {
        this.heroIndex = (this.heroIndex + 1) % this.heroSlides.length;
        if (this.heroSlides[this.heroIndex]) this.heroSlides[this.heroIndex].timestamp = Date.now();
        this.cdr.detectChanges();
      }
    }, 5000);

    this.heroRefreshInterval = setInterval(() => {
      this.cargarHeroDinamico();
    }, 30000);
  }

  prevHero() {
    if (this.heroSlides.length > 1) {
      this.heroIndex = (this.heroIndex - 1 + this.heroSlides.length) % this.heroSlides.length;
      this.cdr.detectChanges();
    }
  }

  nextHero() {
    if (this.heroSlides.length > 1) {
      this.heroIndex = (this.heroIndex + 1) % this.heroSlides.length;
      this.cdr.detectChanges();
    }
  }

  irHero() {
    if (this.heroActual?.filtro) {
      const filtro = this.heroActual.filtro;

      this.filtroActual = filtro;
      this.categoriaActual = null;
      this.tituloSeccion = this.getTituloFiltro(filtro);
      this.cargarProductosHome();

      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { filtro, categoria: null },
        queryParamsHandling: 'merge'
      });
    }
  }

  add(p: any) {
    if (this.procesandoCarrito) return;
    this.procesandoCarrito = true;

    this.cart.add(p);
    this.productoAgregado = p;
    this.mostrarCartModal = true;
    this.procesandoCarrito = false;

    clearTimeout(this.modalTimer);
    this.modalTimer = setTimeout(() => {
      this.mostrarCartModal = false;
      this.cdr.detectChanges();
    }, 4000);
  }

  cerrarCartModal() {
    this.mostrarCartModal = false;
    clearTimeout(this.modalTimer);
    this.cdr.detectChanges();
  }

  irAlCarrito() {
    this.mostrarCartModal = false;
    clearTimeout(this.modalTimer);
    this.router.navigate(['/cliente/carrito']);
  }

  hacerPedido() {
    this.mostrarCartModal = false;
    clearTimeout(this.modalTimer);
    this.router.navigate(['/cliente/carrito']);
  }

  seguirComprando() {
    this.mostrarCartModal = false;
    clearTimeout(this.modalTimer);
    this.cdr.detectChanges();
  }

  subtotalCarrito(): number {
    return this.itemsCarrito.reduce((acc, it) => acc + (Number(it?.precioOferta ?? it?.precio ?? 0) * Number(it?.cantidad || 1)), 0);
  }

  cantidadItemsCarrito(): number { return this.itemsCarrito.length; }
  cantidadUnidadesCarrito(): number { return this.itemsCarrito.reduce((acc, it) => acc + Number(it?.cantidad || 1), 0); }

  img(p: any): string {
    return p?.imagenes?.find((x: any) => x?.esPrincipal)?.urlImagen
      || p?.imagenes?.[0]?.urlImagen
      || p?.imagenes?.[0]?.url_imagen
      || '';
  }

  imgHero(p: any): string | null {
    return p?.imagenes?.find((x: any) => x?.esPrincipal)?.urlImagen
      || p?.imagenes?.[0]?.urlImagen
      || p?.imagenes?.[0]?.url_imagen
      || null;
  }

  imgPromo(c: any): string { return c?.imagen || ''; }
  imgCarrito(item: any): string { return item?.imagen || ''; }

  precio(p: any): number {
    const oferta = p?.variantes?.find((v: any) => v?.precioOferta != null && v.precioOferta > 0 && v.precioOferta < v.precio);
    if (oferta) return Number(oferta.precioOferta);
    const normal = p?.variantes?.find((v: any) => v?.precio != null && v.precio > 0);
    return Number(normal?.precio ?? p?.precioBase ?? p?.precio_base ?? p?.precio ?? 0);
  }

  precioAnterior(p: any): number | null {
    const oferta = p?.variantes?.find((v: any) => v?.precioOferta != null && v.precioOferta > 0 && v.precioOferta < v.precio);
    return oferta ? Number(oferta.precio) : null;
  }

  precioCarrito(item: any): number { return Number(item?.precioOferta ?? item?.precio ?? 0); }

  descuento(p: any): number | null {
    const anterior = this.precioAnterior(p);
    const actual = this.precio(p);
    return (anterior && anterior > actual) ? Math.round(((anterior - actual) / anterior) * 100) : null;
  }

  trackByProducto(_: number, p: any) { return p?.idProducto ?? p?.id; }
  trackByCartItem(_: number, item: any) { return item?.idVariante ?? item?.idItem ?? item?.idProducto; }

  getTituloFiltro(filtro: FiltroHome): string {
    if (filtro === 'nuevos') return 'Lo nuevo';
    if (filtro === 'ofertas') return 'Mejores ofertas';
    return 'Más vendidos';
  }

  get heroActual() { return this.heroSlides.length ? this.heroSlides[this.heroIndex] : null; }
  get itemsCarritoPreview(): CartItem[] { return this.itemsCarrito.slice(0, 4); }
  get hayMasItemsCarrito(): boolean { return this.itemsCarrito.length > 4; }

  verDetalle(p: any) {
    const id = p?.idProducto ?? p?.id;
    if (id) this.router.navigate(['/cliente/producto', id]);
  }

  disminuirCantidad(item: CartItem) {
    if (this.procesandoCarrito) return;
    this.procesandoCarrito = true;

    const actual = Number(item?.cantidad || 1);
    if (actual <= 1) {
      this.cart.remove(item.idVariante);
    } else {
      this.cart.setQty(item.idVariante, actual - 1);
    }

    this.procesandoCarrito = false;
    this.cdr.detectChanges();
  }

  aumentarCantidad(item: CartItem) {
    if (this.procesandoCarrito) return;
    this.procesandoCarrito = true;

    const actual = Number(item?.cantidad || 1);
    this.cart.setQty(item.idVariante, actual + 1);

    this.procesandoCarrito = false;
    this.cdr.detectChanges();
  }

  eliminarDelCarrito(item: CartItem) {
    if (this.procesandoCarrito) return;
    this.procesandoCarrito = true;

    this.cart.remove(item.idVariante);
    this.procesandoCarrito = false;
    this.cdr.detectChanges();
  }
}