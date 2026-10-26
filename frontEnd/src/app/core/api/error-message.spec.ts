import { HttpErrorResponse } from '@angular/common/http';
import { errorMessage } from './error-message';

describe('errorMessage', () => {
  it('lists the field errors of a validation problem', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: { title: 'Validation failed', errors: { isbn: 'must be a valid ISBN' } },
    });
    expect(errorMessage(error)).toBe('Validation failed: isbn must be a valid ISBN.');
  });

  it('uses the detail of a problem response', () => {
    const error = new HttpErrorResponse({
      status: 409,
      error: { title: 'Invalid operation', detail: 'Book 6 has no available copy' },
    });
    expect(errorMessage(error)).toBe('Book 6 has no available copy');
  });

  it('explains an unreachable gateway', () => {
    expect(errorMessage(new HttpErrorResponse({ status: 0 }))).toContain('port 8080');
  });

  it('explains a missing permission', () => {
    expect(errorMessage(new HttpErrorResponse({ status: 403 }))).toBe(
      'You are not allowed to do this.',
    );
  });

  it('handles non-HTTP errors', () => {
    expect(errorMessage(new Error('boom'))).toBe('Unexpected error.');
  });
});
