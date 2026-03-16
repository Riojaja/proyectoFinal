import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface VentaPeriodo {
  periodo: number;
  total: number;
}

export interface AdminDashboardResponse {
  gmv: number;
  comisiones: number;
  vendedoresPendientes: number;
  categoriasActivas: number;
  vendedores: {
    idVendedor: number;
    nombre: string;
    logoUrl: string | null;
    totalVentas: number;
  }[];
  ventasMensuales: VentaPeriodo[];
  ventasSemanales: VentaPeriodo[];
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private api = environment.apiUrl;

  constructor(private http: HttpClient) { }

  // =========================
  // DASHBOARD
  // =========================
  obtenerDashboard(): Observable<AdminDashboardResponse> {
    return this.http.get<AdminDashboardResponse>(`${this.api}/admin/dashboard`);
  }

  // =========================
  // USUARIOS
  // =========================
  listarUsuarios(): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/usuarios`);
  }

  actualizarUsuario(id: number, data: any): Observable<any> {
    return this.http.put<any>(`${this.api}/usuarios/${id}`, data);
  }

  eliminarUsuario(id: number): Observable<any> {
    return this.http.delete<any>(`${this.api}/usuarios/${id}`);
  }

  // =========================
  // CATEGORÍAS
  // =========================
  listarCategorias(): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/categorias/todas`);
  }

  crearCategoria(data: any): Observable<any> {
    return this.http.post<any>(`${this.api}/categorias`, data);
  }

  editarCategoria(id: number, data: any): Observable<any> {
    return this.http.put<any>(`${this.api}/categorias/${id}`, data);
  }

  eliminarCategoria(id: number): Observable<any> {
    return this.http.delete<any>(`${this.api}/categorias/${id}`);
  }

  // =========================
  // PRODUCTOS
  // =========================
  listarProductos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/productos`);
  }

  actualizarProducto(id: number, data: any): Observable<any> {
    return this.http.put<any>(`${this.api}/productos/${id}`, data);
  }

  // =========================
  // VENDEDORES
  // =========================
  listarVendedores(): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/vendedores`);
  }

  // =========================
  // LIQUIDACIONES
  // =========================
  listarLiquidaciones(): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/liquidaciones`);
  }

  // =========================
  // ICONOS CATEGORIA
  // =========================
  listarIconosCategoria(): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/iconos-categoria/activos`);
  }

  listarTodosIconosCategoria(): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/iconos-categoria`);
  }

  crearIconoCategoria(data: any): Observable<any> {
    return this.http.post<any>(`${this.api}/iconos-categoria`, data);
  }

  editarIconoCategoria(id: number, data: any): Observable<any> {
    return this.http.put<any>(`${this.api}/iconos-categoria/${id}`, data);
  }

  eliminarIconoCategoria(id: number): Observable<any> {
    return this.http.delete<any>(`${this.api}/iconos-categoria/${id}`);
  }

  // =========================
  // MARCAS
  // =========================
  listarMarcas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/marcas`);
  }

  listarMarcasActivas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/marcas/activas`);
  }

  crearMarca(data: { nombre: string; estado: boolean }): Observable<any> {
    return this.http.post<any>(`${this.api}/marcas`, data);
  }

  editarMarca(id: number, data: { nombre: string; estado: boolean }): Observable<any> {
    return this.http.put<any>(`${this.api}/marcas/${id}`, data);
  }

  listarMarcasPorCategoria(idCategoria: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/categorias/${idCategoria}/marcas`);
  }

  asignarMarcaACategoria(data: { idCategoria: number; idMarca: number }): Observable<any> {
    return this.http.post<any>(`${this.api}/categorias/marcas`, data);
  }

  quitarMarcaDeCategoria(idCategoria: number, idMarca: number): Observable<any> {
    return this.http.delete<any>(`${this.api}/categorias/${idCategoria}/marcas/${idMarca}`);
  }

  // =========================
  // ATRIBUTOS
  // =========================
  listarAtributos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/atributos`);
  }

  crearAtributo(data: { nombre: string; tipoDato: string; unidad: string | null }): Observable<any> {
    return this.http.post<any>(`${this.api}/atributos`, data);
  }

  editarAtributo(id: number, data: { nombre: string; tipoDato: string; unidad: string | null }): Observable<any> {
    return this.http.put<any>(`${this.api}/atributos/${id}`, data);
  }

  listarAtributosPorCategoria(idCategoria: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/categorias/${idCategoria}/atributos`);
  }

  asignarAtributoACategoria(data: {
    idCategoria: number;
    idAtributo: number;
    obligatorio: boolean;
    orden: number;
  }): Observable<any> {
    return this.http.post<any>(`${this.api}/categorias/atributos`, data);
  }

  editarAtributoDeCategoria(data: {
    idCategoria: number;
    idAtributo: number;
    obligatorio: boolean;
    orden: number;
  }): Observable<any> {
    return this.http.put<any>(`${this.api}/categorias/atributos`, data);
  }

  quitarAtributoDeCategoria(idCategoria: number, idAtributo: number): Observable<any> {
    return this.http.delete<any>(`${this.api}/categorias/${idCategoria}/atributos/${idAtributo}`);
  }

  // =========================
  // VALORES DE ATRIBUTO
  // =========================
  listarValoresPorAtributo(idAtributo: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/atributo-valores/atributo/${idAtributo}`);
  }

  crearValorAtributo(data: { idAtributo: number; valor: string }): Observable<any> {
    return this.http.post<any>(`${this.api}/atributo-valores`, data);
  }
}