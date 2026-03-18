import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import Swal from 'sweetalert2';

type CuponRow = {
    idCupon: number;
    codigo: string;
    tipoDescuento: 'porcentaje' | 'monto_fijo';
    valorDescuento: number;
    montoMinimo: number;
    cantidadMaximaUsos: number;
    cantidadUsos: number;
    fechaInicio: string;
    fechaExpiracion: string;
    activo: boolean;
};

function fechasCuponValidator(control: AbstractControl): ValidationErrors | null {
    const fechaInicio = control.get('fechaInicio')?.value;
    const fechaExpiracion = control.get('fechaExpiracion')?.value;

    if (!fechaInicio || !fechaExpiracion) return null;

    const inicio = new Date(fechaInicio + 'T00:00:00');
    const fin = new Date(fechaExpiracion + 'T00:00:00');

    if (fin <= inicio) {
        return { fechaFinMenorOIgualInicio: true };
    }

    return null;
}

@Component({
    standalone: true,
    selector: 'app-vendedor-marketing',
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './vendedor-marketing.component.html',
    styleUrls: ['./vendedor-marketing.component.scss'],
})
export class VendedorMarketingComponent implements OnInit {
    private http = inject(HttpClient);
    private fb = inject(FormBuilder);
    private cdr = inject(ChangeDetectorRef);
    private api = 'http://localhost:8090/api';

    loading = false;
    procesando = false; // Para prevenir doble clic en guardado

    cupones: CuponRow[] = [];
    showModal = false;

    form = this.fb.group(
        {
            codigo: ['', [Validators.required, Validators.maxLength(50)]],
            tipoDescuento: ['porcentaje', [Validators.required]],
            valorDescuento: [0, [Validators.required, Validators.min(0.01)]],
            montoMinimo: [0, [Validators.min(0)]],
            cantidadMaximaUsos: [0, [Validators.min(0)]],
            fechaInicio: ['', [Validators.required]],
            fechaExpiracion: ['', [Validators.required]],
            activo: [true],
        },
        { validators: fechasCuponValidator }
    );

    ngOnInit(): void {
        this.cargar();
    }

    cargar() {
        if (this.loading) return;
        this.loading = true;
        this.cdr.detectChanges();

        this.http.get<CuponRow[]>(`${this.api}/cupones/mis-cupones`).subscribe({
            next: (res) => {
                this.cupones = res || [];
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error(err);
                this.cupones = [];
                this.loading = false;
                this.cdr.detectChanges();
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: 'No se pudieron cargar tus cupones.',
                    timer: 3000,
                    showConfirmButton: false,
                });
            }
        });
    }

    refrescar() {
        this.cargar();
    }

    get cuponesActivos(): number {
        return this.cupones.filter(c => c.activo && !this.estaExpirado(c)).length;
    }

    get ventasConCupon(): number {
        return 0;
    }

    get proximoCuponEstimado(): number {
        return 0;
    }

    badgeEstado(c: CuponRow) {
        if (!c.activo || this.estaExpirado(c)) return 'badge bg-danger-subtle text-danger';
        return 'badge bg-success-subtle text-success';
    }

    textoEstado(c: CuponRow) {
        return (!c.activo || this.estaExpirado(c)) ? 'Expirado' : 'Activo';
    }

    estaExpirado(c: CuponRow): boolean {
        const hoy = new Date();
        const fin = new Date(c.fechaExpiracion);
        return fin.getTime() < hoy.getTime();
    }

    abrirCrear() {
        this.showModal = true;
        this.cdr.detectChanges();

        const hoy = new Date();
        const ini = this.toDateInput(hoy);
        const fin = this.toDateInput(new Date(hoy.getTime() + 7 * 24 * 3600 * 1000));

        this.form.reset({
            codigo: '',
            tipoDescuento: 'porcentaje',
            valorDescuento: 0,
            montoMinimo: 0,
            cantidadMaximaUsos: 0,
            fechaInicio: ini,
            fechaExpiracion: fin,
            activo: true,
        });
    }

    cerrarModal() {
        this.showModal = false;
        this.cdr.detectChanges();
    }

    minFechaHoy(): string {
        return this.toDateInput(new Date());
    }

    minFechaFin(): string {
        return this.form.controls.fechaInicio.value || this.minFechaHoy();
    }

    fechaInicioPasada(): boolean {
        const fechaInicio = this.form.controls.fechaInicio.value;
        if (!fechaInicio) return false;
        return fechaInicio < this.minFechaHoy();
    }

    guardarCupon() {
        if (this.procesando) return;
        this.procesando = true;

        if (this.form.invalid) {
            this.form.markAllAsTouched();
            this.procesando = false;

            if (this.fechaInicioPasada()) {
                Swal.fire({
                    icon: 'warning',
                    title: 'Fecha inválida',
                    text: 'La fecha de inicio no puede ser menor que hoy.',
                    timer: 2000,
                    showConfirmButton: false,
                });
                return;
            }

            if (this.form.errors?.['fechaFinMenorOIgualInicio']) {
                Swal.fire({
                    icon: 'warning',
                    title: 'Fechas inválidas',
                    text: 'La fecha fin debe ser mayor que la fecha inicio.',
                    timer: 2000,
                    showConfirmButton: false,
                });
                return;
            }

            Swal.fire({
                icon: 'warning',
                title: 'Campos incompletos',
                text: 'Completa correctamente los campos obligatorios.',
                timer: 2000,
                showConfirmButton: false,
            });
            return;
        }

        if (this.fechaInicioPasada()) {
            this.procesando = false;
            Swal.fire({
                icon: 'warning',
                title: 'Fecha inválida',
                text: 'La fecha de inicio no puede ser menor que hoy.',
                timer: 2000,
                showConfirmButton: false,
            });
            return;
        }

        const v = this.form.getRawValue();

        const payload = {
            codigo: v.codigo?.trim()?.toUpperCase(),
            tipoDescuento: v.tipoDescuento,
            valorDescuento: Number(v.valorDescuento || 0),
            montoMinimo: Number(v.montoMinimo || 0),
            cantidadMaximaUsos: Number(v.cantidadMaximaUsos || 0),
            fechaInicio: v.fechaInicio,
            fechaExpiracion: v.fechaExpiracion,
            activo: !!v.activo,
        };

        this.http.post(`${this.api}/cupones`, payload).subscribe({
            next: () => {
                this.procesando = false;
                this.cerrarModal();
                this.cargar();
                Swal.fire({
                    icon: 'success',
                    title: 'Éxito',
                    text: 'Cupón creado correctamente.',
                    timer: 2000,
                    showConfirmButton: false,
                });
            },
            error: (err) => {
                this.procesando = false;
                console.error(err);
                const mensaje = typeof err?.error === 'string'
                    ? err.error
                    : 'No se pudo guardar el cupón.';
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: mensaje,
                    timer: 3000,
                    showConfirmButton: false,
                });
            }
        });
    }

    private toDateInput(d: Date) {
        const yyyy = d.getFullYear();
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}`;
    }
}