import { Component, input, output } from '@angular/core';
import { Page } from '../core/models';

@Component({
  selector: 'app-pager',
  template: `
    @let current = page();
    @if (current && current.totalPages > 1) {
      <nav class="pager" aria-label="Pagination">
        <button
          type="button"
          class="button small"
          [disabled]="current.page === 0"
          (click)="pageChange.emit(current.page - 1)"
        >
          Previous
        </button>
        <span class="muted">
          Page {{ current.page + 1 }} of {{ current.totalPages }} ·
          {{ current.totalElements }} results
        </span>
        <button
          type="button"
          class="button small"
          [disabled]="current.last"
          (click)="pageChange.emit(current.page + 1)"
        >
          Next
        </button>
      </nav>
    }
  `,
})
export class Pager {
  readonly page = input<Page<unknown> | null>(null);
  readonly pageChange = output<number>();
}
