import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { tap, switchMap } from 'rxjs/operators';
import { StorageService } from './storage.service';
import { SessionService } from './session.service';

export type RegisterPayload = {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  tipoDocumento: string;
  numeroDocumento: string;
  telefono?: string;
  rol?: string;
  nombreTienda?: string;
  ruc?: string;
};

@Injectable({ providedIn: 'root' })
export class AuthService {
  private base = `${environment.apiUrl}/auth`;

  constructor(
    private http: HttpClient,
    private storage: StorageService,
    private session: SessionService,
  ) { }

  token(): string | null { return this.storage.get('token'); }
  isLogged(): boolean { return !!this.token(); }

  login(email: string, password: string) {
    return this.http.post<any>(`${this.base}/login`, { email, password }).pipe(
      tap(res => {
        this.storage.set('token', res.token);
        this.storage.set('email', res.email);
        this.storage.set('nombreCompleto', res.nombreCompleto);
      }),
      switchMap(() => this.session.refresh())
    );
  }

  register(payload: RegisterPayload) {
    return this.http.post<any>(`${this.base}/register`, payload).pipe(
      tap(res => {
        this.storage.set('token', res.token);
        this.storage.set('email', res.email);
        this.storage.set('nombreCompleto', res.nombreCompleto);
      }),
      switchMap(() => this.session.refresh())
    );
  }

  logout() {
    this.storage.clear();
    this.session.clear();
  }
}