import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';
import Swal from 'sweetalert2';

@Injectable({ providedIn: 'root' })
export class FavoritoService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private api = environment.apiUrl;

  private favoritosCountSubject = new BehaviorSubject<number>(0);
  favoritosCount$ = this.favoritosCountSubject.asObservable();

  /**
   * Obtiene todos los favoritos del usuario actual
   */
  misFavoritos(): Observable<any[]> {
    const usuarioId = this.auth.getCurrentUserId();
    if (!usuarioId) {
      return of([]);
    }

    return this.http.get<any[]>(`${this.api}/favoritos/usuario/${usuarioId}`)
      .pipe(
        tap(favs => this.favoritosCountSubject.next(favs.length)),
        catchError(err => {
          console.error('Error cargando favoritos:', err);
          return of([]);
        })
      );
  }

  /**
   * 👈 MÉTODO QUE FALTABA - Verifica si un producto es favorito
   */
  verificarFavorito(productoId: number): Observable<boolean> {
    console.log('🟢 [FAVORITO SERVICE] verificarFavorito para producto:', productoId);

    const usuarioId = this.auth.getCurrentUserId();
    console.log('🟢 [FAVORITO SERVICE] Usuario ID obtenido:', usuarioId);

    if (!usuarioId) {
      console.log('🟢 [FAVORITO SERVICE] Usuario no autenticado');
      return of(false);
    }

    return this.http.get<boolean>(
      `${this.api}/favoritos/verificar/usuario/${usuarioId}/producto/${productoId}`
    ).pipe(
      tap(res => console.log('🟢 [FAVORITO SERVICE] Resultado verificación:', res)),
      catchError(err => {
        console.error('🟢 [FAVORITO SERVICE] Error en verificación:', err);
        return of(false);
      })
    );
  }


  /**
   * Agrega un producto a favoritos
   */
  agregarFavorito(productoId: number): Observable<any> {
    const usuarioId = this.auth.getCurrentUserId();
    if (!usuarioId) {
      this.mostrarErrorLogin();
      return of(null);
    }

    return this.http.post(`${this.api}/favoritos`, {
      idUsuario: usuarioId,
      idProducto: productoId
    }).pipe(
      tap(() => {
        this.actualizarContador();
        this.mostrarMensaje('Añadido a favoritos', 'success');
      }),
      catchError(err => {
        this.mostrarMensaje('Error al añadir a favoritos', 'error');
        return of(null);
      })
    );
  }

  /**
   * Elimina un producto de favoritos
   */
  eliminarFavorito(productoId: number): Observable<any> {
    const usuarioId = this.auth.getCurrentUserId();
    if (!usuarioId) {
      return of(null);
    }

    return this.http.delete(
      `${this.api}/favoritos/usuario/${usuarioId}/producto/${productoId}`
    ).pipe(
      tap(() => {
        this.actualizarContador(); // 👈 ESTO ACTUALIZA LA LISTA
        this.mostrarMensaje('Eliminado de favoritos', 'info');
      }),
      catchError(err => {
        this.mostrarMensaje('Error al eliminar de favoritos', 'error');
        return of(null);
      })
    );
  }

  /**
   * Alterna el estado de favorito (agrega/elimina)
   */
  toggleFavorito(productoId: number): Observable<boolean> {
    console.log('🟢 [FAVORITO SERVICE] 1. toggleFavorito llamado para producto:', productoId);

    const usuarioId = this.auth.getCurrentUserId();
    console.log('🟢 [FAVORITO SERVICE] 2. Usuario ID:', usuarioId);

    if (!usuarioId) {
      console.log('🟢 [FAVORITO SERVICE] 3. Usuario no autenticado');
      return of(false);
    }

    return this.verificarFavorito(productoId).pipe(
      map(esFavorito => {
        console.log('🟢 [FAVORITO SERVICE] 4. Estado actual en BD:', esFavorito);
        if (esFavorito) {
          console.log('🟢 [FAVORITO SERVICE] 5. Eliminando favorito...');
          this.eliminarFavorito(productoId).subscribe({
            next: () => console.log('✅ Eliminado correctamente'),
            error: err => console.error('❌ Error al eliminar:', err)
          });
          return false;
        } else {
          console.log('🟢 [FAVORITO SERVICE] 6. Agregando favorito...');
          this.agregarFavorito(productoId).subscribe({
            next: () => console.log('✅ Agregado correctamente'),
            error: err => console.error('❌ Error al agregar:', err)
          });
          return true;
        }
      })
    );
  }

  /**
   * Actualiza el contador de favoritos
   */
  actualizarContador(): void {
    const usuarioId = this.auth.getCurrentUserId();
    if (usuarioId) {
      this.http.get<any[]>(`${this.api}/favoritos/usuario/${usuarioId}`)
        .subscribe({
          next: (favs) => {
            // Solo actualizar si el valor cambió
            if (favs.length !== this.favoritosCountSubject.value) {
              this.favoritosCountSubject.next(favs.length);
            }
          },
          error: () => { }
        });
    }
  }

  /**
   * Muestra mensaje de error de autenticación
   */
  private mostrarErrorLogin(): void {
    Swal.fire({
      icon: 'warning',
      title: 'Inicia sesión',
      text: 'Debes iniciar sesión para usar favoritos',
      timer: 2000,
      showConfirmButton: false
    });
  }

  /**
   * Muestra mensaje de éxito/error
   */
  private mostrarMensaje(texto: string, tipo: 'success' | 'error' | 'info'): void {
    Swal.fire({
      icon: tipo,
      title: texto,
      timer: 1500,
      showConfirmButton: false,
      toast: true,
      position: 'top-end'
    });
  }
}