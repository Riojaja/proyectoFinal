import { Directive, ElementRef, Input, OnChanges, SimpleChanges, Inject, PLATFORM_ID, Renderer2 } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Directive({
  selector: '[appBackgroundImage]',
  standalone: true
})
export class BackgroundImageDirective implements OnChanges {
  @Input() appBackgroundImage: string = '';
  @Input() timestamp: number = 0;

  constructor(
    private el: ElementRef,
    private renderer: Renderer2,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['appBackgroundImage'] || changes['timestamp']) {
      this.applyBackground();
    }
  }

  private applyBackground(): void {
    if (!this.appBackgroundImage) return;

    console.log('🎨 Aplicando imagen:', this.appBackgroundImage, 'timestamp:', this.timestamp);

    const gradient = 'linear-gradient(120deg, rgba(10,22,30,.70), rgba(15,40,55,.45))';

    if (isPlatformBrowser(this.platformId)) {
      const cacheBuster = this.timestamp || Date.now();
      const imageUrl = `${this.appBackgroundImage}?v=${cacheBuster}`;

      // ✅ FORZAR ESTILO DIRECTAMENTE (sin esperar carga)
      this.renderer.setStyle(
        this.el.nativeElement,
        'backgroundImage',
        `${gradient}, url('${imageUrl}')`
      );
      this.renderer.setStyle(this.el.nativeElement, 'backgroundSize', 'cover');
      this.renderer.setStyle(this.el.nativeElement, 'backgroundPosition', 'center');

      console.log('✅ Estilo aplicado directamente');

      // También forzar un cambio de clase para provocar repintado
      this.renderer.addClass(this.el.nativeElement, 'hero-refreshed');
      setTimeout(() => {
        this.renderer.removeClass(this.el.nativeElement, 'hero-refreshed');
      }, 100);
    }
  }
}