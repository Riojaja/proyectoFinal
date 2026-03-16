import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import Swal from 'sweetalert2';

type EstadoDisputa =
  | 'abierta'
  | 'en_revision'
  | 'resuelta_reembolso'
  | 'resuelta_rechazada';

type DisputaRow = {
  id: number;
  ordenId: number | null;
  sku: string;
  usuarioEmail: string;
  vendedorNombre: string;
  vendedorRuc: string;
  motivo: string;
  descripcion: string;
  evidenciaJson: string;
  estado: EstadoDisputa;
  fechaApertura?: string | null;
  fechaResolucion?: string | null;
};

type ChatMsg = { from: 'admin' | 'vendedor'; text: string; time: string };

@Component({
  standalone: true,
  selector: 'app-admin-disputas',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-disputas.component.html',
  styleUrls: ['./admin-disputas.component.scss'],
})
export class AdminDisputasComponent implements OnInit {
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);
  private api = environment.apiUrl;

  loading = false;
  working = false; // Para prevenir doble clic

  q = '';
  page = 1;
  pageSize = 5;

  disputas: DisputaRow[] = [];

  drawerOpen = false;
  selected: DisputaRow | null = null;

  draft = {
    motivo: '',
    descripcion: '',
    evidenciaJson: '',
    nuevoEstado: null as EstadoDisputa | null,
    mensaje: '',
  };

  chats = new Map<number, ChatMsg[]>();

  ngOnInit(): void {
    this.cargar();
  }

  cargar() {
    if (this.loading) return; // Evitar múltiples llamadas
    this.loading = true;

    const estados: EstadoDisputa[] = [
      'abierta',
      'en_revision',
      'resuelta_reembolso',
      'resuelta_rechazada',
    ];

    const reqs = estados.map((e) =>
      this.http
        .get<any[]>(`${this.api}/disputas/estado?estado=${e}`)
        .pipe(catchError(() => of([])))
    );

    forkJoin(reqs).subscribe({
      next: (listas) => {
        const flat = (listas || []).flat();

        const map = new Map<number, DisputaRow>();
        flat.forEach((d) => {
          const row = this.toRow(d);
          map.set(row.id, row);
        });

        this.disputas = Array.from(map.values()).sort((a, b) => b.id - a.id);
        this.page = 1;
        this.loading = false;
        this.cdr.detectChanges(); // Forzar actualización de la vista
      },
      error: (err) => {
        console.error('Error cargando disputas', err);
        this.loading = false;
        this.cdr.detectChanges();
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudieron cargar las disputas.',
          timer: 3000,
          showConfirmButton: false,
        });
      },
    });
  }

  private toRow(d: any): DisputaRow {
    return {
      id: Number(d?.idDisputa),
      ordenId: d?.idOrdenDetalle ?? null,
      sku: d?.sku || '—',
      usuarioEmail: d?.usuarioEmail || '—',
      vendedorNombre: d?.vendedorNombre || '—',
      vendedorRuc: d?.vendedorRuc || '—',
      motivo: d?.motivo || '—',
      descripcion: d?.descripcion || '',
      evidenciaJson: d?.evidenciaJson || '',
      estado: String(d?.estado || 'abierta') as EstadoDisputa,
      fechaApertura: d?.fechaApertura || null,
      fechaResolucion: d?.fechaResolucion || null,
    };
  }

  get abiertasCount() {
    return this.disputas.filter((x) => x.estado === 'abierta').length;
  }

  get revisionCount() {
    return this.disputas.filter((x) => x.estado === 'en_revision').length;
  }

  get reembolsoCount() {
    return this.disputas.filter(
      (x) => x.estado === 'resuelta_reembolso'
    ).length;
  }

  get promedioDiasResolucion() {
    const resueltas = this.disputas.filter((x) => !!x.fechaResolucion);
    if (!resueltas.length) return 0;

    let totalMs = 0;
    let validas = 0;

    resueltas.forEach((x) => {
      const a = x.fechaApertura ? new Date(x.fechaApertura).getTime() : 0;
      const r = x.fechaResolucion ? new Date(x.fechaResolucion).getTime() : 0;

      if (a && r && r >= a) {
        totalMs += r - a;
        validas++;
      }
    });

    if (!validas) return 0;

    const avg = totalMs / validas;
    return Math.round((avg / (1000 * 60 * 60 * 24)) * 10) / 10;
  }

  get filtered(): DisputaRow[] {
    const s = this.q.trim().toLowerCase();
    if (!s) return this.disputas;

    return this.disputas.filter((r) => {
      const a = String(r.id).toLowerCase();
      const b = String(r.ordenId ?? '').toLowerCase();
      const c = (r.usuarioEmail || '').toLowerCase();
      const d = (r.vendedorNombre || '').toLowerCase();
      const e = (r.motivo || '').toLowerCase();
      const f = (r.estado || '').toLowerCase();
      const g = (r.sku || '').toLowerCase();

      return (
        a.includes(s) ||
        b.includes(s) ||
        c.includes(s) ||
        d.includes(s) ||
        e.includes(s) ||
        f.includes(s) ||
        g.includes(s)
      );
    });
  }

  get totalPages() {
    return Math.max(1, Math.ceil(this.filtered.length / this.pageSize));
  }

  get rows(): DisputaRow[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filtered.slice(start, start + this.pageSize);
  }

  prevPage() {
    if (this.page > 1) {
      this.page--;
    }
  }

  nextPage() {
    if (this.page < this.totalPages) {
      this.page++;
    }
  }

  badgeClass(e: EstadoDisputa) {
    if (e === 'abierta') return 'badge bg-danger-subtle text-danger';
    if (e === 'en_revision') return 'badge bg-warning-subtle text-warning';
    if (e === 'resuelta_reembolso')
      return 'badge bg-success-subtle text-success';
    return 'badge bg-secondary-subtle text-secondary';
  }

  estadoLabel(e: EstadoDisputa) {
    if (e === 'abierta') return 'Abierta';
    if (e === 'en_revision') return 'En Revisión';
    if (e === 'resuelta_reembolso') return 'Reembolso';
    return 'Rechazada';
  }

  openDrawer(r: DisputaRow) {
    this.selected = r;
    this.draft.motivo = r.motivo;
    this.draft.descripcion = r.descripcion;
    this.draft.evidenciaJson = r.evidenciaJson || '';
    this.draft.nuevoEstado = null;
    this.draft.mensaje = '';

    if (!this.chats.has(r.id)) {
      this.chats.set(r.id, [
        {
          from: 'admin',
          text: 'Hola, estamos revisando tu caso.',
          time: this.nowHHMM(),
        },
        {
          from: 'vendedor',
          text: 'Ok, quedo atento. ¿Qué evidencia falta?',
          time: this.nowHHMM(),
        },
      ]);
    }

    this.drawerOpen = true;
    this.lockBodyScroll(true);
  }

  closeDrawer() {
    this.drawerOpen = false;
    this.lockBodyScroll(false);
  }

  private lockBodyScroll(lock: boolean) {
    if (typeof document === 'undefined') return;
    document.body.style.overflow = lock ? 'hidden' : '';
  }

  get chatMsgs(): ChatMsg[] {
    if (!this.selected) return [];
    return this.chats.get(this.selected.id) || [];
  }

  enviarMensaje() {
    if (!this.selected) return;

    const t = this.draft.mensaje.trim();
    if (!t) return;

    const arr = this.chats.get(this.selected.id) || [];
    arr.push({ from: 'admin', text: t, time: this.nowHHMM() });
    this.chats.set(this.selected.id, arr);
    this.draft.mensaje = '';
  }

  private nowHHMM() {
    const d = new Date();
    const hh = String(d.getHours()).padStart(2, '0');
    const mm = String(d.getMinutes()).padStart(2, '0');
    return `${hh}:${mm}`;
  }

  evidenciaItems(): string[] {
    const raw = (this.draft.evidenciaJson || '').trim();
    if (!raw) return [];

    try {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) return parsed.map(String);

      if (typeof parsed === 'object' && parsed) {
        if (Array.isArray((parsed as any).files)) {
          return (parsed as any).files.map((x: any) => String(x));
        }
        if (Array.isArray((parsed as any).urls)) {
          return (parsed as any).urls.map((x: any) => String(x));
        }
      }

      return [];
    } catch {
      return [];
    }
  }

  elegirEstado(e: EstadoDisputa) {
    this.draft.nuevoEstado = e;
  }

  guardarYCerrar() {
    if (!this.selected) return;

    if (!this.draft.nuevoEstado) {
      this.closeDrawer();
      return;
    }

    if (this.working) return; // Prevenir doble clic
    this.working = true;

    this.http
      .patch(`${this.api}/disputas/${this.selected.id}/resolver`, {
        nuevoEstado: this.draft.nuevoEstado,
      })
      .subscribe({
        next: () => {
          this.working = false;
          this.closeDrawer();
          this.cargar();
          Swal.fire({
            icon: 'success',
            title: 'Actualizada',
            text: 'Disputa actualizada correctamente.',
            timer: 2000,
            showConfirmButton: false,
          });
        },
        error: (e) => {
          this.working = false;
          const msg = typeof e?.error === 'string' ? e.error : 'No se pudo actualizar la disputa.';
          Swal.fire({
            icon: 'error',
            title: 'Error',
            text: msg,
            timer: 3000,
            showConfirmButton: false,
          });
        },
      });
  }
}