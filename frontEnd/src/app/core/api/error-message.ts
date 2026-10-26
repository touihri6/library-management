import { HttpErrorResponse } from '@angular/common/http';

const MESSAGE_FIELDS = ['detail', 'message', 'title'] as const;

export function errorMessage(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Unexpected error.';
  }
  const body = error.error as Record<string, unknown> | null;
  if (body && typeof body === 'object') {
    const fieldErrors = body['errors'];
    if (fieldErrors && typeof fieldErrors === 'object') {
      const details = Object.entries(fieldErrors as Record<string, string>)
        .map(([field, message]) => `${field} ${message}`)
        .join(', ');
      if (details) {
        return `Validation failed: ${details}.`;
      }
    }
    for (const field of MESSAGE_FIELDS) {
      const value = body[field];
      if (typeof value === 'string' && value.trim() !== '') {
        return value;
      }
    }
  }
  switch (error.status) {
    case 0:
      return 'The API gateway is unreachable. Is it running on port 8080?';
    case 401:
      return 'Please sign in to do this.';
    case 403:
      return 'You are not allowed to do this.';
    case 502:
    case 503:
      return 'A microservice is unavailable. Please try again in a moment.';
    default:
      return `Request failed (${error.status} ${error.statusText}).`;
  }
}
