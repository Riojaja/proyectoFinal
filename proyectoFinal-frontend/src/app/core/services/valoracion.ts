import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ValoracionResumen {
  promedio: number;
  total: number;
  distribucion: { [key: number]: number };
}

@Injectable({ providedIn: 'root' })
export class ValoracionService {
  private api = environment.apiUrl;

  constructor(private http: HttpClient) {}

  obtenerValoraciones(productoId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/valoraciones/producto/${productoId}`);
  }

  obtenerResumen(productoId: number): Observable<ValoracionResumen> {
    return this.obtenerValoraciones(productoId).pipe(
      map(valoraciones => {
        const total = valoraciones.length;
        if (total === 0) {
          return { promedio: 0, total: 0, distribucion: {} };
        }

        const suma = valoraciones.reduce((acc: number, v: any) => acc + v.calificacion, 0);
        const promedio = suma / total;

        // Inicializar distribución con todas las claves posibles
        const distribucion: { [key: number]: number } = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
        
        valoraciones.forEach((v: any) => {
          const calif = v.calificacion;
          // Asegurar que la calificación esté en el rango 1-5
          if (calif >= 1 && calif <= 5) {
            distribucion[calif] = (distribucion[calif] || 0) + 1;
          }
        });

        return { promedio, total, distribucion };
      })
    );
  }

  crearValoracion(data: any): Observable<any> {
    return this.http.post(`${this.api}/valoraciones`, data);
  }
}