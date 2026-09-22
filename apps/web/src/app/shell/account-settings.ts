import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { DesignModeControl } from '../design/design-mode-control';
import { AuthenticatedShellStore } from './authenticated-shell.store';

@Component({
  selector: 'app-account-settings',
  imports: [DesignModeControl],
  templateUrl: './account-settings.html',
  styleUrl: './shell-destination.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountSettings {
  protected readonly store = inject(AuthenticatedShellStore);
}
