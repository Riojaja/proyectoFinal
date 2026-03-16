import { Component, OnInit, OnDestroy, inject, ChangeDetectorRef } from '@angular/core';
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
  private cdr = inject(ChangeDetectorRef);
  private destroy$ = new Subject<void>();

  usuarioNombre = 'Usuario';
  tiendaNombre = 'Mi Tienda';
  logoUrl: string | null = null;
  cargandoPerfil = false;
  cerrandoSesion = false;

  ngOnInit(): void {
    this.cargarSesion();
    this.cargarPerfilVendedor();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private cargarSesion(): void {
    this.session.ensure().pipe(takeUntil(this.destroy$)).subscribe({
      next: (s) => {
        this.usuarioNombre = s?.nombreCompleto || 'Usuario';
        this.cdr.detectChanges();
      },
      error: () => {
        this.usuarioNombre = 'Usuario';
        this.cdr.detectChanges();
      }
    });
  }

  private cargarPerfilVendedor(): void {
    this.cargandoPerfil = true;
    this.cdr.detectChanges();

    this.vendedorService.miPerfil().pipe(takeUntil(this.destroy$)).subscribe({
      next: (v) => {
        this.tiendaNombre = v?.nombreTienda || 'Mi Tienda';
        this.logoUrl = v?.logoUrl ? this.fullUrl(v.logoUrl) : null;
        this.cargandoPerfil = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando perfil vendedor', err);
        this.tiendaNombre = 'Mi Tienda';
        this.logoUrl = null;
        this.cargandoPerfil = false;
        this.cdr.detectChanges();
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudo cargar la información de la tienda.',
          timer: 3000,
          showConfirmButton: false,
        });
      }
    });
  }

  private fullUrl(url: string): string {
    if (!url) return '';
    if (url.startsWith('http://') || url.startsWith('https://')) return url;
    return `${environment.apiUrl}${url.startsWith('/') ? '' : '/'}${url}`;
  }

  get inicialTienda(): string {
    return this.tiendaNombre?.trim()?.charAt(0)?.toUpperCase() || 'T';
  }

  get inicialUsuario(): string {
    return this.usuarioNombre?.trim()?.charAt(0)?.toUpperCase() || 'U';
  }

  logout(): void {
    if (this.cerrandoSesion) return;
    this.cerrandoSesion = true;

    Swal.fire({
      title: '¿Cerrar sesión?',
      text: '¿Estás seguro de que deseas salir?',
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Sí, cerrar sesión',
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      if (result.isConfirmed) {
        this.auth.logout();
        this.router.navigateByUrl('/cliente', { replaceUrl: true }).finally(() => {
          this.cerrandoSesion = false;
        });
      } else {
        this.cerrandoSesion = false;
      }
    });
  }
}