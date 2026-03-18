import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import Swal from 'sweetalert2';

type VendedorPerfilDto = {
    nombreTienda?: string;
    nombre?: string;
    ruc?: string;
    descripcion?: string;
    logoUrl?: string;
    estado?: string | boolean;
};

type TiendaPerfilDto = {
    idTienda?: number;
    nombre?: string;
    descripcion?: string;
    direccion?: string;
    telefono?: string;
    email?: string;
    emailPublico?: string;
    estado?: boolean;
    banner?: string;
    bannerUrl?: string;
};

@Component({
    standalone: true,
    selector: 'app-vendedor-tienda',
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './vendedor-tienda.component.html',
    styleUrls: ['./vendedor-tienda.component.scss'],
})
export class VendedorTiendaComponent implements OnInit {
    private fb = inject(FormBuilder);
    private http = inject(HttpClient);
    private cdr = inject(ChangeDetectorRef);

    private base = 'http://localhost:8090/api';
    private serverBase = 'http://localhost:8090';

    logoPreview: string | null = null;
    bannerPreview: string | null = null;

    logoFile: File | null = null;
    bannerFile: File | null = null;

    loading = false;      // Para carga inicial
    procesando = false;   // Para guardado (previene doble clic)

    tieneTienda = false;

    form = this.fb.group({
        nombre: ['', [Validators.required, Validators.maxLength(150)]],
        ruc: [{ value: '', disabled: true }, [Validators.required]],
        descripcion: ['', [Validators.maxLength(255)]],
        direccion: ['', [Validators.maxLength(255)]],
        telefono: ['', [Validators.maxLength(20)]],
        emailPublico: ['', [Validators.email, Validators.maxLength(150)]],
        estado: [true],
    });

    ngOnInit(): void {
        Promise.resolve().then(() => {
            this.cargar();
            this.cdr.detectChanges();
        });
    }

    cargar(): void {
        if (this.loading) return;
        this.loading = true;
        this.cdr.detectChanges();
        this.cargarPerfilVendedor();
    }

    private cargarPerfilVendedor(): void {
        this.http.get<VendedorPerfilDto>(`${this.base}/vendedores/perfil`).subscribe({
            next: (v) => {
                this.form.patchValue({
                    ruc: v.ruc || '',
                });

                this.logoPreview = v.logoUrl ? this.fullUrl(v.logoUrl) : null;
                this.cdr.detectChanges();
                this.cargarTienda();
            },
            error: (e: HttpErrorResponse) => {
                this.loading = false;
                this.cdr.detectChanges();
                this.mostrarError(e, 'No se pudo cargar el perfil del vendedor.');
            }
        });
    }

    private cargarTienda(): void {
        this.http.get<TiendaPerfilDto>(`${this.base}/tiendas/perfil`).subscribe({
            next: (t) => {
                this.tieneTienda = true;

                this.form.patchValue({
                    nombre: t.nombre || '',
                    descripcion: t.descripcion || '',
                    direccion: t.direccion || '',
                    telefono: t.telefono || '',
                    emailPublico: t.emailPublico || t.email || '',
                    estado: t.estado ?? true,
                });

                this.bannerPreview = (t.bannerUrl || t.banner)
                    ? this.fullUrl(t.bannerUrl || t.banner || '')
                    : null;

                this.loading = false;
                this.cdr.detectChanges();
            },
            error: (e: HttpErrorResponse) => {
                if (e.status === 404) {
                    this.tieneTienda = false;
                    this.form.patchValue({
                        nombre: '',
                        descripcion: '',
                        direccion: '',
                        telefono: '',
                        emailPublico: '',
                        estado: true,
                    });
                    this.bannerPreview = null;
                    this.loading = false;
                    this.cdr.detectChanges();
                    return;
                }

                this.loading = false;
                this.cdr.detectChanges();
                this.mostrarError(e, 'No se pudo cargar la tienda.');
            }
        });
    }

    private fullUrl(url: string): string {
        if (!url) return url;
        if (url.startsWith('http://') || url.startsWith('https://')) return url;
        if (url.startsWith('/')) return `${this.serverBase}${url}`;
        return `${this.serverBase}/${url}`;
    }

    onLogoChange(ev: Event): void {
        const input = ev.target as HTMLInputElement;
        const f = input.files?.[0];
        if (!f) return;

        this.logoFile = f;
        this.logoPreview = URL.createObjectURL(f);
        this.cdr.detectChanges();
    }

    onBannerChange(ev: Event): void {
        const input = ev.target as HTMLInputElement;
        const f = input.files?.[0];
        if (!f) return;

        this.bannerFile = f;
        this.bannerPreview = URL.createObjectURL(f);
        this.cdr.detectChanges();
    }

