import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BusquedaStateService {
  // BehaviorSubject guarda el último valor y lo emite a nuevos suscriptores
  private terminoSubject = new BehaviorSubject<string>('');
  
  // Observable público para que los componentes se suscriban
  termino$: Observable<string> = this.terminoSubject.asObservable();

  constructor() { }

  /**
   * Actualiza el término de búsqueda y notifica a todos los suscriptores
   * @param termino El término a buscar
   */
  buscar(termino: string): void {
    this.terminoSubject.next(termino);
  }

  /**
   * Obtiene el valor actual del término sin suscribirse
   */
  get terminoActual(): string {
    return this.terminoSubject.value;
  }

  /**
   * Limpia el término de búsqueda
   */
  limpiar(): void {
    this.terminoSubject.next('');
  }
}