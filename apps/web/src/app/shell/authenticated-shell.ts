import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthenticatedShellStore } from './authenticated-shell.store';

@Component({
  selector: 'app-authenticated-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  providers: [AuthenticatedShellStore],
  templateUrl: './authenticated-shell.html',
  styleUrl: './authenticated-shell.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthenticatedShell implements OnInit {
  protected readonly store = inject(AuthenticatedShellStore);

  ngOnInit(): void {
    void this.store.load();
  }

  protected selectCampaign(event: Event): void {
    this.store.selectCampaign((event.target as HTMLSelectElement).value);
  }
}
