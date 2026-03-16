import { Component, OnDestroy, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import Swal from 'sweetalert2';

import { ProductoService } from '../../../../core/services/producto.service';
import { CategoriaService } from '../../../../core/services/categoria.service';
import { CartService, CartItem } from '../../../../core/services/cart.service';

type FiltroHome = 'mas-vendidos' | 'nuevos' | 'ofertas';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
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

  productos: any[] = [];
  categorias: any[] = [];
  promosCategorias: any[] = [];
  heroSlides: any[] = [];

  cargando = false;
  procesandoCarrito = false; // para prevenir doble clic en agregar

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

  ngOnInit(): void {
    this.cargarCategorias();
    this.cargarPromosCategorias();
    this.cargarHeroDesdeProductos();
    this.iniciarRotacion();

    this.itemsCarrito = this.cart.getItems();
    this.cartSub = this.cart.items$.subscribe(items => {
      this.itemsCarrito = items || [];
      this.cdr.detectChanges(); // forzar actualización del modal
    });

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
        this.filtroActual = filtro;
      } else {
        this.filtroActual = 'mas-vendidos';
      }

      this.tituloSeccion = this.getTituloFiltro(this.filtroActual);
      this.cargarProductosHome();
    });
  }

  ngOnDestroy(): void {
    this.rotadorSub?.unsubscribe();
    this.querySub?.unsubscribe();
    this.cartSub?.unsubscribe();
    clearTimeout(this.modalTimer);
  }

  cargarCategorias() {
    this.categoriaService.listarPrincipales().subscribe({
      next: (data) => {
        this.categorias = data || [];
        if (this.categoriaActual) {
          this.actualizarTituloCategoria(this.categoriaActual);
        }
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

  cargarHeroDesdeProductos() {
    this.heroSlides = [];

    this.productoService.listarMasVendidos(1).subscribe({
      next: (data) => {
        const p = data?.[0];
        const imagen = p ? this.imgHero(p) : null;

        if (p && imagen) {
          this.upsertHeroSlide({
            titulo: '¡PUNAMBA MARKET!',
            subtitulo: p.nombre || 'Producto más vendido',
            boton: 'Ver más vendidos',
            filtro: 'mas-vendidos' as FiltroHome,
            imagen
          });
        }
        this.cdr.detectChanges();
      },
      error: () => { }
    });

    this.productoService.listarNuevos(1).subscribe({
      next: (data) => {
        const p = data?.[0];
        const imagen = p ? this.imgHero(p) : null;

        if (p && imagen) {
          this.upsertHeroSlide({
            titulo: 'Lo más nuevo',
            subtitulo: p.nombre || 'Producto recién llegado',
            boton: 'Ver novedades',
            filtro: 'nuevos' as FiltroHome,
            imagen
          });
        }
        this.cdr.detectChanges();
      },
      error: () => { }
    });

    this.productoService.listarOfertas(1).subscribe({
      next: (data) => {
        const p = data?.[0];
        const imagen = p ? this.imgHero(p) : null;

        if (p && imagen) {
          this.upsertHeroSlide({
            titulo: 'Mejores ofertas',
            subtitulo: p.nombre || 'Producto en oferta',
            boton: 'Ver ofertas',
            filtro: 'ofertas' as FiltroHome,
            imagen
          });
        }
        this.cdr.detectChanges();
      },
      error: () => { }
    });
  }

  upsertHeroSlide(slide: any) {
    const idx = this.heroSlides.findIndex(x => x.filtro === slide.filtro);

    if (idx >= 0) {
      this.heroSlides[idx] = slide;
    } else {
      this.heroSlides.push(slide);
    }

    if (this.heroIndex >= this.heroSlides.length) {
      this.heroIndex = 0;
    }
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

    this.productoService.listar({
      categoria: this.categoriaActual,
      limit: 10
    }).subscribe({
      next: (data) => {
        this.productos = data || [];
        this.cargando = false;
        this.cdr.detectChanges();

        if (this.categoriaActual) {
          this.actualizarTituloCategoria(this.categoriaActual);
        }
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

  cambiarFiltro(filtro: FiltroHome) {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { filtro, categoria: null },
      queryParamsHandling: 'merge'
    });
  }

  irACategoria(idCategoria: number) {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { categoria: idCategoria, filtro: null },
      queryParamsHandling: 'merge'
    });
  }

  volverAlInicio() {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { categoria: null, filtro: 'mas-vendidos' },
      queryParamsHandling: 'merge'
    });
  }

  actualizarTituloCategoria(idCategoria: number) {
    const cat = this.categorias.find(c => c.idCategoria === idCategoria);
    this.tituloSeccion = cat ? `Productos de ${cat.nombre}` : 'Productos por categoría';
    this.cdr.detectChanges();
  }

  iniciarRotacion() {
    this.rotadorSub = interval(5000).subscribe(() => {
      if (this.heroSlides.length > 1) {
        this.heroIndex = (this.heroIndex + 1) % this.heroSlides.length;
        this.cdr.detectChanges();
      }
    });
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
      this.cambiarFiltro(this.heroActual.filtro);
    }
  }

  add(p: any) {
    if (this.procesandoCarrito) return; // prevenir doble clic
    this.procesandoCarrito = true;

    this.cart.add(p);
    this.productoAgregado = p;
    this.mostrarCartModal = true;
    this.procesandoCarrito = false; // liberamos rápido porque add es síncrono, pero puede ser asíncrono si el servicio llama a API, en ese caso habría que ajustar

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
    return this.itemsCarrito.reduce((acc, it) => {
      const precio = Number(it?.precioOferta ?? it?.precio ?? 0);
      const cantidad = Number(it?.cantidad || 1);
      return acc + (precio * cantidad);
    }, 0);
  }

  cantidadItemsCarrito(): number {
    return this.itemsCarrito.length;
  }

  cantidadUnidadesCarrito(): number {
    return this.itemsCarrito.reduce((acc, it) => acc + Number(it?.cantidad || 1), 0);
  }

  img(p: any) {
    return p?.imagenes?.find((x: any) => x?.esPrincipal)?.urlImagen
      || p?.imagenes?.[0]?.urlImagen
      || p?.imagenes?.[0]?.url_imagen
      || '';
  }

  imgHero(p: any) {
    return p?.imagenes?.find((x: any) => x?.esPrincipal)?.urlImagen
      || p?.imagenes?.[0]?.urlImagen
      || p?.imagenes?.[0]?.url_imagen
      || null;
  }

  imgPromo(c: any) {
    return c?.imagen || '';
  }

  imgCarrito(item: any) {
    return item?.imagen || '';
  }

  precio(p: any) {
    const varianteOferta = p?.variantes?.find((v: any) =>
      v?.precioOferta != null &&
      Number(v.precioOferta) > 0 &&
      Number(v.precioOferta) < Number(v.precio)
    );

    if (varianteOferta) return Number(varianteOferta.precioOferta);

    const varianteNormal = p?.variantes?.find((v: any) =>
      v?.precio != null && Number(v.precio) > 0
    );

    return Number(
      varianteNormal?.precio ??
      p?.precioBase ??
      p?.precio_base ??
      p?.precio ??
      0
    );
  }

  precioAnterior(p: any) {
    const varianteOferta = p?.variantes?.find((v: any) =>
      v?.precioOferta != null &&
      Number(v.precioOferta) > 0 &&
      Number(v.precioOferta) < Number(v.precio)
    );

    return varianteOferta ? Number(varianteOferta.precio) : null;
  }

  precioCarrito(item: any) {
    return Number(item?.precioOferta ?? item?.precio ?? 0);
  }

  descuento(p: any) {
    const anterior = this.precioAnterior(p);
    const actual = this.precio(p);

    if (!anterior || anterior <= actual) return null;

    return Math.round(((anterior - actual) / anterior) * 100);
  }

  trackByProducto(_: number, p: any) {
    return p?.idProducto ?? p?.id;
  }

  trackByCartItem(_: number, item: any) {
    return item?.idVariante ?? item?.idItem ?? item?.idProducto;
  }

  getTituloFiltro(filtro: FiltroHome): string {
    if (filtro === 'nuevos') return 'Lo nuevo';
    if (filtro === 'ofertas') return 'Mejores ofertas';
    return 'Más vendidos';
  }

  get heroActual() {
    return this.heroSlides.length ? this.heroSlides[this.heroIndex] : null;
  }

  get itemsCarritoPreview(): CartItem[] {
    return this.itemsCarrito.slice(0, 4);
  }

  get hayMasItemsCarrito(): boolean {
    return this.itemsCarrito.length > 4;
  }

  verDetalle(p: any) {
    const id = p?.idProducto ?? p?.id;
    if (!id) return;

    this.router.navigate(['/cliente/producto', id]);
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
  }

  aumentarCantidad(item: CartItem) {
    if (this.procesandoCarrito) return;
    this.procesandoCarrito = true;

    const actual = Number(item?.cantidad || 1);
    this.cart.setQty(item.idVariante, actual + 1);
    this.procesandoCarrito = false;
  }

  eliminarDelCarrito(item: CartItem) {
    if (this.procesandoCarrito) return;
    this.procesandoCarrito = true;

    this.cart.remove(item.idVariante);
    this.procesandoCarrito = false;
  }
}