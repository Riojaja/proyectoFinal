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
   * Trae el usuario logueado desde el backend (/api/auth/me) y lo cachea.
   * Si falla (token inválido), limpia sesión.
   */
  refresh(): Observable<SessionInfo | null> {
    return this.http.get<any>(`${this.base}/me`).pipe(
      map(u => ({
        idUsuario: u.idUsuario,
        email: u.email,
        nombreCompleto: `${u.nombre ?? ''} ${u.apellido ?? ''}`.trim(),
        roles: Array.isArray(u.roles) ? u.roles : [],
        idVendedor: null
      })),
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

  clear() {
    this.save(null);
  }

  hasRole(role: string): boolean {
    const s = this.subject.value;
    return !!s?.roles?.includes(role);
  }
}