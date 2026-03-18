import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Modal } from 'bootstrap';
import Swal from 'sweetalert2';
import { AdminService } from '../../../../core/services/admin.service';

type CatForm = {
  idCategoria?: number;
  nombre: string;
  descripcion: string;
  icono: string;
  estado: boolean;
  idPadre: number | null;
};

type Vista = 'todas' | 'activas' | 'inactivas';

type FlatRow = {
  cat: any;
  level: number;
  hasChildren: boolean;
  isTarget: boolean;
};

@Component({
  selector: 'app-admin-categorias',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-categorias.component.html',
  styleUrls: ['./admin-categorias.component.scss'],
})
export class AdminCategoriasComponent implements OnInit {
  private admin = inject(AdminService);
  private cdr = inject(ChangeDetectorRef);

  cargando = false;
  procesando = false; // Para prevenir doble clic

  categorias: any[] = [];
  iconos: any[] = [];
  iconosAdmin: any[] = [];

  marcas: any[] = [];
  marcasCategoria: any[] = [];

  atributos: any[] = [];
  atributosCategoria: any[] = [];

  rows: FlatRow[] = [];
  selectedId: number | null = null;

  vista: Vista = 'todas';
  busqueda = '';

  modalMode: 'create' | 'edit' = 'create';

  form: CatForm = this.nuevo();

  categoriaEstadoTarget: any = null;
  nuevoEstadoTarget = false;

  categoriaTrabajo: any = null;

  iconoForm = {
    idIcono: null as number | null,
    nombre: '',
    claseCss: '',
    estado: true
  };

  iconoEditMode = false;

  marcaForm = {
    nombre: '',
    estado: true
  };

  marcaCategoriaForm = {
    idMarca: null as number | null
  };

  marcaEditForm = {
    idMarca: null as number | null,
    nombre: '',
    estado: true
  };

  atributoForm = {
    nombre: '',
    tipoDato: 'select',
    unidad: ''
  };

  atributoCategoriaForm = {
    idAtributo: null as number | null,
    obligatorio: false,
    orden: 0
  };

  valorForm = {
    idAtributo: null as number | null,
    valor: ''
  };

  atributoEditForm = {
    idAtributo: null as number | null,
    nombre: '',
    tipoDato: 'select',
    unidad: ''
  };

  tiposDato = ['text', 'number', 'select', 'boolean'];

  private childrenByParent = new Map<number | null, any[]>();

  ngOnInit(): void {
    this.refrescar();
    this.cargarIconos();
    this.cargarMarcas();
    this.cargarAtributos();
  }

  private nuevo(): CatForm {
    return {
      nombre: '',
      descripcion: '',
      icono: '',
      estado: true,
      idPadre: null,
    };
  }

  refrescar() {
    this.cargando = true;
    this.cdr.detectChanges(); // Forzar mostrar spinner

    this.admin.listarCategorias().subscribe({
      next: (c) => {
        this.categorias = (c || []).slice();
        this.cargando = false;
        this.indexar();
        this.rebuildView();
        this.cdr.detectChanges();
      },
      error: (e) => {
        this.cargando = false;
        this.cdr.detectChanges();
        this.mostrarError(e, 'No se pudo cargar categorías.');
      },
    });
  }

  cargarIconos() {
    this.admin.listarIconosCategoria().subscribe({
      next: (data) => {
        this.iconos = data || [];
      },
      error: (e) => {
        this.mostrarError(e, 'No se pudieron cargar los iconos.');
      }
    });
  }

  cargarTodosIconos() {
    this.admin.listarTodosIconosCategoria().subscribe({
      next: (data) => {
        this.iconosAdmin = data || [];
      },
      error: (e) => {
        this.mostrarError(e, 'No se pudieron cargar los iconos.');
      }
    });
  }

  cargarMarcas() {
    this.admin.listarMarcas().subscribe({
      next: (data) => {
        this.marcas = data || [];
      },
      error: (e) => {
        this.mostrarError(e, 'No se pudieron cargar las marcas.');
      }
    });
  }

  cargarAtributos() {
    this.admin.listarAtributos().subscribe({
      next: (data) => {
        this.atributos = data || [];
      },
      error: (e) => {
        this.mostrarError(e, 'No se pudieron cargar los atributos.');
      }
    });
  }

  abrirCrear() {
    this.modalMode = 'create';
    this.form = this.nuevo();
    this.openModal('modalCategoria');
  }

