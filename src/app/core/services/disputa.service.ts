import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export type EstadoDisputa =
  | 'abierta'
  | 'en_revision'
  | 'en_negociacion'
  | 'resuelta_reembolso_total'
  | 'resuelta_reembolso_parcial'
  | 'resuelta_rechazada'
  | 'cerrada';

export type RolEmisor = 'cliente' | 'vendedor' | 'admin';

export type Disputa = {
  idDisputa: number;
  idOrden: number;
  numeroOrden: string;
  idOrdenDetalle: number;
  sku: string;
  usuarioEmail: string;
  clienteNombre: string;
  vendedorNombre: string;
  vendedorRuc: string;
  nombreProducto: string;
  motivo: string;
  descripcion: string;
  evidenciaJson?: string;
  estado: EstadoDisputa;
  montoReembolso: number;
  observacionAdmin?: string;
  resolucionFinal?: string;
  fechaApertura?: string;
  fechaResolucion?: string;
  fechaCierre?: string;
};

export type DisputaMensaje = {
  idMensaje: number;
  idDisputa: number;
  idUsuario: number;
  usuarioEmail: string;
  nombreCompleto: string;
  rolEmisor: RolEmisor;
  mensaje: string;
  adjuntoUrl?: string;
  fechaEnvio: string;
};

@Injectable({ providedIn: 'root' })
export class DisputaService {
  private api = `${environment.apiUrl}/disputas`;

  constructor(private http: HttpClient) {}

  abrir(payload: {
    idOrdenDetalle: number;
    motivo: string;
    descripcion: string;
    evidenciaJson?: string | null;
  }) {
    return this.http.post<Disputa>(`${this.api}/abrir`, payload);
  }

  misDisputas() {
    return this.http.get<Disputa[]>(`${this.api}/mis-disputas`);
  }

  todasAdmin() {
    return this.http.get<Disputa[]>(`${this.api}/admin/todas`);
  }

  obtener(id: number) {
    return this.http.get<Disputa>(`${this.api}/${id}`);
  }

  listarMensajes(idDisputa: number) {
    return this.http.get<DisputaMensaje[]>(`${this.api}/${idDisputa}/mensajes`);
  }

  enviarMensaje(
    idDisputa: number,
    payload: { mensaje: string; adjuntoUrl?: string }
  ) {
    return this.http.post<DisputaMensaje>(`${this.api}/${idDisputa}/mensajes`, payload);
  }

  resolver(
    idDisputa: number,
    payload: {
      nuevoEstado: EstadoDisputa;
      montoReembolso?: number | null;
      observacionAdmin?: string;
      resolucionFinal?: string;
    }
  ) {
    return this.http.patch<Disputa>(`${this.api}/${idDisputa}/resolver`, payload);
  }
}