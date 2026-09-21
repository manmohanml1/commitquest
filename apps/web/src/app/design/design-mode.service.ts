import { DOCUMENT } from '@angular/common';
import { Injectable, inject, signal } from '@angular/core';

export type DesignMode = 'modern' | 'chronicle';

interface StoredDesignPreference {
  version: 1;
  mode: DesignMode;
  easyRead: boolean;
}

const STORAGE_KEY = 'commitquest.design-mode.v1';
const DEFAULT_PREFERENCE: StoredDesignPreference = {
  version: 1,
  mode: 'modern',
  easyRead: false,
};

@Injectable({ providedIn: 'root' })
export class DesignModeService {
  private readonly document = inject(DOCUMENT);
  private readonly preference = signal(this.restorePreference());

  readonly mode = () => this.preference().mode;
  readonly easyRead = () => this.preference().easyRead;

  constructor() {
    this.applyPreference(this.preference());
  }

  setMode(mode: DesignMode): void {
    this.updatePreference({ ...this.preference(), mode });
  }

  setEasyRead(easyRead: boolean): void {
    this.updatePreference({ ...this.preference(), easyRead });
  }

  private updatePreference(preference: StoredDesignPreference): void {
    this.preference.set(preference);
    this.applyPreference(preference);

    try {
      this.document.defaultView?.localStorage.setItem(STORAGE_KEY, JSON.stringify(preference));
    } catch {
      // Storage may be blocked; the in-memory preference remains usable for this visit.
    }
  }

  private restorePreference(): StoredDesignPreference {
    try {
      const stored = this.document.defaultView?.localStorage.getItem(STORAGE_KEY);
      if (!stored) return DEFAULT_PREFERENCE;

      const candidate = JSON.parse(stored) as Partial<StoredDesignPreference>;
      if (
        candidate.version !== 1 ||
        (candidate.mode !== 'modern' && candidate.mode !== 'chronicle') ||
        typeof candidate.easyRead !== 'boolean'
      ) {
        return DEFAULT_PREFERENCE;
      }

      return candidate as StoredDesignPreference;
    } catch {
      return DEFAULT_PREFERENCE;
    }
  }

  private applyPreference(preference: StoredDesignPreference): void {
    const root = this.document.documentElement;
    root.dataset['commitquestMode'] = preference.mode;
    root.dataset['commitquestEasyRead'] = String(preference.easyRead);
  }
}
