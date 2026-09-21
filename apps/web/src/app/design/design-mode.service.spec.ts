import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';

import { DesignModeService } from './design-mode.service';

const STORAGE_KEY = 'commitquest.design-mode.v1';

describe('DesignModeService', () => {
  beforeEach(() => {
    window.localStorage.removeItem(STORAGE_KEY);
    delete document.documentElement.dataset['commitquestMode'];
    delete document.documentElement.dataset['commitquestEasyRead'];
    TestBed.configureTestingModule({});
  });

  afterEach(() => {
    window.localStorage.removeItem(STORAGE_KEY);
    delete document.documentElement.dataset['commitquestMode'];
    delete document.documentElement.dataset['commitquestEasyRead'];
    TestBed.resetTestingModule();
  });

  it('defaults to Modern and applies the document contract', () => {
    const service = TestBed.inject(DesignModeService);

    expect(service.mode()).toBe('modern');
    expect(service.easyRead()).toBe(false);
    expect(document.documentElement.dataset['commitquestMode']).toBe('modern');
    expect(document.documentElement.dataset['commitquestEasyRead']).toBe('false');
  });

  it('restores a valid versioned preference', () => {
    window.localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({ version: 1, mode: 'chronicle', easyRead: true }),
    );

    const service = TestBed.inject(DesignModeService);

    expect(service.mode()).toBe('chronicle');
    expect(service.easyRead()).toBe(true);
  });

  it('rejects malformed and unknown preferences', () => {
    window.localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({ version: 2, mode: 'storybook', easyRead: 'sometimes' }),
    );

    const service = TestBed.inject(DesignModeService);

    expect(service.mode()).toBe('modern');
    expect(service.easyRead()).toBe(false);
  });

  it('persists mode and easy-read changes without a transport dependency', () => {
    const service = TestBed.inject(DesignModeService);
    service.setMode('chronicle');
    service.setEasyRead(true);

    expect(JSON.parse(window.localStorage.getItem(STORAGE_KEY) ?? '{}')).toEqual({
      version: 1,
      mode: 'chronicle',
      easyRead: true,
    });
    expect(document.documentElement.dataset['commitquestMode']).toBe('chronicle');
    expect(document.documentElement.dataset['commitquestEasyRead']).toBe('true');
  });

  it('continues in memory when browser storage is unavailable', () => {
    const blockedDocument = {
      documentElement: document.documentElement,
      defaultView: {
        localStorage: {
          getItem: () => null,
          setItem: () => {
            throw new DOMException('Blocked');
          },
        },
      },
    } as unknown as Document;
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [{ provide: DOCUMENT, useValue: blockedDocument }],
    });
    const service = TestBed.inject(DesignModeService);

    service.setMode('chronicle');

    expect(service.mode()).toBe('chronicle');
  });
});
