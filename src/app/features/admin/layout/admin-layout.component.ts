import { Component, HostListener, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { filter, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/services/auth.service';
import { DisputaService } from '../../../core/services/disputa.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.scss'],
})
export class AdminLayoutComponent implements OnInit {
  private router = inject(Router);
  private auth = inject(AuthService);
  private http = inject(HttpClient);
  private disputaService = inject(DisputaService);

  private api = environment.apiUrl;

  adminNombre = 'Administrador';
  adminEmail = '';
  adminIniciales = 'A';

  vendedoresPendientes = 0;
  disputasAbiertas = 0;

  sidebarOpen = true;
  currentUrl = '';

  private isNavigating = false;

  ngOnInit(): void {
    this.currentUrl = this.router.url;
    this.cargarUsuarioLocal();
    this.setInitialSidebarState();

    setTimeout(() => {
      this.cargarBadges();
    });

    this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe((e: any) => {
        this.currentUrl = e.urlAfterRedirects || e.url || '';
        if (window.innerWidth < 992) {
          this.sidebarOpen = false;
        }
        this.isNavigating = false;
      });
  }

  private cargarUsuarioLocal() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');

    const nombre =
      user?.nombreCompleto ||
      [user?.nombre, user?.apellido].filter(Boolean).join(' ') ||
      user?.email ||
      'Administrador';

    this.adminNombre = nombre;
    this.adminEmail = user?.email || '';
    this.adminIniciales = this.obtenerIniciales(nombre);
  }

  private obtenerIniciales(nombre: string): string {
    const partes = String(nombre).trim().split(/\s+/).filter(Boolean);
    if (!partes.length) return 'A';
    if (partes.length === 1) return partes[0].slice(0, 1).toUpperCase();
    return (partes[0][0] + partes[1][0]).toUpperCase();
  }

  private setInitialSidebarState() {
    this.sidebarOpen = window.innerWidth >= 992;
  }

  @HostListener('window:resize')
  onResize() {
    if (window.innerWidth >= 992) {
      this.sidebarOpen = true;
    }
  }

  toggleSidebar() {
    this.sidebarOpen = !this.sidebarOpen;
  }

  closeSidebarOnMobile() {
    if (window.innerWidth < 992) {
      this.sidebarOpen = false;
    }
  }

  cargarBadges() {
    const vendedoresReq = this.http.get<any[]>(`${this.api}/vendedores`).pipe(
      catchError((err) => {
        console.error('Error cargando vendedores', err);
        return of([]);
      })
    );

    const disputasReq = this.disputaService.todasAdmin().pipe(
      catchError((err) => {
        console.error('Error cargando disputas', err);
        return of([]);
      })
    );

    forkJoin([vendedoresReq, disputasReq]).subscribe({
      next: ([vendedores, disputas]) => {
        this.vendedoresPendientes = (vendedores || []).filter(
          (v) => String(v?.estado || '').toLowerCase() === 'pendiente'
        ).length;

        this.disputasAbiertas = (disputas || []).filter(
          (d: any) => String(d?.estado || '').toLowerCase() === 'abierta'
        ).length;
      },
      error: (err) => {
        console.error('Error en badges', err);
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudieron cargar algunos indicadores',
          timer: 3000,
          showConfirmButton: false,
        });
      }
    });
  }

  esRutaActiva(url: string): boolean {
    return this.currentUrl === url;
  }

  navegarConProteccion(ruta: string): void {
    if (this.isNavigating) return;
    this.isNavigating = true;
    this.router.navigate([ruta]).finally(() => {
      setTimeout(() => (this.isNavigating = false), 500);
    });
  }

  logout() {
    if (this.isNavigating) return;
    this.isNavigating = true;

    Swal.fire({
      title: '¿Cerrar sesión?',
      text: '¿Estás seguro de que deseas salir?',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí, salir',
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      if (result.isConfirmed) {
        this.auth.logout();
        this.router.navigateByUrl('/cliente', { replaceUrl: true }).then(() => {
          this.isNavigating = false;
        });
      } else {
        this.isNavigating = false;
      }
    });
  }
}