  editar(c: any) {
    this.modalMode = 'edit';
    this.selectedId = c?.idCategoria ?? null;

    this.form = {
      idCategoria: c.idCategoria,
      nombre: c.nombre || '',
      descripcion: c.descripcion || '',
      icono: c.icono || '',
      estado: c.estado ?? true,
      idPadre: c?.idCategoriaPadre ?? null,
    };

    this.openModal('modalCategoria');
  }

  guardar() {
    if (this.procesando) return;
    this.procesando = true;

    const payload: any = {
      nombre: this.form.nombre,
      descripcion: this.form.descripcion,
      icono: this.form.icono || null,
      estado: this.form.estado,
      idCategoriaPadre: this.form.idPadre
    };

    const req = this.form.idCategoria
      ? this.admin.editarCategoria(this.form.idCategoria, payload)
      : this.admin.crearCategoria(payload);

    req.subscribe({
      next: () => {
        this.procesando = false;
        this.closeModal('modalCategoria');
        this.form = this.nuevo();
        this.refrescar();
        Swal.fire({
          icon: 'success',
          title: 'Éxito',
          text: this.form.idCategoria ? 'Categoría actualizada correctamente.' : 'Categoría creada correctamente.',
          timer: 2000,
          showConfirmButton: false,
        });
      },
      error: (e) => {
        this.procesando = false;
        this.mostrarError(e, 'No se pudo guardar.');
      },
    });
  }

  abrirModalEstado(cat: any) {
    this.categoriaEstadoTarget = cat;
    this.nuevoEstadoTarget = !(cat?.estado ?? true);
    this.openModal('modalEstadoCategoria');
  }

  confirmarCambioEstado() {
    if (!this.categoriaEstadoTarget || this.procesando) return;
    this.procesando = true;

    const payload = this.payloadDesde(this.categoriaEstadoTarget, {
      estado: this.nuevoEstadoTarget
    });

    this.admin.editarCategoria(this.categoriaEstadoTarget.idCategoria, payload).subscribe({
      next: () => {
        this.procesando = false;
        this.closeModal('modalEstadoCategoria');
        this.categoriaEstadoTarget = null;
        this.refrescar();
        Swal.fire({
          icon: 'success',
          title: 'Éxito',
          text: `Categoría ${this.nuevoEstadoTarget ? 'activada' : 'inactivada'} correctamente.`,
          timer: 2000,
          showConfirmButton: false,
        });
      },
      error: (e) => {
        this.procesando = false;
        this.mostrarError(e, 'No se pudo cambiar el estado.');
      }
    });
  }

  abrirModalIconos() {
    this.iconoForm = {
      idIcono: null,
      nombre: '',
      claseCss: '',
      estado: true
    };
    this.iconoEditMode = false;
    this.cargarTodosIconos();
    this.openModal('modalIconos');
  }

  editarIcono(icono: any) {
    this.iconoEditMode = true;
    this.iconoForm = {
      idIcono: icono.idIcono,
      nombre: icono.nombre || '',
      claseCss: icono.claseCss || '',
      estado: !!icono.estado
    };
  }

  nuevoIcono() {
    this.iconoEditMode = false;
    this.iconoForm = {
      idIcono: null,
      nombre: '',
      claseCss: '',
      estado: true
    };
  }

  guardarIcono() {
    if (this.procesando) return;
    this.procesando = true;

    const payload = {
      nombre: this.iconoForm.nombre,
      claseCss: this.iconoForm.claseCss,
      estado: this.iconoForm.estado
    };

    const req = this.iconoForm.idIcono
      ? this.admin.editarIconoCategoria(this.iconoForm.idIcono, payload)
      : this.admin.crearIconoCategoria(payload);

    req.subscribe({
      next: () => {
        this.procesando = false;
        this.nuevoIcono();
        this.cargarTodosIconos();
        this.cargarIconos();
        Swal.fire({
          icon: 'success',
          title: 'Éxito',
          text: this.iconoForm.idIcono ? 'Icono actualizado correctamente.' : 'Icono creado correctamente.',
          timer: 2000,
          showConfirmButton: false,
        });
      },
      error: (e) => {
        this.procesando = false;
        this.mostrarError(e, 'No se pudo guardar el icono.');
      }
    });
  }

