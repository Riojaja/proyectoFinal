import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';

import { CartService, CartItem } from '../../../../core/services/cart.service';
import { AuthService } from '../../../../core/services/auth.service';
import { UiModalService } from '../../../../core/services/ui-modal.service';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './carrito.component.html',
  styleUrls: ['./carrito.scss']
})
export class CarritoComponent implements OnInit {
  public cart = inject(CartService);
  public auth = inject(AuthService);
  private router = inject(Router);
  private uiModal = inject(UiModalService);
  private cdr = inject(ChangeDetectorRef);

  items: CartItem[] = [];
  selected = new Set<number>();
  procesando = false; // para prevenir doble clic

  ngOnInit(): void {
    this.items = this.cart.getItems();
    this.cart.refresh();

    this.cart.items$.subscribe(list => {
      this.items = list;

      const ids = new Set(list.map(x => x.idVariante));
      this.selected = new Set(Array.from(this.selected).filter(id => ids.has(id)));

      if (this.selected.size === 0 && list.length > 0) {
        list.forEach(x => this.selected.add(x.idVariante));
      }
      this.cdr.detectChanges();
    });

    if (this.items.length > 0) {
      this.items.forEach(x => this.selected.add(x.idVariante));
    }
  }

  isSelected(idVariante: number) {
    return this.selected.has(idVariante);
  }

  toggleOne(idVariante: number, checked: boolean) {
    if (checked) {
      this.selected.add(idVariante);
    } else {
      this.selected.delete(idVariante);
    }
  }

  toggleAll(checked: boolean) {
    this.selected.clear();
    if (checked) {
      this.items.forEach(x => this.selected.add(x.idVariante));
    }
  }

  get allChecked() {
    return this.items.length > 0 && this.selected.size === this.items.length;
  }

  get selectedCount() {
    return this.selected.size;
  }

  get estaLogueado() {
    return this.auth.isLogged();
  }

  precioUnitario(it: CartItem) {
    return Number(it.precioOferta ?? it.precio ?? 0);
  }

  subtotalItem(it: CartItem) {
    return this.precioUnitario(it) * Number(it.cantidad || 1);
  }

  selectedTotal() {
    return this.items
      .filter(x => this.selected.has(x.idVariante))
      .reduce((acc, x) => acc + this.subtotalItem(x), 0);
  }

  selectedUnits() {
    return this.items
      .filter(x => this.selected.has(x.idVariante))
      .reduce((acc, x) => acc + Number(x.cantidad || 0), 0);
  }

  aumentar(it: CartItem) {
    if (this.procesando) return;
    this.procesando = true;

    this.cart.setQty(it.idVariante, Number(it.cantidad || 1) + 1);

    setTimeout(() => {
      this.procesando = false;
      this.cdr.detectChanges();
    }, 300);
  }

  disminuir(it: CartItem) {
    if (this.procesando) return;
    this.procesando = true;

    const actual = Number(it.cantidad || 1);

    if (actual <= 1) {
      this.cart.remove(it.idVariante);
    } else {
      this.cart.setQty(it.idVariante, actual - 1);
    }

    setTimeout(() => {
      this.procesando = false;
      this.cdr.detectChanges();
    }, 300);
  }

  eliminar(it: CartItem) {
    if (this.procesando) return;

    Swal.fire({
      title: '¿Eliminar producto?',
      text: `¿Estás seguro de eliminar "${it.nombre}" del carrito?`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
    }).then((result) => {
      if (result.isConfirmed) {
        this.procesando = true;
        this.cart.remove(it.idVariante);
        setTimeout(() => {
          this.procesando = false;
          this.cdr.detectChanges();
        }, 300);
        Swal.fire({
          icon: 'success',
          title: 'Eliminado',
          text: 'Producto eliminado del carrito.',
          timer: 1500,
          showConfirmButton: false,
        });
      }
    });
  }

  atributos(it: CartItem) {
    return (it.atributos || [])
      .filter(x => typeof x === 'string' && x.trim().length > 0)
      .join(' • ');
  }

  imagenItem(it: CartItem) {
    if (it.imagen && it.imagen.startsWith('http')) return it.imagen;
    if (it.imagen) return 'http://localhost:8090' + it.imagen;
    return 'https://via.placeholder.com/110x110';
  }

  abrirLogin() {
    this.uiModal.abrirLogin();
  }

  comprar() {
    if (this.procesando) return;

    if (this.selectedCount === 0) {
      Swal.fire({
        icon: 'warning',
        title: 'Selecciona productos',
        text: 'Debes seleccionar al menos un producto para continuar.',
        timer: 2000,
        showConfirmButton: false,
      });
      return;
    }

    this.procesando = true;

    if (!this.auth.isLogged()) {
      this.procesando = false;
      this.abrirLogin();
      return;
    }

    if (this.selected.size > 0 && this.selected.size < this.items.length) {
      this.items
        .filter(it => !this.selected.has(it.idVariante))
        .forEach(it => this.cart.remove(it.idVariante));
    }

    setTimeout(() => {
      this.procesando = false;
      this.router.navigate(['/cliente/checkout']);
    }, 300);
  }
}