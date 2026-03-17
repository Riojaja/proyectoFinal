import { Component, Input, OnInit, OnDestroy, Inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FavoritoService } from '../../../core/services/favorito';
import { AuthService } from '../../../core/services/auth.service';
import { UiModalService } from '../../../core/services/ui-modal.service';
import Swal from 'sweetalert2';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-favorito-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button class="favorito-btn" 
            (click)="toggleFavorito($event)" 
            [disabled]="procesando"
            [class.active]="esFavorito">
      <i class="bi" [ngClass]="esFavorito ? 'bi-heart-fill' : 'bi-heart'"></i>
    </button>
  `,
  styles: [`
    .favorito-btn {
      background: white;
      border: none;
      font-size: 1.3rem;
      color: #ddd;
      cursor: pointer;
      transition: all 0.2s ease;
      padding: 0;
      width: 36px;
      height: 36px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      z-index: 20;
    }
    .favorito-btn.active {
      color: #ff5a00;
    }
    .favorito-btn:not(.active):hover {
      color: #ff8c5a;
      transform: scale(1.1);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    .favorito-btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  `]
})
export class FavoritoButtonComponent implements OnInit, OnDestroy {
  @Input() productoId!: number;
  esFavorito = false;
  procesando = false;
  private destroy$ = new Subject<void>();

  constructor(
    private favoritoService: FavoritoService,
    private auth: AuthService,
    private uiModal: UiModalService,
    private cdr: ChangeDetectorRef, // 👈 AÑADIDO para forzar detección de cambios
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    if (this.isBrowser() && this.auth.isLogged()) {
      this.checkFavorito();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  checkFavorito(): void {
    this.favoritoService.verificarFavorito(this.productoId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res: boolean) => {
          this.esFavorito = res;
          this.cdr.detectChanges(); // 👈 FORZAR DETECCIÓN DE CAMBIOS
        },
        error: (err) => {
          console.error('Error verificando favorito:', err);
          this.cdr.detectChanges(); // 👈 TAMBIÉN EN CASO DE ERROR
        }
      });
  }

  toggleFavorito(event: Event): void {
    event.stopPropagation();
    console.log('🔴 [FAVORITO] 1. Click en corazón para producto:', this.productoId);

    if (!this.isBrowser()) {
      console.log('🔴 [FAVORITO] 2. No estamos en navegador');
      return;
    }

    if (!this.auth.isLogged()) {
      console.log('🔴 [FAVORITO] 3. Usuario no logueado, abriendo modal');
      this.uiModal.abrirLogin();
      return;
    }

    if (this.procesando) {
      console.log('🔴 [FAVORITO] 4. Ya está procesando');
      return;
    }

    console.log('🔴 [FAVORITO] 5. Estado actual antes de toggle:', this.esFavorito);
    this.procesando = true;

    this.favoritoService.toggleFavorito(this.productoId)
      .subscribe({
        next: (esFavoritoAhora: boolean) => {
          console.log('🔴 [FAVORITO] 6. Respuesta del servicio:', esFavoritoAhora);
          this.esFavorito = esFavoritoAhora;
          this.procesando = false;
          this.cdr.detectChanges(); // 👈 FORZAR DETECCIÓN DE CAMBIOS AQUÍ

          Swal.fire({
            icon: 'success',
            title: esFavoritoAhora ? 'Añadido a favoritos' : 'Eliminado de favoritos',
            timer: 1500,
            showConfirmButton: false,
            toast: true,
            position: 'top-end'
          });

          this.favoritoService.actualizarContador();
        },
        error: (err) => {
          console.error('🔴 [FAVORITO] 7. Error:', err);
          this.procesando = false;
          this.cdr.detectChanges(); // 👈 TAMBIÉN EN CASO DE ERROR
          
          Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'No se pudo completar la operación',
            timer: 2000,
            showConfirmButton: false
          });
        }
      });
  }
}