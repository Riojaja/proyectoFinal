import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UiModalService {
    private loginModalSubject = new BehaviorSubject<boolean>(false);
    loginModal$ = this.loginModalSubject.asObservable();

    abrirLogin() {
        this.loginModalSubject.next(true);
    }

    cerrarLogin() {
        this.loginModalSubject.next(false);
    }
}