import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { ProductoService } from '../../../../core/services/producto.service';
import Swal from 'sweetalert2';

type Categoria = { idCategoria: number; nombre: string };
type Marca = { idMarca: number; nombre: string; estado?: boolean };
type AtributoOpcion = { idValor: number; valor: string };
type CategoriaAtributo = {
  idAtributo: number;
  nombre: string;
  tipoDato: string;
  unidad?: string | null;
  obligatorio: boolean;
  orden: number;
  opciones: AtributoOpcion[];
};
type ProductoImagen = { idImagen?: number; urlImagen: string; esPrincipal?: boolean; orden?: number };
type Producto = any;

@Component({
  standalone: true,
  selector: 'app-vendedor-productos',
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './vendedor-productos.component.html',
  styleUrls: ['./vendedor-productos.component.scss'],
})
export class VendedorProductosComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);
  private productoService = inject(ProductoService);
  private api = environment.apiUrl;

  productos: Producto[] = [];
  categorias: Categoria[] = [];
  marcas: Marca[] = [];
  atributosCategoria: CategoriaAtributo[] = [];
  atributosSeleccionados: Record<number, any> = {};

  loading = false;
  procesando = false;

  private modalRef: any;
  editandoId: number | null = null;

  imageFiles: File[] = [];
  imagePreviews: string[] = [];
  existingImages: ProductoImagen[] = [];

  formGeneral = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(200)]],
    idCategoria: [null as any, [Validators.required]],
    idMarca: [null as any],
    descripcion: [''],
    modelo: [''],
    precioBase: [0, [Validators.required, Validators.min(0)]],
  });

  formVariante = this.fb.group({
    sku: ['', [Validators.required, Validators.maxLength(100)]],
    precio: [0, [Validators.required, Validators.min(0)]],
    precioOferta: [null as any],
    stock: [0, [Validators.required, Validators.min(0)]],
    activo: [true],
  });

  ngOnInit(): void {
    this.cargarCategorias();
    this.cargarProductos();

    this.formGeneral.controls.idCategoria.valueChanges.subscribe((idCategoria) => {
      const id = Number(idCategoria);
      if (!id) {
        this.marcas = [];
        this.atributosCategoria = [];
        this.atributosSeleccionados = {};
        this.formGeneral.patchValue({ idMarca: null }, { emitEvent: false });
        return;
      }
      this.cargarMarcasPorCategoria(id);
      this.cargarAtributosPorCategoria(id);
      this.formGeneral.patchValue({ idMarca: null }, { emitEvent: false });
    });
  }

  cargarProductos(): void {
    console.log('A. cargarProductos() iniciado');
    if (this.loading) {
      console.log('   Ya está cargando, saliendo');
      return;
    }
    this.loading = true;
    this.cdr.detectChanges();

    this.productoService.misProductos().subscribe({
      next: (res) => {
        console.log('B. Respuesta de misProductos:', res);

        // 🔥 FILTRAR SOLO PRODUCTOS ACTIVOS (excluir inactivos)
        const productosActivos = (res || []).filter(p =>
          p.estado !== 'inactivo' && p.estado !== 'INACTIVO'
        );

        console.log('   Productos activos después del filtro:', productosActivos);

        const lista = productosActivos || [];

        if (!lista.length) {
          console.log('   Lista vacía');
          this.productos = [];
          this.loading = false;
          this.cdr.detectChanges();
          return;
        }

        let pendientes = lista.length;
        console.log('   Pendientes de cargar imágenes:', pendientes);

        for (const p of lista) {
          this.http.get<ProductoImagen[]>(`${this.api}/productos/${p.idProducto}/imagenes`).subscribe({
            next: (imgs) => {
              p.imagenes = imgs || [];
              pendientes--;
              console.log(`   Imagen cargada para producto ${p.idProducto}, pendientes: ${pendientes}`);
              if (pendientes === 0) {
                console.log('   Todas las imágenes cargadas, asignando productos');
                this.productos = lista;
                this.loading = false;
                this.cdr.detectChanges();
              }
            },
            error: (err) => {
              console.log(`   Error cargando imagen para producto ${p.idProducto}:`, err);
              p.imagenes = [];
              pendientes--;
              if (pendientes === 0) {
                console.log('   Asignando productos con errores de imágenes');
                this.productos = lista;
                this.loading = false;
                this.cdr.detectChanges();
              }
            },
          });
        }
      },
      error: (err: HttpErrorResponse) => {
        console.log('C. Error en misProductos:', err);
        this.loading = false;
        this.cdr.detectChanges();
        this.mostrarError(err, 'No se pudieron cargar tus productos.');
      },
    });
  }

  cargarCategorias(): void {
    this.http.get<Categoria[]>(`${this.api}/categorias/todas`).subscribe({
      next: (res) => (this.categorias = res || []),
      error: () => (this.categorias = []),
    });
  }

  cargarMarcasPorCategoria(idCategoria: number, marcaActual?: string | null): void {
    if (!idCategoria) {
      this.marcas = [];
      return;
    }
    this.http.get<Marca[]>(`${this.api}/categorias/${idCategoria}/marcas`).subscribe({
      next: (res) => {
        this.marcas = res || [];
        if (marcaActual) {
          const marcaEncontrada = this.marcas.find(
            (m) => m.nombre.toLowerCase() === String(marcaActual).toLowerCase()
          );
          this.formGeneral.patchValue({
            idMarca: marcaEncontrada ? marcaEncontrada.idMarca : null,
          }, { emitEvent: false });
        }
      },
      error: () => (this.marcas = []),
    });
  }

  cargarAtributosPorCategoria(idCategoria: number, atributosVariante: any[] = []): void {
    if (!idCategoria) {
      this.atributosCategoria = [];
      this.atributosSeleccionados = {};
      return;
    }
    this.http.get<CategoriaAtributo[]>(`${this.api}/categorias/${idCategoria}/atributos`).subscribe({
      next: (res) => {
        this.atributosCategoria = (res || []).sort((a, b) => (a.orden || 0) - (b.orden || 0));
        this.atributosSeleccionados = {};
        for (const atr of this.atributosCategoria) {
          this.atributosSeleccionados[atr.idAtributo] = null;
        }
        for (const a of atributosVariante || []) {
          const idAtributo = a?.atributo?.idAtributo ?? a?.idAtributo;
          const idValor = a?.valor?.idValor ?? a?.idValor;
          if (idAtributo) {
            this.atributosSeleccionados[idAtributo] = idValor ?? null;
          }
        }
      },
      error: () => {
        this.atributosCategoria = [];
        this.atributosSeleccionados = {};
      },
    });
  }

  imagenPrincipal(p: any): string | null {
    const imgs = p?.imagenes || [];
    const principal = imgs.find((x: any) => x.esPrincipal) || imgs[0];
    return principal?.urlImagen ? this.fullUrl(principal.urlImagen) : null;
  }

  fullUrl(url: string): string {
    if (!url) return url;
    if (url.startsWith('http')) return url;
    return `${environment.apiUrl}${url.startsWith('/') ? '' : '/'}${url}`;
  }

  badgeEstado(estado: string): string {
    const e = (estado || '').toLowerCase();
    if (e === 'activo') return 'badge bg-success-subtle text-success';
    if (e === 'agotado') return 'badge bg-warning-subtle text-warning';
    return 'badge bg-secondary-subtle text-secondary';
  }

  private async mostrarModal(): Promise<void> {
    const el = document.getElementById('modalProducto');
    if (!el) return;
    const bs: any = await import('bootstrap');
    this.modalRef = new bs.Modal(el, { backdrop: 'static' });
    this.modalRef.show();
  }

  cerrarModal(): void {
    this.modalRef?.hide();
  }

  async abrirNuevo(): Promise<void> {
    this.editandoId = null;
    this.formGeneral.reset({
      nombre: '',
      idCategoria: null,
      idMarca: null,
      descripcion: '',
      modelo: '',
      precioBase: 0,
    });
    this.formVariante.reset({
      sku: '',
      precio: 0,
      precioOferta: null,
      stock: 0,
      activo: true,
    });
    this.marcas = [];
    this.atributosCategoria = [];
    this.atributosSeleccionados = {};
    this.existingImages = [];
    this.resetImagenesSeleccionadas();
    await this.mostrarModal();
  }

  async editarProducto(p: any): Promise<void> {
    if (this.procesando) return;
    this.editandoId = p?.idProducto ?? null;
    const idCategoria = Number(p?.idCategoria ?? 0);
    const variante = p?.variantes?.length ? p.variantes[0] : null;

    this.formGeneral.patchValue({
      nombre: p?.nombre ?? '',
      idCategoria: idCategoria || null,
      idMarca: null,
      descripcion: p?.descripcion ?? '',
      modelo: p?.modelo ?? '',
      precioBase: p?.precioBase ?? 0,
    });

    this.formVariante.patchValue({
      sku: variante?.sku ?? '',
      precio: variante?.precio ?? (p?.precioBase ?? 0),
      precioOferta: variante?.precioOferta ?? null,
      stock: variante?.stock ?? 0,
      activo: variante?.activo ?? true,
    });

    this.resetImagenesSeleccionadas();

    this.http.get<ProductoImagen[]>(`${this.api}/productos/${p.idProducto}/imagenes`).subscribe({
      next: (imgs) => (this.existingImages = imgs || []),
      error: () => (this.existingImages = []),
    });

    if (idCategoria) {
      this.cargarMarcasPorCategoria(idCategoria, p?.marca ?? null);
      this.cargarAtributosPorCategoria(idCategoria, variante?.atributos || []);
    } else {
      this.marcas = [];
      this.atributosCategoria = [];
      this.atributosSeleccionados = {};
    }

    await this.mostrarModal();
  }

  // ✅ MÉTODO CORREGIDO
  eliminarProducto(p: any): void {
    console.log('1. eliminarProducto llamado con:', p);
    const id = p?.idProducto;
    if (!id) return;

    if (this.procesando) return;
    this.procesando = true;
    console.log('2. ID a eliminar:', id);

    Swal.fire({
      title: '¿Eliminar producto?',
      text: 'Esta acción no se puede deshacer',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      console.log('3. Resultado del modal:', result);
      if (result.isConfirmed) {
        console.log('4. Enviando petición DELETE...');
        this.productoService.eliminar(id).subscribe({
          next: () => {
            console.log('5. DELETE exitoso, código 204 recibido');
            this.procesando = false;
            this.cdr.detectChanges();

            Swal.fire({
              icon: 'success',
              title: 'Eliminado',
              text: 'Producto eliminado correctamente',
              timer: 2000,
              showConfirmButton: false,
            });

            console.log('6. Llamando a cargarProductos()');
            setTimeout(() => {
              console.log('7. Ejecutando cargarProductos() después del timeout');
              this.cargarProductos();
            }, 500);
          },
          error: (err: HttpErrorResponse) => {
            console.log('❌ Error en DELETE:', err);
            this.procesando = false;
            this.cdr.detectChanges();
            this.mostrarError(err, 'No se pudo eliminar el producto');
          }
        });
      } else {
        this.procesando = false;
      }
    });
  }

  resetImagenesSeleccionadas(): void {
    this.imageFiles = [];
    this.imagePreviews = [];
  }

  abrirSelectorImagenes(input: HTMLInputElement): void {
    input.click();
  }

  onSelectImages(ev: any): void {
    const files: File[] = Array.from(ev.target.files || []);
    if (!files.length) return;

    const disponibles = 4 - (this.imageFiles.length + this.existingImages.length);
    if (disponibles <= 0) {
      Swal.fire({
        icon: 'warning',
        title: 'Límite alcanzado',
        text: 'Solo puedes tener hasta 4 imágenes por producto.',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

    const toAdd = files.slice(0, disponibles);
    toAdd.forEach((f) => {
      this.imageFiles.push(f);
      this.imagePreviews.push(URL.createObjectURL(f));
    });

    ev.target.value = '';
  }

  removeSelectedImage(i: number): void {
    this.imageFiles.splice(i, 1);
    this.imagePreviews.splice(i, 1);
  }

  private uploadImages(productId: number): Promise<void> {
    if (!this.imageFiles.length) return Promise.resolve();

    const uploads = this.imageFiles.map((file) => {
      const fd = new FormData();
      fd.append('file', file);
      return this.http.post(`${this.api}/productos/${productId}/imagenes`, fd).toPromise();
    });

    return Promise.all(uploads).then(() => undefined);
  }

  guardarProducto(): void {
    if (this.procesando) return;
    if (this.formGeneral.invalid || this.formVariante.invalid) {
      this.formGeneral.markAllAsTouched();
      this.formVariante.markAllAsTouched();
      Swal.fire({
        icon: 'warning',
        title: 'Campos incompletos',
        text: 'Completa los campos obligatorios.',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

    this.procesando = true;
    this.cdr.detectChanges();

    const g = this.formGeneral.getRawValue();
    const v = this.formVariante.getRawValue();
    const marcaSeleccionada = this.marcas.find((m) => m.idMarca === Number(g.idMarca));

    const payload: any = {
      nombre: g.nombre,
      descripcion: g.descripcion,
      marca: marcaSeleccionada ? marcaSeleccionada.nombre : null,
      modelo: g.modelo,
      precioBase: Number(g.precioBase || 0),
      idCategoria: Number(g.idCategoria),
      variantes: [
        {
          sku: v.sku,
          precio: Number(v.precio || 0),
          precioOferta:
            v.precioOferta !== null && v.precioOferta !== undefined && v.precioOferta !== ''
              ? Number(v.precioOferta)
              : null,
          stock: Number(v.stock || 0),
          activo: !!v.activo,
          atributos: [] as any[],
        },
      ],
    };

    for (const atr of this.atributosCategoria) {
      const valorSeleccionado = this.atributosSeleccionados[atr.idAtributo];
      if (atr.obligatorio && (valorSeleccionado === null || valorSeleccionado === undefined || valorSeleccionado === '')) {
        this.procesando = false;
        this.cdr.detectChanges();
        Swal.fire({
          icon: 'warning',
          title: 'Atributo obligatorio',
          text: `Falta completar el atributo: ${atr.nombre}`,
          timer: 2000,
          showConfirmButton: false,
        });
        return;
      }
      if (valorSeleccionado) {
        payload.variantes[0].atributos.push({
          idAtributo: atr.idAtributo,
          idValor: Number(valorSeleccionado),
        });
      }
    }

    if (this.editandoId) {
      const id = this.editandoId;
      this.http.put(`${this.api}/productos/${id}`, payload).subscribe({
        next: () => {
          this.uploadImages(id)
            .then(() => {
              this.procesando = false;
              this.cerrarModal();
              this.cargarProductos();
              Swal.fire({
                icon: 'success',
                title: 'Actualizado',
                text: 'Producto actualizado correctamente.',
                timer: 2000,
                showConfirmButton: false,
              });
            })
            .catch(() => {
              this.procesando = false;
              this.cerrarModal();
              this.cargarProductos();
              Swal.fire({
                icon: 'warning',
                title: 'Actualizado parcialmente',
                text: 'Producto actualizado, pero falló la subida de algunas imágenes.',
                timer: 3000,
                showConfirmButton: false,
              });
            });
        },
        error: (err: HttpErrorResponse) => {
          this.procesando = false;
          this.cdr.detectChanges();
          this.mostrarError(err, 'No se pudo actualizar el producto.');
        },
      });
      return;
    }

    this.http.post<any>(`${this.api}/productos`, payload).subscribe({
      next: (created) => {
        const newId = created?.idProducto;
        if (!newId) {
          this.procesando = false;
          this.cerrarModal();
          this.cargarProductos();
          Swal.fire({
            icon: 'warning',
            title: 'Creado sin ID',
            text: 'Producto creado, pero no se pudo obtener el ID para las imágenes.',
            timer: 3000,
            showConfirmButton: false,
          });
          return;
        }
        this.uploadImages(newId)
          .then(() => {
            this.procesando = false;
            this.cerrarModal();
            this.cargarProductos();
            Swal.fire({
              icon: 'success',
              title: 'Creado',
              text: 'Producto creado correctamente.',
              timer: 2000,
              showConfirmButton: false,
            });
          })
          .catch(() => {
            this.procesando = false;
            this.cerrarModal();
            this.cargarProductos();
            Swal.fire({
              icon: 'warning',
              title: 'Creado parcialmente',
              text: 'Producto creado, pero falló la subida de algunas imágenes.',
              timer: 3000,
              showConfirmButton: false,
            });
          });
      },
      error: (err: HttpErrorResponse) => {
        this.procesando = false;
        this.cdr.detectChanges();
        this.mostrarError(err, 'No se pudo crear el producto.');
      },
    });
  }

  private mostrarError(err: HttpErrorResponse, fallback: string): void {
    let mensaje = fallback;
    if (err?.error) {
      if (typeof err.error === 'string') mensaje = err.error;
      else if (err.error.message) mensaje = err.error.message;
    } else if (err?.message) {
      mensaje = err.message;
    }
    Swal.fire({
      icon: 'error',
      title: 'Error',
      text: mensaje,
      timer: 3000,
      showConfirmButton: false,
    });
  }
}