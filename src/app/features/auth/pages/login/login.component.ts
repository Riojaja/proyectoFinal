import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  email = '';
  password = '';
  error = '';
  loading = false;

  constructor(
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ingresar() {
    this.error = '';
    this.loading = true;

    this.auth.login(this.email, this.password).subscribe({
      next: (session) => {
        const roles: string[] = session?.roles || [];
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');

        if (returnUrl) {
          if (returnUrl.startsWith('/admin') && roles.includes('ADMIN')) {
            this.router.navigateByUrl('/admin');
          } else if (returnUrl.startsWith('/vendedor') && roles.includes('VENDEDOR')) {
            this.router.navigateByUrl('/vendedor');
          } else if (returnUrl.startsWith('/cliente')) {
            this.router.navigateByUrl('/cliente');
          } else {
            this.redirigirPorRol(roles);
          }
        } else {
          this.redirigirPorRol(roles);
        }

        this.loading = false;
      },
      error: (e) => {
        if (e.status === 0) {
          this.error = 'No se pudo conectar con el servidor.';
        } else if (e.status === 401) {
          this.error = 'Credenciales inválidas.';
        } else {
          this.error = 'No se pudo iniciar sesión.';
        }
        this.loading = false;
      }
    });
  }

  private redirigirPorRol(roles: string[]) {
    if (roles.includes('ADMIN')) {
      this.router.navigateByUrl('/admin');
    } else if (roles.includes('VENDEDOR')) {
      this.router.navigateByUrl('/vendedor');
    } else {
      this.router.navigateByUrl('/cliente');
    }
  }
}