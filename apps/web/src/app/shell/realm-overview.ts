import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { AuthenticatedShellStore } from './authenticated-shell.store';

@Component({
  selector: 'app-realm-overview',
  templateUrl: './realm-overview.html',
  styleUrl: './shell-destination.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RealmOverview {
  protected readonly store = inject(AuthenticatedShellStore);
}
