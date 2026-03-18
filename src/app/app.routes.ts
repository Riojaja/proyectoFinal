import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'cliente' },

  {
    path: 'cliente',
    loadComponent: () =>
      import('./features/cliente/pages/home/home.component').then(m => m.HomeComponent),
  },
  {
    path: 'cliente/producto/:id',
    loadComponent: () =>
      import('./features/cliente/pages/producto-detalle/producto-detalle.component')
        .then(m => m.ProductoDetalleComponent)
  },
  {
    path: 'cliente/carrito',
    loadComponent: () =>
      import('./features/cliente/pages/carrito/carrito.component').then(m => m.CarritoComponent),
  },
  {
    path: 'cliente/checkout',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/cliente/pages/checkout/checkout.component').then(m => m.CheckoutComponent),
  },
  {
    path: 'cliente/confirmacion',
    loadComponent: () =>
      import('./features/cliente/pages/confirmacion/confirmacion.component')
        .then(m => m.ConfirmacionComponent)
  },
  {
    path: 'cliente/confirmacion/:id',
    loadComponent: () =>
      import('./features/cliente/pages/confirmacion/confirmacion.component')
        .then(m => m.ConfirmacionComponent)
  },
  {
    path: 'cliente/pedidos',
    loadComponent: () =>
      import('./features/cliente/pages/pedidos/pedidos.component')
        .then(m => m.PedidosComponent)
  },
  {
    path: 'cliente/pedido/:id',
    loadComponent: () =>
      import('./features/cliente/pages/pedido-detalle/pedido-detalle.component')
        .then(m => m.PedidoDetalleComponent)
  },
  {
    path: 'auth/login',
    loadComponent: () =>
      import('./features/auth/pages/login/login.component').then(m => m.LoginComponent),
  },

  // =========================
  // VENDEDOR
  // =========================
  {
    path: 'vendedor',
    canActivate: [roleGuard],
    data: { roles: ['VENDEDOR'] },
    loadComponent: () =>
      import('./features/vendedor/layout/vendedor-layout.component')
        .then(m => m.VendedorLayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/vendedor/pages/dashboard/vendedor-dashboard.component')
            .then(m => m.VendedorDashboardComponent),
      },
      {
        path: 'productos',
        loadComponent: () =>
          import('./features/vendedor/pages/productos/vendedor-productos.component')
            .then(m => m.VendedorProductosComponent),
      },
      {
        path: 'tienda',
        loadComponent: () =>
          import('./features/vendedor/pages/tienda/vendedor-tienda.component')
            .then(m => m.VendedorTiendaComponent),
      },
      {
        path: 'pedidos',
        loadComponent: () =>
          import('./features/vendedor/pages/pedidos/vendedor-pedidos.component')
            .then(m => m.VendedorPedidosComponent),
      },
      {
        path: 'liquidaciones',
        loadComponent: () =>
          import('./features/vendedor/pages/liquidaciones/vendedor-liquidaciones.component')
            .then(m => m.VendedorLiquidacionesComponent),
      },
      {
        path: 'marketing',
        loadComponent: () =>
          import('./features/vendedor/pages/marketing/vendedor-marketing.component')
            .then(m => m.VendedorMarketingComponent),
      },
      {
        path: 'disputas',
        loadComponent: () =>
          import('./features/vendedor/pages/disputas/vendedor-disputas.component')
            .then(m => m.VendedorDisputasComponent),
      },
    ],
  },

  // =========================
  // ADMIN
  // =========================
  {
    path: 'admin',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () =>
      import('./features/admin/layout/admin-layout.component').then(m => m.AdminLayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/admin/pages/dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent),
      },
      {
        path: 'usuarios',
        loadComponent: () =>
          import('./features/admin/pages/usuarios/admin-usuarios.component').then(m => m.AdminUsuariosComponent),
      },
      {
        path: 'categorias',
        loadComponent: () =>
          import('./features/admin/pages/categorias/admin-categorias.component').then(m => m.AdminCategoriasComponent),
      },
      {
        path: 'vendedores',
        loadComponent: () =>
          import('./features/admin/pages/vendedores/admin-vendedores.component')
            .then(m => m.AdminVendedoresComponent),
      },
      {
        path: 'liquidaciones',
        loadComponent: () =>
          import('./features/admin/pages/liquidaciones/admin-liquidaciones.component')
            .then(m => m.AdminLiquidacionesComponent),
      },
      {
        path: 'disputas',
        loadComponent: () =>
          import('./features/admin/pages/disputas/admin-disputas.component')
            .then(m => m.AdminDisputasComponent),
      },
    ]
  },

  { path: '**', redirectTo: 'cliente' },
];