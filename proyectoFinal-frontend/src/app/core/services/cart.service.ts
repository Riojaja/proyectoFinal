import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, forkJoin, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import { StorageService } from './storage.service';
import { AuthService } from './auth.service';
import { SessionService } from './session.service';

export type CartItem = {
  idItem?: number;
  idVariante: number;
  idProducto: number;
  nombre: string;
  sku?: string;
  precio: number;
  precioOferta?: number | null;
  imagen?: string | null;
  cantidad: number;
  stock?: number | null;
  atributos?: string[];
};

type CarritoDto = {
  idCarrito: number;
  idUsuario: number;
  totalItems: number;
  total: number;
  items: Array<{
    idItem: number;
    idVariante: number;
    sku: string;
    idProducto: number;
    nombreProducto: string;
    imagen: string | null;
    precio: number;
    precioOferta: number | null;
    stock: number | null;
    cantidad: number;
    subtotal: number;
    atributos?: string[];
  }>;
};

const KEY_GUEST = 'punamba_cart_guest';

@Injectable({ providedIn: 'root' })
export class CartService {
  private api = `${environment.apiUrl}/carrito`;

  private subject = new BehaviorSubject<CartItem[]>([]);
  items$ = this.subject.asObservable();

  constructor(
    private http: HttpClient,
    private storage: StorageService,
    private auth: AuthService,
    private session: SessionService,
  ) {
    this.subject.next(this.readGuest());

    this.session.session$.subscribe(s => {
      if (s && this.auth.isLogged()) {
        this.syncGuestToServer();
      } else {
        this.subject.next(this.readGuest());
      }
    });
  }

  private readGuest(): CartItem[] {
    try {
      return JSON.parse(this.storage.get(KEY_GUEST) || '[]');
    } catch {
      return [];
    }
  }

  private saveGuest(items: CartItem[]) {
    this.storage.set(KEY_GUEST, JSON.stringify(items));
    this.subject.next(items);
  }

  private clearGuest() {
    this.storage.remove(KEY_GUEST);
  }

  refresh() {
    if (this.auth.isLogged()) {
      return this.loadServer().subscribe();
    }

    this.subject.next(this.readGuest());
    return of(null).subscribe();
  }

  private loadServer() {
    return this.http.get<CarritoDto>(`${this.api}/mi-carrito`).pipe(
      map(dto => (dto?.items || []).map(it => this.mapItem(it))),
      tap(items => this.subject.next(items)),
      catchError(() => {
        this.subject.next(this.readGuest());
        return of([]);
      })
    );
  }

  private syncGuestToServer() {
    const guest = this.readGuest();

    this.loadServer().subscribe({
      next: () => {
        if (!guest.length) return;

        const reqs = guest.map(g =>
          this.http.post<CarritoDto>(`${this.api}/agregar`, {
            idVariante: g.idVariante,
            cantidad: g.cantidad,
          }).pipe(catchError(() => of(null)))
        );

        forkJoin(reqs).subscribe({
          next: () => {
            this.clearGuest();
            this.loadServer().subscribe();
          },
          error: () => {
            this.loadServer().subscribe();
          }
        });
      }
    });
  }

  private mapItem(it: CarritoDto['items'][number]): CartItem {
    return {
      idItem: it.idItem,
      idVariante: it.idVariante,
      idProducto: it.idProducto,
      nombre: it.nombreProducto,
      sku: it.sku,
      precio: Number(it.precio || 0),
      precioOferta: it.precioOferta != null ? Number(it.precioOferta) : null,
      imagen: it.imagen,
      cantidad: Number(it.cantidad || 1),
      stock: it.stock,
      atributos: Array.isArray(it.atributos) ? it.atributos : [],
    };
  }

  getItems(): CartItem[] {
    return this.subject.value;
  }

