import { Component, OnInit, inject, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { BusquedaService } from '../../../../core/services/busqueda';
import { BusquedaStateService } from '../../../../core/services/busqueda-state';
import { CartService } from '../../../../core/services/cart.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-buscar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './buscar.html',
  styleUrls: ['./buscar.scss']
})
export class BuscarComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private busquedaService = inject(BusquedaService);
  private busquedaState = inject(BusquedaStateService);
  private cart = inject(CartService);
  private cdr = inject(ChangeDetectorRef);
  private destroy$ = new Subject<void>();

  termino = '';
  resultados: any[] = [];
  cargando = false;
  procesandoCarrito = false;

  ngOnInit(): void {
    // 👇 Escuchar búsquedas inmediatas desde el navbar (SIN DOBLE CLIC)
    this.busquedaState.termino$
      .pipe(takeUntil(this.destroy$))
      .subscribe(termino => {
        if (termino && termino !== this.termino) {
          console.log('🔍 Búsqueda instantánea desde navbar:', termino);
          this.termino = termino;
          this.buscar();
        }
      });

    // 👇 Escuchar queryParams (para cuando se recarga la página o se navega directamente)
    this.route.queryParams
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const queryTermino = params['q'] || '';
        if (queryTermino && queryTermino !== this.termino) {
          console.log('🔍 Búsqueda desde queryParams:', queryTermino);
          this.termino = queryTermino;
          this.buscar();
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  buscar() {
    if (!this.termino) return;

    this.cargando = true;
    this.resultados = [];
    this.cdr.detectChanges(); // 👈 Mostrar "Buscando..." INMEDIATAMENTE

    this.busquedaService.buscar(this.termino).subscribe({
      next: (res) => {
        this.resultados = res || [];
        this.cargando = false;
        this.cdr.detectChanges(); // 👈 Mostrar resultados INMEDIATAMENTE
      },
      error: (err) => {
        console.error('Error en búsqueda:', err);
        this.resultados = [];
        this.cargando = false;
        this.cdr.detectChanges();
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudo completar la búsqueda',
          timer: 3000,
          showConfirmButton: false
        });
      }
    });
  }

  verDetalle(p: any) {
    const id = p?.idProducto ?? p?.id;
    if (id) {
      this.router.navigate(['/cliente/producto', id]);
    }
  }

  img(p: any): string {
    return p?.imagenes?.find((x: any) => x?.esPrincipal)?.urlImagen
      || p?.imagenes?.[0]?.urlImagen
      || p?.imagenes?.[0]?.url_imagen
      || '';
  }

  precio(p: any): number {
    const oferta = p?.variantes?.find((v: any) => v?.precioOferta > 0);
    if (oferta) return Number(oferta.precioOferta);
    const normal = p?.variantes?.find((v: any) => v?.precio > 0);
    return Number(normal?.precio ?? p?.precioBase ?? p?.precio_base ?? p?.precio ?? 0);
  }

  add(p: any) {
    if (this.procesandoCarrito) return;
    this.procesandoCarrito = true;

    this.cart.add(p);
    this.procesandoCarrito = false;
    this.cdr.detectChanges();

    Swal.fire({
      icon: 'success',
      title: 'Añadido',
      text: 'Producto añadido al carrito',
      timer: 1500,
      showConfirmButton: false
    });
  }
}