import { Component, Input, Output, EventEmitter, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ValoracionService } from '../../../core/services/valoracion';

@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="star-rating" [class.interactive]="interactive">
      <span *ngFor="let star of [1,2,3,4,5]; let i = index"
            class="star"
            [class.filled]="i < valorRedondeado"
            (click)="interactive && seleccionar(i + 1)"
            (mouseenter)="interactive && hover(i + 1)"
            (mouseleave)="interactive && hover(0)">
        <i class="bi" [ngClass]="{
          'bi-star-fill': i < valorRedondeado,
          'bi-star': i >= valorRedondeado
        }"></i>
      </span>
      <span class="rating-text" *ngIf="mostrarTexto && cargado">
        {{ valor | number:'1.1-1' }} ({{ total }} {{ total === 1 ? 'reseña' : 'reseñas' }})
      </span>
      <span class="rating-text" *ngIf="mostrarTexto && !cargado">Cargando...</span>
    </div>
  `,
  styles: [`
    .star-rating {
      display: flex;
      align-items: center;
      gap: 2px;
    }
    .star {
      font-size: 1rem;
      cursor: default;
    }
    .interactive .star {
      cursor: pointer;
      transition: transform 0.2s ease;
    }
    .interactive .star:hover {
      transform: scale(1.2);
    }
    .star i {
      color: #ffc107;
    }
    .rating-text {
      margin-left: 8px;
      font-size: 0.8rem;
      color: #6c757d;
    }
  `]
})
export class StarRatingComponent implements OnInit {
  @Input() productoId?: number;
  @Input() valor: number = 0;
  @Input() total: number = 0;
  @Input() mostrarTexto: boolean = true;
  @Input() interactive: boolean = false;
  @Output() valorChange = new EventEmitter<number>();

  cargado = true;
  valorRedondeado = 0;

  constructor(
    private valoracionService: ValoracionService,
    private cdr: ChangeDetectorRef // 👈 Inyectamos ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.valorRedondeado = Math.round(this.valor);
    
    if (this.productoId && this.valor === 0 && this.total === 0) {
      this.cargarValoraciones();
    }
  }

  ngOnChanges(): void {
    this.valorRedondeado = Math.round(this.valor);
  }

  cargarValoraciones(): void {
    if (!this.productoId) return;
    
    this.cargado = false; // 👈 Cambiamos el estado inmediatamente
    
    this.valoracionService.obtenerResumen(this.productoId).subscribe({
      next: (resumen) => {
        this.valor = resumen.promedio;
        this.total = resumen.total;
        this.valorRedondeado = Math.round(this.valor);
        this.cargado = true; // 👈 Actualizamos al recibir datos
        
        // 👇 FORZAMOS LA DETECCIÓN DE CAMBIOS
        this.cdr.detectChanges();
      },
      error: () => {
        this.cargado = true;
        this.cdr.detectChanges(); // 👈 También en caso de error
      }
    });
  }

  hover(valor: number): void {}

  seleccionar(valor: number): void {
    this.valor = valor;
    this.valorRedondeado = valor;
    this.valorChange.emit(valor);
  }
}