  add(product: any) {
    const variantes = product?.variantes || [];

    const v =
      product?.varianteSeleccionada ||
      variantes.find((x: any) => x?.activo !== false) ||
      variantes[0];

    const idVariante = v?.idVariante ?? v?.id_variante;
    const idProducto = product?.idProducto ?? product?.id_producto ?? product?.id;
    const cantidad = Math.max(1, Math.floor(Number(product?.cantidad || 1)));

    if (!idVariante) {
      console.warn('Producto sin variante. Crea al menos 1 variante para este producto.', product);
      return;
    }

    const fallback: Partial<CartItem> = {
      idVariante,
      idProducto: Number(idProducto || 0),
      nombre: String(product?.nombre || 'Producto'),
      precio: Number(
        v?.precio ?? product?.precioBase ?? product?.precio_base ?? product?.precio ?? 0
      ),
      precioOferta: v?.precioOferta != null ? Number(v.precioOferta) : null,
      sku: v?.sku,
      imagen:
        product?.imagenes?.find((x: any) => x?.esPrincipal)?.urlImagen ||
        product?.imagenes?.[0]?.urlImagen ||
        product?.imagenes?.[0]?.url_imagen ||
        null,
      cantidad,
      atributos: Array.isArray(v?.atributos) ? v.atributos : [],
    };

    this.addVariante(idVariante, cantidad, fallback);
  }

  addVariante(idVariante: number, cantidad = 1, fallback?: Partial<CartItem>) {
    const cant = Math.max(1, Math.floor(Number(cantidad || 1)));

    if (this.auth.isLogged()) {
      this.http.post<CarritoDto>(`${this.api}/agregar`, { idVariante, cantidad: cant }).subscribe({
        next: (dto) => {
          const items = (dto?.items || []).map(it => this.mapItem(it));
          this.subject.next(items);
        },
        error: () => {
          this.addGuest(idVariante, cant, fallback);
        }
      });
      return;
    }

    this.addGuest(idVariante, cant, fallback);
  }

  private addGuest(idVariante: number, cantidad: number, fallback?: Partial<CartItem>) {
    const cant = Math.max(1, Math.floor(Number(cantidad || 1)));
    const items = this.readGuest();
    const found = items.find(x => x.idVariante === idVariante);

    if (found) {
      found.cantidad += cant;
      this.saveGuest([...items]);
      return;
    }

    const safe: CartItem = {
      idVariante,
      idProducto: Number(fallback?.idProducto || 0),
      nombre: String(fallback?.nombre || 'Producto'),
      sku: fallback?.sku,
      precio: Number(fallback?.precio || 0),
      precioOferta: fallback?.precioOferta != null ? Number(fallback.precioOferta) : null,
      imagen: fallback?.imagen ?? null,
      cantidad: cant,
      stock: fallback?.stock ?? null,
      atributos: fallback?.atributos || [],
    };

    this.saveGuest([safe, ...items]);
  }

  setQty(idVariante: number, qty: number) {
    const n = Math.max(1, Math.floor(Number(qty || 1)));

    if (this.auth.isLogged()) {
      this.http.put<CarritoDto>(`${this.api}/item/${idVariante}`, { cantidad: n }).subscribe({
        next: (dto) => {
          const items = (dto?.items || []).map(it => this.mapItem(it));
          this.subject.next(items);
        },
        error: () => {
          // Si el backend no soporta PUT, intentamos recargar.
          this.loadServer().subscribe({
            error: () => {
              const items = this.readGuest().map(x =>
                x.idVariante === idVariante ? { ...x, cantidad: n } : x
              );
              this.saveGuest(items);
            }
          });
        }
      });
      return;
    }

    const items = this.readGuest().map(x =>
      x.idVariante === idVariante ? { ...x, cantidad: n } : x
    );
    this.saveGuest(items);
  }

  remove(idVariante: number) {
    if (this.auth.isLogged()) {
      this.http.delete(`${this.api}/item/${idVariante}`).subscribe({
        next: () => this.loadServer().subscribe(),
        error: () => {
          this.saveGuest(this.readGuest().filter(x => x.idVariante !== idVariante));
        }
      });
      return;
    }

    this.saveGuest(this.readGuest().filter(x => x.idVariante !== idVariante));
  }

  clear() {
    if (this.auth.isLogged()) {
      this.http.delete(`${this.api}/limpiar`).subscribe({
        next: () => this.subject.next([]),
        error: () => {
          this.saveGuest([]);
        }
      });
      return;
    }

    this.saveGuest([]);
  }

  total(): number {
    return this.getItems().reduce((acc, x) => {
      const precioFinal =
        x.precioOferta != null && Number(x.precioOferta) > 0
          ? Number(x.precioOferta)
          : Number(x.precio || 0);

      return acc + precioFinal * Number(x.cantidad || 1);
    }, 0);
  }

  count(): number {
    return this.getItems().length;
  }

  countCantidad(): number {
    return this.getItems().reduce((acc, i) => acc + Number(i.cantidad || 0), 0);
  }
}
