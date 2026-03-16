import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class VendedorService {
  private api = environment.apiUrl;

  constructor(private http: HttpClient) { }

  // Dashboard
  obtenerDashboard() {
    return this.http.get<any>(`${this.api}/vendedores/dashboard`);
  }

  // Productos
  listarProductos() {
    return this.http.get<any[]>(`${this.api}/productos`);
  }

  crearProducto(data: any) {
    return this.http.post<any>(`${this.api}/productos`, data);
  }

  actualizarProducto(id: number, data: any) {
    return this.http.put<any>(`${this.api}/productos/${id}`, data);
  }

  eliminarProducto(id: number) {
    return this.http.delete(`${this.api}/productos/${id}`);
  }

  // Perfil de vendedor
  miPerfil() {
    return this.http.get<any>(`${this.api}/vendedores/perfil`);
  }

  subirLogo(file: File) {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<any>(`${this.api}/vendedores/perfil/logo`, fd);
  }
}