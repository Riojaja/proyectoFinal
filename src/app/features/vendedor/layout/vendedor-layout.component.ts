import {
  Component,
  OnInit,
  OnDestroy,
  inject,
  signal,
  computed
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import Swal from 'sweetalert2';
import { AuthService } from '../../../core/services/auth.service';
import { SessionService } from '../../../core/services/session.service';
import { VendedorService } from '../../../core/services/vendedor.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-vendedor-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './vendedor-layout.component.html',
  styleUrls: ['./vendedor-layout.component.scss']
})
export class VendedorLayoutComponent implements OnInit, OnDestroy {
  private router = inject(Router);
  private auth = inject(AuthService);
  private session = inject(SessionService);
  private vendedorService = inject(VendedorService);
  private destroy$ = new Subject<void>();

  // Signals
  usuarioNombre = signal<string>('Usuario');
  tiendaNombre = signal<string>('Mi Tienda');
  logoUrl = signal<string | null>(null);
  imagenError = signal<boolean>(false);
  
  cargandoPerfil = signal<boolean>(false);
  cerrandoSesion = signal<boolean>(false);

  // Computed signals
  inicialTienda = computed(() => {
    return this.tiendaNombre()?.trim()?.charAt(0)?.toUpperCase() || 'T';
  });

  inicialUsuario = computed(() => {
    return this.usuarioNombre()?.trim()?.charAt(0)?.toUpperCase() || 'U';
  });

  safeLogoUrl = computed(() => {
    const url = this.logoUrl();
    return url && !this.imagenError() ? url : '';
  });

  ngOnInit(): void {
    this.cargarSesion();
    this.cargarPerfilVendedor();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private cargarSesion(): void {
    this.session.ensure()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (s: any) => {
          this.usuarioNombre.set(s?.nombreCompleto || s?.nombre || 'Usuario');
        },
        error: () => {
          this.usuarioNombre.set('Usuario');
        }
      });
  }

  private cargarPerfilVendedor(): void {
    this.cargandoPerfil.set(true);
    this.imagenError.set(false);

    this.vendedorService.miPerfil()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (v: any) => {
          this.tiendaNombre.set(v?.nombreTienda || 'Mi Tienda');
          this.logoUrl.set(v?.logoUrl ? this.fullUrl(v.logoUrl) : null);
          this.cargandoPerfil.set(false);
        },
        error: (err: any) => {
          console.error('Error cargando perfil vendedor', err);
          this.tiendaNombre.set('Mi Tienda');
          this.logoUrl.set(null);
          this.cargandoPerfil.set(false);
        }
      });
  }

  private fullUrl(url: string): string {
    if (!url) return '';
    if (url.startsWith('http://') || url.startsWith('https://')) return url;
    return `${environment.apiUrl}${url.startsWith('/') ? '' : '/'}${url}`;
  }

  onImageError(): void {
    this.imagenError.set(true);
    console.warn('Error cargando logo:', this.logoUrl());
  }

  logout(): void {
    if (this.cerrandoSesion()) return;
    this.cerrandoSesion.set(true);

    Swal.fire({
      title: '¿Cerrar sesión?',
      text: '¿Estás seguro de que deseas salir?',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí, cerrar sesión',
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      if (result.isConfirmed) {
        this.auth.logout();
        this.router.navigateByUrl('/cliente', { replaceUrl: true }).finally(() => {
          this.cerrandoSesion.set(false);
        });
      } else {
        this.cerrandoSesion.set(false);
      }
    });
  }
}