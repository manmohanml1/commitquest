import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AuthenticatedShellStore } from './authenticated-shell.store';

@Component({
  selector: 'app-campaign-vault',
  imports: [RouterLink],
  templateUrl: './campaign-vault.html',
  styleUrl: './shell-destination.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CampaignVault {
  protected readonly store = inject(AuthenticatedShellStore);
}
