import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler } from '@angular/common/http';
import { StorageService } from '../services/storage.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private storage: StorageService) { }

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const token = this.storage.get('token');
    const url = req.url || '';

    const isPublicAuth =
      url.includes('/auth/login') ||
      url.includes('/auth/register') ||
      url.includes('/auth/refresh');

    if (!token || isPublicAuth) {
      return next.handle(req);
    }

    const authReq = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });

    return next.handle(authReq);
  }
}