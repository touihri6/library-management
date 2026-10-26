import { signal } from '@angular/core';
import { errorMessage } from '../core/api/error-message';

export class Feedback {
  readonly error = signal<string | null>(null);
  readonly notice = signal<string | null>(null);
  readonly busy = signal(false);

  async run(action: () => Promise<unknown>, success?: string): Promise<boolean> {
    this.busy.set(true);
    this.error.set(null);
    this.notice.set(null);
    try {
      await action();
      if (success) {
        this.notice.set(success);
      }
      return true;
    } catch (err) {
      this.error.set(errorMessage(err));
      return false;
    } finally {
      this.busy.set(false);
    }
  }
}
