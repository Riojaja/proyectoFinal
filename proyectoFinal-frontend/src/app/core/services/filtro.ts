import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FiltroService {
  
  private filtroSubject = new BehaviorSubject<string>('mas-vendidos');
  
  filtro$: Observable<string> = this.filtroSubject.asObservable();

  constructor() { }

  /**
   * Cambia el filtro actual y notifica a todos los suscriptores
   * @param filtro El nuevo filtro ('mas-vendidos', 'ofertas', 'nuevos', etc.)
   */
  cambiarFiltro(filtro: string): void {
    this.filtroSubject.next(filtro);
  }

  /**
   * Obtiene el valor actual del filtro sin suscribirse
   */
  get filtroActual(): string {
    return this.filtroSubject.value;
  }
}