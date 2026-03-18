import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import Swal from 'sweetalert2';

import { AdminService } from '../../../../core/services/admin.service';
import { environment } from '../../../../../environments/environment';

type UsuarioRow = {
  idUsuario: number;
  nombre: string;
  apellido: string;
  tipoDocumento: string;
  numeroDocumento: string;
  email: string;
  emailVerificado?: boolean;
  telefono?: string;
  estado: 'activo' | 'inactivo' | 'suspendido';
  roles: any[];
};

@Component({
  selector: 'app-admin-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-usuarios.component.html',
  styleUrls: ['./admin-usuarios.component.scss'],
})
export class AdminUsuariosComponent implements OnInit {
  private admin = inject(AdminService);
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef); // Por si acaso, aunque no es estrictamente necesario

  cargando = false;
  guardando = false;

  usuarios: UsuarioRow[] = [];
  rolesDisponibles: string[] = ['ADMIN', 'VENDEDOR', 'CLIENTE'];

  seleccionado: UsuarioRow | null = null;

  modalCrear = false;
  modalDetalle = false;

  editForm = this.fb.group({
    nombre: ['', [Validators.required]],
    apellido: ['', [Validators.required]],
    telefono: [''],
    estado: ['activo' as 'activo' | 'inactivo' | 'suspendido', [Validators.required]],
  });

  filtrosForm = this.fb.group({
    tipoDocumento: [''],
    rol: [''],
    nombre: [''],
  });

  crearForm = this.fb.group({
    nombre: ['', [Validators.required]],
    apellido: ['', [Validators.required]],
    tipoDocumento: ['DNI', [Validators.required]],
    numeroDocumento: ['', [Validators.required]],
    telefono: [''],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    estadoActivo: [true],
    rol: ['ADMIN', [Validators.required]],
  });

  ngOnInit(): void {
    this.refrescar();
  }

  refrescar(): void {
    this.cargando = true;
    // Forzar detección de cambios para mostrar el spinner inmediatamente
    this.cdr.detectChanges();

    this.admin.listarUsuarios().subscribe({
      next: (u: any) => {
        this.usuarios = (u || []) as UsuarioRow[];
        this.cargando = false;

        if (this.seleccionado) {
          const nuevo = this.usuarios.find(x => x.idUsuario === this.seleccionado!.idUsuario) || null;
          this.seleccionado = nuevo;
          if (nuevo && this.esAdmin(nuevo)) {
            this.cargarEditForm(nuevo);
          }
        }
        this.cdr.detectChanges(); // Asegurar actualización
      },
      error: (e: any) => {
        this.cargando = false;
        this.cdr.detectChanges();
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: e?.error || 'No se pudo cargar usuarios.',
          timer: 3000,
          showConfirmButton: false,
        });
      },
    });
  }

  limpiarFiltros(): void {
    this.filtrosForm.setValue({
      tipoDocumento: '',
      rol: '',
      nombre: '',
    });
  }

  get usuariosFiltrados(): UsuarioRow[] {
    const f = this.filtrosForm.getRawValue();

    const tipo = String(f.tipoDocumento || '').trim().toUpperCase();
    const rol = String(f.rol || '').trim().toUpperCase();
    const q = String(f.nombre || '').trim().toLowerCase();

    return (this.usuarios || []).filter(u => {
      const roles = (u.roles || [])
        .map((r: any) => r?.nombre || r)
        .map((x: string) => String(x).toUpperCase());

      const okTipo = !tipo || String(u.tipoDocumento || '').toUpperCase() === tipo;
      const okRol = !rol || roles.includes(rol);

      const nombre = String(u.nombre || '').toLowerCase();
      const apellido = String(u.apellido || '').toLowerCase();
      const nombreCompleto = `${nombre} ${apellido}`.trim();
      const email = String(u.email || '').toLowerCase();
      const documento = String(u.numeroDocumento || '').toLowerCase();

      const okBusqueda =
        !q ||
        nombre.includes(q) ||
        apellido.includes(q) ||
        nombreCompleto.includes(q) ||
        email.includes(q) ||
        documento.includes(q);

      return okTipo && okRol && okBusqueda;
    });
  }

  rolesDe(u: UsuarioRow | null): string[] {
    if (!u) return [];
    return (u.roles || [])
      .map((r: any) => r?.nombre || r)
      .map((x: string) => String(x).toUpperCase());
  }

  esAdmin(u: UsuarioRow | null): boolean {
    return this.rolesDe(u).includes('ADMIN');
  }

  cargarEditForm(u: UsuarioRow): void {
    this.editForm.patchValue({
      nombre: u.nombre || '',
      apellido: u.apellido || '',
      telefono: u.telefono || '',
      estado: (u.estado || 'activo') as 'activo' | 'inactivo' | 'suspendido',
    });
  }

  verDetalle(u: UsuarioRow): void {
    this.seleccionado = u;
    if (this.esAdmin(u)) {
      this.cargarEditForm(u);
    }
    this.modalDetalle = true;
  }

  cerrarDetalle(): void {
    this.modalDetalle = false;
  }

  editar(u: UsuarioRow): void {
    this.seleccionado = u;

    if (!this.esAdmin(u)) {
      Swal.fire({
        icon: 'warning',
        title: 'Acción no permitida',
        text: 'Solo puedes editar usuarios con rol ADMIN.',
        timer: 3000,
        showConfirmButton: false,
      });
      return;
    }

    this.cargarEditForm(u);
    this.modalDetalle = true;
  }

  guardarPanel(): void {
    // Prevenir doble clic
    if (this.guardando) return;

    if (!this.seleccionado || !this.esAdmin(this.seleccionado)) return;

    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      Swal.fire({
        icon: 'error',
        title: 'Campos incompletos',
        text: 'Completa nombre y apellido.',
        timer: 3000,
        showConfirmButton: false,
      });
      return;
    }

    const id = this.seleccionado.idUsuario;
    const v = this.editForm.getRawValue();

    this.guardando = true;

    this.admin.actualizarUsuario(id, {
      nombre: v.nombre,
      apellido: v.apellido,
      telefono: v.telefono,
      estado: v.estado,
    }).subscribe({
      next: () => {
        this.guardando = false;
        this.modalDetalle = false;
        this.refrescar();
        Swal.fire({
          icon: 'success',
          title: 'Actualizado',
          text: 'Administrador actualizado correctamente.',
          timer: 2000,
          showConfirmButton: false,
        });
      },
      error: (e: any) => {
        this.guardando = false;
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: e?.error || 'No se pudo actualizar usuario.',
          timer: 3000,
          showConfirmButton: false,
        });
      }
    });
  }

  eliminar(u: UsuarioRow): void {
    // Usar SweetAlert2 para confirmación
    Swal.fire({
      title: '¿Eliminar usuario?',
      text: `¿Estás seguro de eliminar a ${u.email}?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      if (result.isConfirmed) {
        // Prevenir doble clic mientras se procesa
        if (this.guardando) return;
        this.guardando = true;

        this.admin.eliminarUsuario(u.idUsuario).subscribe({
          next: () => {
            this.guardando = false;
            if (this.seleccionado?.idUsuario === u.idUsuario) {
              this.seleccionado = null;
            }
            this.refrescar();
            Swal.fire({
              icon: 'success',
              title: 'Eliminado',
              text: 'Usuario eliminado correctamente.',
              timer: 2000,
              showConfirmButton: false,
            });
          },
          error: (e: any) => {
            this.guardando = false;
            Swal.fire({
              icon: 'error',
              title: 'Error',
              text: e?.error || 'No se pudo eliminar.',
              timer: 3000,
              showConfirmButton: false,
            });
          },
        });
      }
    });
  }

  abrirCrear(): void {
    this.modalCrear = true;
    this.crearForm.reset({
      nombre: '',
      apellido: '',
      tipoDocumento: 'DNI',
      numeroDocumento: '',
      telefono: '',
      email: '',
      password: '',
      estadoActivo: true,
      rol: 'ADMIN',
    });
  }

  cerrarCrear(): void {
    this.modalCrear = false;
  }

  crearUsuario(): void {
    // Prevenir doble clic
    if (this.guardando) return;

    if (this.crearForm.invalid) {
      this.crearForm.markAllAsTouched();
      Swal.fire({
        icon: 'error',
        title: 'Campos incompletos',
        text: 'Completa los campos obligatorios.',
        timer: 3000,
        showConfirmButton: false,
      });
      return;
    }

    const v = this.crearForm.getRawValue();

    const payload = {
      nombre: v.nombre,
      apellido: v.apellido,
      email: v.email,
      password: v.password,
      tipoDocumento: v.tipoDocumento,
      numeroDocumento: v.numeroDocumento,
      telefono: v.telefono,
      rol: 'ADMIN',
    };

    this.guardando = true;

    this.http.post(`${environment.apiUrl}/auth/register`, payload).subscribe({
      next: () => {
        const wantedActivo = !!v.estadoActivo;

        this.refrescar();

        // Si se requiere inactivo, actualizar después de crear
        setTimeout(() => {
          if (!wantedActivo) {
            const creado = this.usuarios.find(x => x.email === v.email);
            if (creado) {
              this.admin.actualizarUsuario(creado.idUsuario, { estado: 'inactivo' }).subscribe({
                next: () => this.refrescar(),
                error: () => {}
              });
            }
          }
        }, 400);

        this.guardando = false;
        this.cerrarCrear();
        Swal.fire({
          icon: 'success',
          title: 'Creado',
          text: 'Administrador creado correctamente.',
          timer: 2000,
          showConfirmButton: false,
        });
      },
      error: (e: any) => {
        this.guardando = false;
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: e?.error || 'No se pudo crear administrador.',
          timer: 3000,
          showConfirmButton: false,
        });
      }
    });
  }

  badgeEstado(e: string): string {
    const s = String(e || '').toLowerCase();
    if (s === 'activo') return 'badge bg-success-subtle text-success';
    if (s === 'suspendido') return 'badge bg-warning-subtle text-warning';
    return 'badge bg-secondary-subtle text-secondary';
  }
}