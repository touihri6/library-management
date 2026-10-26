import { HttpParams } from '@angular/common/http';

export type QueryValue = string | number | boolean | null | undefined;

export function toParams(query: Record<string, QueryValue>): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(query)) {
    if (value !== null && value !== undefined && value !== '') {
      params = params.set(key, value);
    }
  }
  return params;
}
