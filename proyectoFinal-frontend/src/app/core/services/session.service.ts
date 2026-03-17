import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { StorageService } from './storage.service';

export type SessionInfo = {
  idUsuario: number;
  email: string;
  nombreCompleto: string;
  roles: string[]; // ["ADMIN", "VENDEDOR", "CLIENTE"]
  idVendedor?: number | null;
  token?: string; // Mantener como string | undefined, no null
};

const KEY_SESSION = 'punamba_session';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private base = `${environment.apiUrl}/usuarios`;
  private subject = new BehaviorSubject<SessionInfo | null>(this.read());
  session$ = this.subject.asObservable();

  constructor(private http: HttpClient, private storage: StorageService) { }

  private read(): SessionInfo | null {
    try {
      const raw = this.storage.get(KEY_SESSION);
      return raw ? (JSON.parse(raw) as SessionInfo) : null;
    } catch {
      return null;
    }
  }

  private save(session: SessionInfo | null) {
    if (!session) {
      this.storage.remove(KEY_SESSION);
      this.subject.next(null);
      return;
    }
    this.storage.set(KEY_SESSION, JSON.stringify(session));
    this.subject.next(session);
  }

  /**
   * Establece la sesión manualmente (después de login)
   */
  setSession(session: SessionInfo): void {
    this.save(session);
  }

  /**
   * Trae el usuario logueado desde el backend (/api/auth/me) y lo cachea.
   * Si falla (token inválido), limpia sesión.
   */
  refresh(): Observable<SessionInfo | null> {
    return this.http.get<any>(`${this.base}/me`).pipe(
      map(u => {
        const token = this.getTokenFromStorage();
        return {
          idUsuario: u.idUsuario,
          email: u.email,
          nombreCompleto: `${u.nombre ?? ''} ${u.apellido ?? ''}`.trim(),
          roles: Array.isArray(u.roles) ? u.roles : [],
          idVendedor: u.idVendedor || null,
          token: token || undefined // Convertir null a undefined
        };
      }),
      tap(s => this.save(s)),
      catchError(() => {
        this.save(null);
        return of(null);
      })
    );
  }

  /**
   * Útil para guards: devuelve sesión si ya existe, si no intenta refresh.
   */
  ensure(): Observable<SessionInfo | null> {
    const current = this.subject.value;
    if (current) return of(current);
    return this.refresh();
  }

  /**
   * Obtiene la sesión actual (síncrono)
   */
  getCurrentSession(): SessionInfo | null {
    return this.subject.value;
  }

  /**
   * Obtiene el usuario actual (alias de getCurrentSession)
   */
  getCurrentUser(): SessionInfo | null {
    return this.getCurrentSession();
  }

  /**
   * Obtiene el ID del usuario actual
   */
  getCurrentUserId(): number | null {
    return this.subject.value?.idUsuario || null;
  }

  /**
   * Obtiene los roles del usuario actual
   */
  getCurrentUserRoles(): string[] {
    return this.subject.value?.roles || [];
  }

  /**
   * Verifica si el usuario tiene un rol específico
   */
  hasRole(role: string): boolean {
    const s = this.subject.value;
    return !!s?.roles?.includes(role);
  }

  /**
   * Verifica si el usuario tiene alguno de los roles especificados
   */
  hasAnyRole(roles: string[]): boolean {
    const userRoles = this.getCurrentUserRoles();
    return roles.some(role => userRoles.includes(role));
  }

  /**
   * Verifica si el usuario tiene todos los roles especificados
   */
  hasAllRoles(roles: string[]): boolean {
    const userRoles = this.getCurrentUserRoles();
    return roles.every(role => userRoles.includes(role));
  }

  /**
   * Obtiene el token del localStorage
   */
  private getTokenFromStorage(): string | null {
    return localStorage.getItem('token');
  }

  /**
   * Limpia la sesión
   */
  clear(): void {
    this.save(null);
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  }

  /**
   * Verifica si hay una sesión activa
   */
  isLogged(): boolean {
    return !!this.subject.value && !!this.getTokenFromStorage();
  }
}