import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { SessionService } from '../services/session.service';

/**
 * Usa data.roles: ['ADMIN', 'VENDEDOR']
 */
export const roleGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const session = inject(SessionService);
  const router = inject(Router);

  if (!auth.isLogged()) {
    return router.createUrlTree(['/auth/login'], { queryParams: { returnUrl: state.url } });
  }

  const required: string[] = (route.data?.['roles'] as string[]) || [];
  if (required.length === 0) return true;

  return session.ensure().pipe(
    map(s => {
      const ok = !!s && required.some(r => s.roles?.includes(r));
      return ok ? true : router.createUrlTree(['/cliente']);
    })
  );
};