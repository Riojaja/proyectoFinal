import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private base = `${environment.apiUrl}/categorias`;

  constructor(private http: HttpClient) {}

  listarPrincipales() {
    return this.http.get<any[]>(this.base);
  }

  listarTodas() {
    return this.http.get<any[]>(`${this.base}/todas`);
  }

  listarPromos(limit = 3) {
    return this.http.get<any[]>(`${this.base}/promos`, {
      params: { limit }
    });
  }
}
