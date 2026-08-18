import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class IdentityNavigation {
  navigate(url: string): void {
    window.location.assign(url);
  }
}
