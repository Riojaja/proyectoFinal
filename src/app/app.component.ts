import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';
import { NavbarComponent } from './shared/components/navbar/navbar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, NavbarComponent],
  templateUrl: './app.html',
})
export class AppComponent {
  private router = inject(Router);

  showNavbar = true;

  constructor() {
    this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe((e: any) => {
        const url = e.urlAfterRedirects || e.url;

        // ✅ Oculta navbar global en paneles y auth
        this.showNavbar = !(
          url.startsWith('/vendedor') ||
          url.startsWith('/admin') ||
          url.startsWith('/auth')
        );
      });
  }
}