import { Routes } from '@angular/router';

import { App } from './app';
import { AccountSettings } from './shell/account-settings';
import { AuthenticatedShell } from './shell/authenticated-shell';
import { CampaignVault } from './shell/campaign-vault';
import { RealmOverview } from './shell/realm-overview';

export const routes: Routes = [
  { path: '', component: App, title: 'CommitQuest — Portfolio Citadel Demo' },
  {
    path: 'app',
    component: AuthenticatedShell,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'overview' },
      { path: 'overview', component: RealmOverview, title: 'Realm overview — CommitQuest' },
      { path: 'vault', component: CampaignVault, title: 'Campaign vault — CommitQuest' },
      { path: 'account', component: AccountSettings, title: 'Account settings — CommitQuest' },
    ],
  },
  { path: '**', redirectTo: '' },
];
