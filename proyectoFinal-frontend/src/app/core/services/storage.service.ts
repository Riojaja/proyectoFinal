import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

/**
 * Wrapper de localStorage que NO rompe SSR.
 * - En browser usa localStorage
 * - En server usa un Map en memoria
 */
@Injectable({ providedIn: 'root' })
export class StorageService {
  private mem = new Map<string, string>();
  private isBrowser: boolean;

  constructor(@Inject(PLATFORM_ID) platformId: Object) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  get(key: string): string | null {
    if (this.isBrowser) return localStorage.getItem(key);
    return this.mem.get(key) ?? null;
  }

  set(key: string, value: string) {
    if (this.isBrowser) localStorage.setItem(key, value);
    else this.mem.set(key, value);
  }

  remove(key: string) {
    if (this.isBrowser) localStorage.removeItem(key);
    else this.mem.delete(key);
  }

  clear() {
    if (this.isBrowser) localStorage.clear();
    else this.mem.clear();
  }
}