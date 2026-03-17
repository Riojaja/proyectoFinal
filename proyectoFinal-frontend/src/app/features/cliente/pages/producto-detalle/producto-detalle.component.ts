import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';

import { ProductoService } from '../../../../core/services/producto.service';
import { CartService } from '../../../../core/services/cart.service';

@Component({
    selector: 'app-producto-detalle',
    standalone: true,
    imports: [CommonModule, RouterModule, FormsModule],
    templateUrl: './producto-detalle.component.html',
    styleUrls: ['./producto-detalle.component.scss']
})
export class ProductoDetalleComponent implements OnInit {
    private productoService = inject(ProductoService);
    private cart = inject(CartService);
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private cdr = inject(ChangeDetectorRef);

    producto: any = null;
    cargando = true;
    procesando = false; // Para prevenir doble clic

    imagenActual = 0;
    varianteSeleccionada: any = null;
    cantidad = 1;

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const id = Number(params.get('id'));

            if (!id) {
                this.cargando = false;
                this.cdr.detectChanges();
                return;
            }

            this.cargarProducto(id);
        });
    }

    cargarProducto(id: number) {
        this.cargando = true;
        this.cdr.detectChanges();

        this.productoService.obtener(id).subscribe({
            next: (data) => {
                this.producto = data;
                this.cargando = false;
                this.imagenActual = 0;

                const variantesActivas = this.variantesDisponibles;
                this.varianteSeleccionada = variantesActivas.length ? variantesActivas[0] : null;
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error(err);
                this.producto = null;
                this.cargando = false;
                this.cdr.detectChanges();
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: 'No se pudo cargar el producto.',
                    timer: 3000,
                    showConfirmButton: false,
                });
            }
        });
    }

    get imagenesProducto(): any[] {
        return this.producto?.imagenes || [];
    }

    get variantesDisponibles(): any[] {
        const variantes = this.producto?.variantes || [];
        return variantes.filter((v: any) => v?.activo !== false);
    }

    get resenasProducto(): any[] {
        return this.producto?.resenas || this.producto?.reseñas || this.producto?.reviews || [];
    }

    get imagenPrincipal(): string {
        const imgs = this.imagenesProducto;

        if (!imgs.length) return '';

        return imgs[this.imagenActual]?.urlImagen
            || imgs[this.imagenActual]?.url_imagen
            || imgs[0]?.urlImagen
            || imgs[0]?.url_imagen
            || '';
    }

    cambiarImagen(i: number) {
        this.imagenActual = i;
        this.cdr.detectChanges();
    }

    prevImagen() {
        if (this.imagenesProducto.length > 1) {
            this.imagenActual =
                (this.imagenActual - 1 + this.imagenesProducto.length) % this.imagenesProducto.length;
            this.cdr.detectChanges();
        }
    }

    nextImagen() {
        if (this.imagenesProducto.length > 1) {
            this.imagenActual = (this.imagenActual + 1) % this.imagenesProducto.length;
            this.cdr.detectChanges();
        }
    }

    seleccionarVariante(v: any) {
        this.varianteSeleccionada = v;
        this.cantidad = 1;
        this.cdr.detectChanges();
    }

    precioActual(): number {
        const v = this.varianteSeleccionada;

        if (v?.precioOferta != null && Number(v.precioOferta) > 0 && Number(v.precioOferta) < Number(v.precio)) {
            return Number(v.precioOferta);
        }

        if (v?.precio != null && Number(v.precio) > 0) {
            return Number(v.precio);
        }

        return Number(
            this.producto?.precioBase ??
            this.producto?.precio_base ??
            this.producto?.precio ??
            0
        );
    }

    precioAnterior(): number | null {
        const v = this.varianteSeleccionada;

        if (v?.precioOferta != null && Number(v.precioOferta) > 0 && Number(v.precioOferta) < Number(v.precio)) {
            return Number(v.precio);
        }

        return null;
    }

    descuento(): number | null {
        const anterior = this.precioAnterior();
        const actual = this.precioActual();

        if (!anterior || anterior <= actual) return null;

        return Math.round(((anterior - actual) / anterior) * 100);
    }

    stockDisponible(): number {
        const v = this.varianteSeleccionada;

        return Number(
            v?.stock ??
            v?.stockDisponible ??
            this.producto?.stock ??
            0
        );
    }

    puedeSumar(): boolean {
        return this.cantidad < this.stockDisponible();
    }

    // ✅ CORREGIDO: Sin setTimeout, solo prevención de doble clic
    restarCantidad() {
        if (this.procesando) return;
        this.procesando = true;

        if (this.cantidad > 1) {
            this.cantidad--;
        }

        this.procesando = false;
        this.cdr.detectChanges();
    }

    // ✅ CORREGIDO: Sin setTimeout, solo prevención de doble clic
    sumarCantidad() {
        if (this.procesando) return;
        this.procesando = true;

        if (this.puedeSumar()) {
            this.cantidad++;
        }

        this.procesando = false;
        this.cdr.detectChanges();
    }

    // ✅ CORREGIDO: Sin setTimeout, solo prevención de doble clic
    addCarrito() {
        if (this.procesando) return;
        this.procesando = true;

        if (!this.producto) return;

        const payload = {
            ...this.producto,
            varianteSeleccionada: this.varianteSeleccionada,
            cantidad: this.cantidad
        };

        this.cart.add(payload);
        this.procesando = false;
        this.cdr.detectChanges();

        Swal.fire({
            icon: 'success',
            title: 'Añadido',
            text: 'Producto añadido al carrito.',
            timer: 1500,
            showConfirmButton: false,
        });
    }

    // ✅ CORREGIDO: Sin setTimeout, solo prevención de doble clic
    comprarAhora() {
        if (this.procesando) return;
        this.procesando = true;

        if (!this.producto) return;

        const payload = {
            ...this.producto,
            varianteSeleccionada: this.varianteSeleccionada,
            cantidad: this.cantidad
        };

        this.cart.add(payload);
        this.procesando = false;
        this.router.navigate(['/cliente/carrito']);
    }

    textoVariante(v: any): string {
        const partes: string[] = [];

        if (v?.color?.nombre) partes.push(v.color.nombre);
        if (v?.color && typeof v.color === 'string') partes.push(v.color);
        if (v?.talla?.nombre) partes.push(v.talla.nombre);
        if (v?.talla && typeof v.talla === 'string') partes.push(v.talla);
        if (v?.atributosTexto) partes.push(v.atributosTexto);
        if (v?.sku) partes.push(`SKU: ${v.sku}`);

        if (!partes.length) {
            return `Variante ${v?.idVariante ?? ''}`.trim();
        }

        return partes.join(' · ');
    }

    promedioResenas(): number {
        const lista = this.resenasProducto;
        if (!lista.length) return 0;

        const suma = lista.reduce((acc: number, r: any) => acc + Number(r?.calificacion ?? r?.rating ?? 0), 0);
        return Number((suma / lista.length).toFixed(1));
    }

    estrellasResena(valor: number) {
        return Array.from({ length: 5 }, (_, i) => i < Math.round(valor));
    }

    nombreResena(r: any): string {
        return r?.nombreUsuario || r?.usuario?.nombre || r?.cliente?.nombre || 'Usuario';
    }

    comentarioResena(r: any): string {
        return r?.comentario || r?.descripcion || r?.mensaje || '';
    }

    fechaResena(r: any): string {
        return r?.fecha || r?.fechaCreacion || r?.createdAt || '';
    }

    calificacionResena(r: any): number {
        return Number(r?.calificacion ?? r?.rating ?? 0);
    }

    marcaProducto(): string {
        return this.producto?.marca?.nombre
            || this.producto?.marca
            || this.varianteSeleccionada?.marca?.nombre
            || '';
    }

    modeloProducto(): string {
        return this.producto?.modelo
            || this.varianteSeleccionada?.modelo
            || '';
    }

    colorProducto(): string {
        return this.varianteSeleccionada?.color?.nombre
            || this.varianteSeleccionada?.color
            || '';
    }

    tallaProducto(): string {
        return this.varianteSeleccionada?.talla?.nombre
            || this.varianteSeleccionada?.talla
            || '';
    }

    // ✅ MÉTODO CORRECTO - Devuelve array con nombre y valor
    atributosExtras(): { nombre: string; valor: string }[] {
        const lista =
            this.varianteSeleccionada?.atributos ||
            this.producto?.atributos ||
            [];

        if (!Array.isArray(lista) || !lista.length) {
            return [];
        }

        return lista.map((a: any) => {
            // Buscar nombre
            let nombre = 'Atributo';
            for (const prop of ['nombre', 'nombreAtributo', 'descripcion', 'label', 'titulo', 'key', 'name']) {
                if (a[prop]) {
                    nombre = a[prop];
                    break;
                }
                if (a.atributo && a.atributo[prop]) {
                    nombre = a.atributo[prop];
                    break;
                }
                if (a.tipoAtributo && a.tipoAtributo[prop]) {
                    nombre = a.tipoAtributo[prop];
                    break;
                }
            }

            // Buscar valor
            let valor = '';
            for (const prop of ['valor', 'value', 'opcion', 'texto', 'descripcion']) {
                if (a[prop]) {
                    valor = a[prop];
                    break;
                }
                if (a.atributoValor && a.atributoValor[prop]) {
                    valor = a.atributoValor[prop];
                    break;
                }
            }

            return { nombre, valor };
        }).filter(x => x.valor);
    }
} 