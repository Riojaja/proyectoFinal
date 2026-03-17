import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class BusquedaService {
  private api = environment.apiUrl;

  constructor(private http: HttpClient) {}

  buscar(termino: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/productos`, {
      params: { 
        q: termino,
        limit: '20'
      }
    });
  }
}