  eliminarIcono(icono: any) {
    Swal.fire({
      title: '¿Eliminar icono?',
      text: `¿Estás seguro de eliminar el icono "${icono.nombre}"?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      if (result.isConfirmed) {
        if (this.procesando) return;
        this.procesando = true;

        this.admin.eliminarIconoCategoria(icono.idIcono).subscribe({
          next: () => {
            this.procesando = false;
            this.cargarTodosIconos();
            this.cargarIconos();
            Swal.fire({
              icon: 'success',
              title: 'Eliminado',
              text: 'Icono eliminado correctamente.',
              timer: 2000,
              showConfirmButton: false,
            });
          },
          error: (e) => {
            this.procesando = false;
            this.mostrarError(e, 'No se pudo eliminar el icono.');
          }
        });
      }
    });
  }

  abrirModalMarcas(cat: any) {
    this.categoriaTrabajo = cat;
    this.marcaCategoriaForm = { idMarca: null };
    this.marcaForm = { nombre: '', estado: true };
    this.cargarMarcasCategoria(cat.idCategoria);
    this.openModal('modalMarcas');
  }

  cargarMarcasCategoria(idCategoria: number) {
    this.admin.listarMarcasPorCategoria(idCategoria).subscribe({
      next: (data) => {
        this.marcasCategoria = data || [];
      },
      error: (e) => {
        this.mostrarError(e, 'No se pudieron cargar las marcas de la categoría.');
      }
    });
  }

  asignarMarca() {
    if (!this.categoriaTrabajo || !this.marcaCategoriaForm.idMarca || this.procesando) return;

    const yaExiste = (this.marcasCategoria || []).some(
      x => Number(x.idMarca) === Number(this.marcaCategoriaForm.idMarca)
    );

    if (yaExiste) {
      Swal.fire({
        icon: 'warning',
        title: 'Marca ya asignada',
        text: 'Esa marca ya está asignada a esta categoría.',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

    this.procesando = true;

    this.admin.asignarMarcaACategoria({
      idCategoria: this.categoriaTrabajo.idCategoria,
      idMarca: this.marcaCategoriaForm.idMarca
    }).subscribe({
      next: () => {
        this.procesando = false;
        this.marcaCategoriaForm.idMarca = null;
        this.closeModal('modalAsignarMarca');
        this.cargarMarcasCategoria(this.categoriaTrabajo.idCategoria);
        Swal.fire({
          icon: 'success',
          title: 'Asignada',
          text: 'Marca asignada correctamente.',
          timer: 2000,
          showConfirmButton: false,
        });
      },
      error: (e) => {
        this.procesando = false;
        this.mostrarError(e, 'No se pudo asignar la marca.');
      }
    });
  }

  crearMarcaRapida() {
    if (!this.marcaForm.nombre.trim() || this.procesando) return;
    this.procesando = true;

    this.admin.crearMarca({
      nombre: this.marcaForm.nombre,
      estado: this.marcaForm.estado
    }).subscribe({
      next: () => {
        this.procesando = false;
        this.marcaForm = { nombre: '', estado: true };
        this.closeModal('modalCrearMarca');
        this.cargarMarcas();
        Swal.fire({
          icon: 'success',
          title: 'Creada',
          text: 'Marca creada correctamente.',
          timer: 2000,
          showConfirmButton: false,
        });
      },
      error: (e) => {
        this.procesando = false;
        this.mostrarError(e, 'No se pudo crear la marca.');
      }
    });
  }

  abrirEditarMarca(m: any) {
    this.marcaEditForm = {
      idMarca: m.idMarca,
      nombre: m.nombre || '',
      estado: !!m.estado
    };
    this.openModal('modalEditarMarca');
  }

  guardarEdicionMarca() {
    if (!this.marcaEditForm.idMarca || !this.marcaEditForm.nombre.trim() || this.procesando) return;
    this.procesando = true;

    this.admin.editarMarca(this.marcaEditForm.idMarca, {
      nombre: this.marcaEditForm.nombre.trim(),
      estado: this.marcaEditForm.estado
    }).subscribe({
      next: () => {
        this.procesando = false;
        this.closeModal('modalEditarMarca');
        this.cargarMarcas();
        if (this.categoriaTrabajo) {
          this.cargarMarcasCategoria(this.categoriaTrabajo.idCategoria);
        }
        Swal.fire({
          icon: 'success',
          title: 'Actualizada',
          text: 'Marca actualizada correctamente.',
          timer: 2000,
          showConfirmButton: false,
        });
      },
      error: (e) => {
        this.procesando = false;
        this.mostrarError(e, 'No se pudo editar la marca.');
      }
    });
  }

  quitarMarca(item: any) {
    if (!this.categoriaTrabajo || this.procesando) return;

    Swal.fire({
      title: '¿Quitar marca?',
      text: `¿Estás seguro de quitar la marca "${item.nombre}" de esta categoría?`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Sí, quitar',
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      if (result.isConfirmed) {
        this.procesando = true;

        this.admin.quitarMarcaDeCategoria(this.categoriaTrabajo.idCategoria, item.idMarca).subscribe({
          next: () => {
            this.procesando = false;
            this.cargarMarcasCategoria(this.categoriaTrabajo.idCategoria);
            Swal.fire({
              icon: 'success',
              title: 'Quitada',
              text: 'Marca quitada correctamente.',
              timer: 2000,
              showConfirmButton: false,
            });
          },
          error: (e) => {
            this.procesando = false;
            this.mostrarError(e, 'No se pudo quitar la marca.');
          }
        });
      }
    });
  }

  abrirModalAtributos(cat: any) {
    this.categoriaTrabajo = cat;
    this.atributoCategoriaForm = {
      idAtributo: null,
      obligatorio: false,
      orden: 0
    };
    this.atributoForm = {
      nombre: '',
      tipoDato: 'select',
      unidad: ''
    };
    this.valorForm = {
      idAtributo: null,
      valor: ''
    };
    this.cargarAtributosCategoria(cat.idCategoria);
    this.openModal('modalAtributos');
  }

  cargarAtributosCategoria(idCategoria: number) {
    this.admin.listarAtributosPorCategoria(idCategoria).subscribe({
      next: (data) => {
        this.atributosCategoria = data || [];
      },
      error: (e) => {
        this.mostrarError(e, 'No se pudieron cargar los atributos de la categoría.');
      }
    });
  }

  get atributosDisponibles() {
    const asignados = new Set((this.atributosCategoria || []).map(a => Number(a.idAtributo)));
    return (this.atributos || []).filter(a => !asignados.has(Number(a.idAtributo)));
  }

  asignarAtributo() {
    if (!this.categoriaTrabajo || !this.atributoCategoriaForm.idAtributo || this.procesando) return;

    const yaExiste = (this.atributosCategoria || []).some(
      x => Number(x.idAtributo) === Number(this.atributoCategoriaForm.idAtributo)
    );

    if (yaExiste) {
      Swal.fire({
        icon: 'warning',
        title: 'Atributo ya asignado',
        text: 'Ese atributo ya está asignado a esta categoría.',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

    this.procesando = true;

    this.admin.asignarAtributoACategoria({
      idCategoria: this.categoriaTrabajo.idCategoria,
      idAtributo: this.atributoCategoriaForm.idAtributo,
      obligatorio: this.atributoCategoriaForm.obligatorio,
      orden: Number(this.atributoCategoriaForm.orden || 0)
    }).subscribe({
      next: () => {
        this.procesando = false;
        this.atributoCategoriaForm = {
          idAtributo: null,
          obligatorio: false,
          orden: 0
        };
        this.closeModal('modalAsignarAtributo');
        this.cargarAtributosCategoria(this.categoriaTrabajo.idCategoria);
        Swal.fire({
          icon: 'success',
          title: 'Asignado',
          text: 'Atributo asignado correctamente.',
          timer: 2000,
          showConfirmButton: false,
        });
      },
      error: (e) => {
        this.procesando = false;
        this.mostrarError(e, 'No se pudo asignar el atributo.');
      }
    });
  }

  crearAtributoRapido() {
    if (!this.atributoForm.nombre.trim() || this.procesando) return;
    this.procesando = true;

    this.admin.crearAtributo({
      nombre: this.atributoForm.nombre,
      tipoDato: this.atributoForm.tipoDato,
      unidad: this.atributoForm.unidad?.trim() || null
    }).subscribe({
      next: () => {
        this.procesando = false;
        this.atributoForm = {
          nombre: '',
          tipoDato: 'select',
          unidad: ''
        };
        this.closeModal('modalCrearAtributo');
        this.cargarAtributos();
        Swal.fire({
          icon: 'success',
          title: 'Creado',
          text: 'Atributo creado correctamente.',
          timer: 2000,
          showConfirmButton: false,
        });
      },
      error: (e) => {
        this.procesando = false;
        this.mostrarError(e, 'No se pudo crear el atributo.');
      }
    });
  }

  crearValorRapido() {
    if (!this.valorForm.idAtributo || !this.valorForm.valor.trim() || this.procesando) return;
    this.procesando = true;

    this.admin.crearValorAtributo({
      idAtributo: this.valorForm.idAtributo,
      valor: this.valorForm.valor.trim()
    }).subscribe({
      next: () => {
        this.procesando = false;
        this.valorForm = {
          idAtributo: null,
          valor: ''
        };
        this.closeModal('modalCrearValorAtributo');
        this.cargarAtributos();
        if (this.categoriaTrabajo) {
          this.cargarAtributosCategoria(this.categoriaTrabajo.idCategoria);
        }
        Swal.fire({
          icon: 'success',
          title: 'Creado',
          text: 'Valor de atributo creado correctamente.',
          timer: 2000,
          showConfirmButton: false,
        });
      },
      error: (e) => {
        this.procesando = false;
        this.mostrarError(e, 'No se pudo crear el valor.');
      }
    });
  }

  guardarConfigAtributo(item: any) {
    if (!this.categoriaTrabajo || this.procesando) return;
    this.procesando = true;

    this.admin.editarAtributoDeCategoria({
      idCategoria: this.categoriaTrabajo.idCategoria,
      idAtributo: item.idAtributo,
      obligatorio: !!item.obligatorio,
      orden: Number(item.orden || 0)
    }).subscribe({
      next: () => {
        this.procesando = false;
        this.cargarAtributosCategoria(this.categoriaTrabajo.idCategoria);
        Swal.fire({
          icon: 'success',
          title: 'Actualizado',
          text: 'Configuración de atributo actualizada.',
          timer: 2000,
          showConfirmButton: false,
        });
      },
      error: (e) => {
        this.procesando = false;
        this.mostrarError(e, 'No se pudo actualizar el atributo.');
      }
    });
  }

  quitarAtributo(item: any) {
    if (!this.categoriaTrabajo || this.procesando) return;

    Swal.fire({
      title: '¿Quitar atributo?',
      text: `¿Estás seguro de quitar el atributo "${item.nombre}" de esta categoría?`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Sí, quitar',
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      if (result.isConfirmed) {
        this.procesando = true;

        this.admin.quitarAtributoDeCategoria(this.categoriaTrabajo.idCategoria, item.idAtributo).subscribe({
          next: () => {
            this.procesando = false;
            this.cargarAtributosCategoria(this.categoriaTrabajo.idCategoria);
            Swal.fire({
              icon: 'success',
              title: 'Quitado',
              text: 'Atributo quitado correctamente.',
              timer: 2000,
              showConfirmButton: false,
            });
          },
          error: (e) => {
            this.procesando = false;
            this.mostrarError(e, 'No se pudo quitar el atributo.');
          }
        });
      }
    });
  }

  abrirEditarAtributo(item: any) {
    this.atributoEditForm = {
      idAtributo: item.idAtributo,
      nombre: item.nombre || '',
      tipoDato: item.tipoDato || 'select',
      unidad: item.unidad || ''
    };
    this.openModal('modalEditarAtributo');
  }

  guardarEdicionAtributo() {
    if (!this.atributoEditForm.idAtributo || !this.atributoEditForm.nombre.trim() || this.procesando) return;
    this.procesando = true;

    this.admin.editarAtributo(this.atributoEditForm.idAtributo, {
      nombre: this.atributoEditForm.nombre.trim(),
      tipoDato: this.atributoEditForm.tipoDato,
      unidad: this.atributoEditForm.unidad?.trim() || null
    }).subscribe({
      next: () => {
        this.procesando = false;
        this.closeModal('modalEditarAtributo');
        this.cargarAtributos();
        if (this.categoriaTrabajo) {
          this.cargarAtributosCategoria(this.categoriaTrabajo.idCategoria);
        }
        Swal.fire({
          icon: 'success',
          title: 'Actualizado',
          text: 'Atributo actualizado correctamente.',
          timer: 2000,
          showConfirmButton: false,
        });
      },
      error: (e) => {
        this.procesando = false;
        this.mostrarError(e, 'No se pudo editar el atributo.');
      }
    });
  }

  setVista(v: Vista) {
    this.vista = v;
    this.rebuildView();
  }

  seleccionar(row: FlatRow) {
    this.selectedId = row.cat?.idCategoria ?? null;
  }

  get modalTitle() {
    return this.modalMode === 'create' ? 'Nueva categoría' : 'Editar categoría';
  }

  iconClass(icono: string | null | undefined) {
    const v = (icono || '').trim();
    if (!v) return 'bi bi-tag';
    return v;
  }

  get countTodas() {
    return this.categorias.length;
  }

  get countActivas() {
    return this.categorias.filter((x) => !!x?.estado).length;
  }

  get countInactivas() {
    return this.categorias.filter((x) => !x?.estado).length;
  }

  getCategoria(id: number | null) {
    if (id == null) return null;
    return this.categorias.find((x) => x?.idCategoria === id) || null;
  }

  private mostrarError(e: any, fallback: string) {
    let mensaje = fallback;
    const raw = e?.error;
    if (typeof raw === 'string') mensaje = raw;
    else if (raw?.message) mensaje = raw.message;
    else if (e?.message) mensaje = e.message;

    Swal.fire({
      icon: 'error',
      title: 'Error',
      text: mensaje,
      timer: 3000,
      showConfirmButton: false,
    });
  }

  private payloadDesde(cat: any, override: Partial<any> = {}) {
    const idPadre = cat?.idCategoriaPadre ?? null;

    return {
      nombre: override['nombre'] ?? cat?.nombre,
      descripcion: override['descripcion'] ?? cat?.descripcion,
      icono: override['icono'] ?? (cat?.icono || null),
      estado: override['estado'] ?? (cat?.estado ?? true),
      idCategoriaPadre: override['idCategoriaPadre'] ?? idPadre,
    };
  }

  private indexar() {
    this.childrenByParent.clear();

    for (const c of this.categorias) {
      const pid = c?.idCategoriaPadre ?? null;
      const arr = this.childrenByParent.get(pid) || [];
      arr.push(c);
      this.childrenByParent.set(pid, arr);
    }

    for (const [k, arr] of this.childrenByParent.entries()) {
      arr.sort((a, b) => String(a?.nombre || '').localeCompare(String(b?.nombre || '')));
      this.childrenByParent.set(k, arr);
    }
  }

  rebuildView() {
    const q = this.busqueda.trim().toLowerCase();

    const matchVista = (c: any) => {
      if (this.vista === 'todas') return true;
      if (this.vista === 'activas') return !!c?.estado;
      return !c?.estado;
    };

    const matchTexto = (c: any) => {
      if (!q) return true;
      const n = String(c?.nombre || '').toLowerCase();
      const d = String(c?.descripcion || '').toLowerCase();
      return n.includes(q) || d.includes(q);
    };

    const rows: FlatRow[] = [];

    const walk = (parentId: number | null, level: number) => {
      const children = this.childrenByParent.get(parentId) || [];

      for (const c of children) {
        if (!matchVista(c) || !matchTexto(c)) continue;

        const id = Number(c.idCategoria);
        const kids = this.childrenByParent.get(id) || [];
        const hasChildren = kids.length > 0;

        rows.push({ cat: c, level, hasChildren, isTarget: true });
        walk(id, level + 1);
      }
    };

    walk(null, 0);
    this.rows = rows;
  }

  openModal(id: string) {
    const el = document.getElementById(id);
    if (!el) return;
    const modal = Modal.getInstance(el) || new Modal(el);
    modal.show();
  }

  closeModal(id: string) {
    const el = document.getElementById(id);
    if (!el) return;
    const modal = Modal.getInstance(el) || new Modal(el);
    modal.hide();
  }

  eliminarDefinitivo(cat: any) {
    Swal.fire({
      title: '¿Eliminar categoría?',
      text: `¿Estás seguro de eliminar definitivamente la categoría "${cat.nombre}"?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      if (result.isConfirmed) {
        if (this.procesando) return;
        this.procesando = true;

        this.admin.eliminarCategoria(cat.idCategoria).subscribe({
          next: () => {
            this.procesando = false;
            this.refrescar();
            Swal.fire({
              icon: 'success',
              title: 'Eliminada',
              text: 'Categoría eliminada correctamente.',
              timer: 2000,
              showConfirmButton: false,
            });
          },
          error: (e) => {
            this.procesando = false;
            this.mostrarError(e, 'No se pudo eliminar la categoría.');
          }
        });
      }
    });
  }
}