    guardarCambios(): void {
        if (this.procesando) return; // Prevención de doble clic

        if (this.form.invalid) {
            this.form.markAllAsTouched();
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

        const raw = this.form.getRawValue();

        const payloadTienda = {
            nombre: (raw.nombre ?? '').trim(),
            descripcion: (raw.descripcion ?? '').trim(),
            direccion: (raw.direccion ?? '').trim(),
            telefono: (raw.telefono ?? '').trim(),
            email: (raw.emailPublico ?? '').trim(),
            estado: raw.estado ?? true,
        };

        const payloadVendedor = {
            nombre: (raw.nombre ?? '').trim(),
            descripcion: (raw.descripcion ?? '').trim(),
            estado: raw.estado ?? true,
        };

        const requestTienda$ = this.tieneTienda
            ? this.http.put(`${this.base}/tiendas/perfil`, payloadTienda)
            : this.http.post(`${this.base}/tiendas/perfil`, payloadTienda);

        requestTienda$.subscribe({
            next: () => {
                this.http.put(`${this.base}/vendedores/perfil`, payloadVendedor).subscribe({
                    next: () => {
                        this.tieneTienda = true;
                        this.cdr.detectChanges();
                        this.subirArchivosSiHay();
                    },
                    error: (e: HttpErrorResponse) => {
                        this.procesando = false;
                        this.cdr.detectChanges();
                        this.mostrarError(e, 'La tienda se guardó, pero no se pudo actualizar el perfil del vendedor.');
                    }
                });
            },
            error: (e: HttpErrorResponse) => {
                this.procesando = false;
                this.cdr.detectChanges();
                this.mostrarError(e, 'No se pudo guardar la tienda.');
            }
        });
    }

    private subirArchivosSiHay(): void {
        const uploads: Promise<any>[] = [];

        if (this.logoFile) uploads.push(this.uploadFile('logo', this.logoFile));
        if (this.bannerFile) uploads.push(this.uploadFile('banner', this.bannerFile));

        if (!uploads.length) {
            this.procesando = false;
            this.cdr.detectChanges();
            Swal.fire({
                icon: 'success',
                title: 'Éxito',
                text: 'Tienda guardada correctamente.',
                timer: 2000,
                showConfirmButton: false,
            });
            this.cargar(); // Recargar datos
            return;
        }

        Promise.allSettled(uploads).then((results) => {
            const failed = results.some(r => r.status === 'rejected');

            this.logoFile = null;
            this.bannerFile = null;
            this.procesando = false;
            this.cdr.detectChanges();

            if (failed) {
                Swal.fire({
                    icon: 'error',
                    title: 'Error parcial',
                    text: 'La tienda se guardó, pero falló la subida de una o más imágenes.',
                    timer: 3000,
                    showConfirmButton: false,
                });
            } else {
                Swal.fire({
                    icon: 'success',
                    title: 'Éxito',
                    text: 'Tienda guardada correctamente.',
                    timer: 2000,
                    showConfirmButton: false,
                });
            }
            this.cargar(); // Recargar datos
        });
    }

    private uploadFile(tipo: 'logo' | 'banner', file: File): Promise<any> {
        const fd = new FormData();
        fd.append('file', file);

        return new Promise((resolve, reject) => {
            if (tipo === 'logo') {
                this.http.post<{ logoUrl?: string; url?: string }>(`${this.base}/vendedores/perfil/logo`, fd).subscribe({
                    next: (r) => {
                        const finalUrl = r.logoUrl || r.url || '';
                        if (finalUrl) {
                            this.logoPreview = this.fullUrl(finalUrl);
                        }
                        this.cdr.detectChanges();
                        resolve(r);
                    },
                    error: (e) => reject(e),
                });
                return;
            }

            this.http.post<{ bannerUrl?: string; url?: string }>(`${this.base}/tiendas/perfil/banner`, fd).subscribe({
                next: (r) => {
                    const finalUrl = r.bannerUrl || r.url || '';
                    if (finalUrl) {
                        this.bannerPreview = this.fullUrl(finalUrl);
                    }
                    this.cdr.detectChanges();
                    resolve(r);
                },
                error: (e) => reject(e),
            });
        });
    }

    private mostrarError(e: HttpErrorResponse, fallback: string): void {
        let mensaje = fallback;
        if (!e) mensaje = fallback;
        else if (typeof e.error === 'string' && e.error.trim()) mensaje = e.error;
        else if (e.error?.error) mensaje = e.error.error;
        else if (e.error?.message) mensaje = e.error.message;
        else if (e.message) mensaje = e.message;

        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: mensaje,
            timer: 3000,
            showConfirmButton: false,
        });
    }
}