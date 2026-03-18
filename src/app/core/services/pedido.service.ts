import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PedidoService {
  private api = `${environment.apiUrl}/ordenes`;

  constructor(private http: HttpClient) {}

  checkout(data: any) {
    return this.http.post(`${this.api}/checkout`, data);
  }

  obtenerPedido(id: number) {
    return this.http.get(`${this.api}/${id}`);
  }

  misPedidos() {
    return this.http.get(`${this.api}/mis-ordenes`);
  }
}