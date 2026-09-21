import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { DesignMode, DesignModeService } from './design-mode.service';

@Component({
  selector: 'app-design-mode-control',
  templateUrl: './design-mode-control.html',
  styleUrl: './design-mode-control.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DesignModeControl {
  protected readonly designMode = inject(DesignModeService);

  protected selectMode(mode: DesignMode): void {
    this.designMode.setMode(mode);
  }

  protected setEasyRead(event: Event): void {
    this.designMode.setEasyRead((event.target as HTMLInputElement).checked);
  }
}
