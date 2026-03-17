import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core'; // 👈 IMPORTAR
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FavoritoService } from '../../../../core/services/favorito';
import { AuthService } from '../../../../core/services/auth.service';
import { StarRatingComponent } from '../../../../shared/components/star-rating/star-rating';
import { FavoritoButtonComponent } from '../../../../shared/components/favorito-button/favorito-button';
import { Subject, takeUntil } from 'rxjs';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-favoritos',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    StarRatingComponent,
    FavoritoButtonComponent
  ],
  templateUrl: './favoritos.html',
  styleUrls: ['./favoritos.scss']
})
export class FavoritosComponent implements OnInit, OnDestroy {
  favoritos: any[] = [];
  cargando = true;
  private destroy$ = new Subject<void>();

  trackByProductoFn = (index: number, p: any): number => {
    return p?.idProducto || index;
  };

  constructor(
    private favoritoService: FavoritoService,
    private auth: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef // 👈 INYECTAR
  ) {}

  ngOnInit(): void {
    if (!this.auth.isLogged()) {
      this.router.navigate(['/cliente']);
      Swal.fire({
        icon: 'warning',
        title: 'Inicia sesión',
        text: 'Debes iniciar sesión para ver tus favoritos',
        timer: 2000,
        showConfirmButton: false
      });
      return;
    }

    this.cargarFavoritos();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  public nombre(item: any): string {
    return item?.nombreProducto || 'Producto sin nombre';
  }

  public img(item: any): string {
    let imagenUrl = item?.imagenProducto || '';
    if (!imagenUrl) return '';
    
    if (!imagenUrl.startsWith('http')) {
      const baseUrl = 'http://localhost:8090';
      imagenUrl = `${baseUrl}${imagenUrl.startsWith('/') ? '' : '/'}${imagenUrl}`;
    }
    return imagenUrl;
  }

  public precio(item: any): number {
    return item?.precioProducto || 0;
  }

  public idProducto(item: any): number {
    return item?.idProducto || 0;
  }

  cargarFavoritos(): void {
    console.log('🔄 Cargando favoritos...');

    this.favoritoService.misFavoritos().subscribe({
      next: (res) => {
        console.log('📦 RESPUESTA COMPLETA:', JSON.stringify(res, null, 2));

        if (Array.isArray(res)) {
          this.favoritos = res;
        } else {
          console.error('❌ La respuesta no es un array:', res);
          this.favoritos = [];
        }

        this.cargando = false;
        this.cdr.detectChanges(); // 👈 FORZAR ACTUALIZACIÓN

        if (this.favoritos.length > 0) {
          const primero = this.favoritos[0];
          console.log('🔍 Primer elemento procesado:', {
            nombre: this.nombre(primero),
            precio: this.precio(primero),
            imagen: this.img(primero),
            id: this.idProducto(primero)
          });
        }
      },
      error: (err) => {
        console.error('❌ Error cargando favoritos:', err);
        this.favoritos = [];
        this.cargando = false;
        this.cdr.detectChanges(); // 👈 TAMBIÉN EN ERROR
      }
    });
  }

  verDetalle(p: any): void {
    this.router.navigate(['/cliente/producto', p.idProducto]);
  }
}