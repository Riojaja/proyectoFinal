import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PagoService {
  private api = `${environment.apiUrl}/pagos`;

  constructor(private http: HttpClient) {}

  listarMetodos() {
    return this.http.get<any[]>(`${this.api}/metodos`);
  }

  confirmarPago(payload: {
    idOrden: string;
    idMetodo: string;
    nroOperacion: string;
  }) {
    return this.http.post(`${this.api}/confirmar`, payload);
  }
}