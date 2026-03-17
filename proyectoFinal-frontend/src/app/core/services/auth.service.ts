import { Injectable, Inject, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { tap, catchError, switchMap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { SessionService } from './session.service';
import { StorageService } from './storage.service';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterPayload {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  tipoDocumento: string;
  numeroDocumento: string;
  telefono?: string;
  rol: string;
  nombreTienda?: string;
  ruc?: string;
}

export interface AuthResponse {
  token: string;
  refreshToken?: string;
  idUsuario: number;
  email: string;
  nombreCompleto: string;
  roles: string[];
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private session = inject(SessionService);
  private storage = inject(StorageService);
  private api = environment.apiUrl;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) { }

  /**
   * Verifica si está en el navegador
   */
  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  /**
   * Obtener token
   */
  token(): string | null {
    return this.storage.get('token');
  }

  /**
   * Verificar si está logueado
   */
  isLogged(): boolean {
    return !!this.token();
  }

  /**
   * Iniciar sesión - VERSIÓN CORREGIDA CON REFRESH
   */
  login(email: string, password: string): Observable<any> {
    console.log('🔵 [AUTH] 1. Intentando login con:', email);

    return this.http.post<any>(`${this.api}/auth/login`, { email, password })
      .pipe(
        tap(response => {
          console.log('🔵 [AUTH] 2. Respuesta del login:', response);

          if (this.isBrowser()) {
            // Guardar datos básicos en storage
            this.storage.set('token', response.token);
            this.storage.set('email', response.email);
            this.storage.set('nombreCompleto', response.nombreCompleto);

            if (response.refreshToken) {
              this.storage.set('refreshToken', response.refreshToken);
            }
          }
        }),
        // 👇 CRÍTICO: Obtener roles desde /me
        switchMap(() => this.session.refresh()),
        tap(session => {
          console.log('🔵 [AUTH] 3. Sesión después de refresh:', session);

          // Guardar usuario completo en localStorage para compatibilidad
          if (this.isBrowser() && session) {
            const user = {
              idUsuario: session.idUsuario,
              email: session.email,
              nombreCompleto: session.nombreCompleto,
              roles: session.roles
            };
            localStorage.setItem('user', JSON.stringify(user));
            console.log('🔵 [AUTH] 4. Usuario guardado en localStorage:', user);
          }
        })
      );
  }

  /**
   * Registrar nueva cuenta
   */
  register(payload: RegisterPayload): Observable<any> {
    return this.http.post<any>(`${this.api}/auth/register`, payload).pipe(
      tap(response => {
        console.log('🟢 [AUTH] Registro exitoso:', response);

        if (this.isBrowser()) {
          this.storage.set('token', response.token);
          this.storage.set('email', response.email);
          this.storage.set('nombreCompleto', response.nombreCompleto);

          if (response.refreshToken) {
            this.storage.set('refreshToken', response.refreshToken);
          }
        }
      }),
      switchMap(() => this.session.refresh())
    );
  }

  /**
   * Cerrar sesión
   */
  logout(): void {
    if (this.isBrowser()) {
      this.storage.clear();
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
    }
    this.session.clear();
  }

  /**
   * Obtener el token JWT (desde storage)
   */
  getToken(): string | null {
    if (!this.isBrowser()) {
      return null;
    }
    return this.storage.get('token');
  }

  /**
   * Obtener el usuario actual desde localStorage (para compatibilidad)
   */
  getCurrentUser(): any {
    if (!this.isBrowser()) {
      return null;
    }
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch (e) {
        console.error('Error parsing user from localStorage', e);
        return null;
      }
    }
    return null;
  }

  /**
   * Obtener el ID del usuario actual (desde sesión)
   */
  getCurrentUserId(): number | null {
    // 👇 PRIMERO INTENTAR DESDE SESSION (más confiable)
    const sessionUser = this.session.getCurrentUser();
    if (sessionUser?.idUsuario) {
      console.log('📌 [AUTH] ID desde sesión:', sessionUser.idUsuario);
      return sessionUser.idUsuario;
    }

    // 👇 FALLBACK a localStorage
    const user = this.getCurrentUser();
    console.log('📌 [AUTH] ID desde localStorage:', user?.idUsuario);
    return user?.idUsuario || null;
  }

  /**
   * Refrescar token
   */
  refreshToken(): Observable<any> {
    if (!this.isBrowser()) {
      return of(null);
    }

    const refreshToken = this.storage.get('refreshToken');
    return this.http.post(`${this.api}/auth/refresh`, { refreshToken })
      .pipe(
        tap((response: any) => {
          if (this.isBrowser()) {
            this.storage.set('token', response.token);
          }
        }),
        catchError(error => {
          this.logout();
          return of(error);
        })
      );
  }
}