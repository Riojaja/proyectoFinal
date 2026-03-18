import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private base = `${environment.apiUrl}/productos`;

  constructor(private http: HttpClient) {}

  listar(filtros?: {
    categoria?: number | null;
    tipo?: string | null;
    limit?: number | null;
  }) {
    let params = new HttpParams();

    if (filtros?.categoria != null) {
      params = params.set('categoria', filtros.categoria);
    }

    if (filtros?.tipo) {
      params = params.set('tipo', filtros.tipo);
    }

    if (filtros?.limit != null) {
      params = params.set('limit', filtros.limit);
    }

    return this.http.get<any[]>(this.base, { params });
  }

  listarOfertas(limit = 8) {
    return this.http.get<any[]>(`${this.base}/ofertas`, {
      params: { limit }
    });
  }

  listarNuevos(limit = 8) {
    return this.http.get<any[]>(`${this.base}/nuevos`, {
      params: { limit }
    });
  }

  listarMasVendidos(limit = 8) {
    return this.http.get<any[]>(`${this.base}/mas-vendidos`, {
      params: { limit }
    });
  }

  misProductos() {
    return this.http.get<any[]>(`${this.base}/mis`);
  }

  obtener(id: number) {
    return this.http.get<any>(`${this.base}/${id}`);
  }

  crear(data: any) {
    return this.http.post<any>(this.base, data);
  }

  editar(id: number, data: any) {
    return this.http.put<any>(`${this.base}/${id}`, data);
  }

  eliminar(id: number) {
    return this.http.delete(`${this.base}/${id}`);
  }
}