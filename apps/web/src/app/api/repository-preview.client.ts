import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable } from 'rxjs';

import { CampaignProjection } from '../domain/campaign';
import { RepositoryPreviewsClient } from './generated/api/repositoryPreviews.service';

export interface PreviewProblem {
  readonly code?: string;
  readonly detail?: string;
}

@Injectable({ providedIn: 'root' })
export class RepositoryPreviewClient {
  private readonly generatedClient = inject(RepositoryPreviewsClient);

  preview(repositoryUrl: string): Observable<CampaignProjection> {
    return this.generatedClient
      .createRepositoryPreview({ repositoryUrl })
      .pipe(map((projection) => projection as CampaignProjection));
  }

  message(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const problem = error.error as PreviewProblem | null;
      if (problem?.detail) return problem.detail;
    }
    return 'The live preview is unavailable. Portfolio Citadel remains ready to explore.';
  }
}
