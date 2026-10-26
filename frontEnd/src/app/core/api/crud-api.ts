import { HttpClient } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../models';
import { QueryValue, toParams } from './query';

export abstract class CrudApi<T, R> {
  protected readonly http = inject(HttpClient);
  protected readonly url: string;

  protected constructor(resource: string) {
    this.url = `${environment.apiUrl}/${resource}`;
  }

  list(query: Record<string, QueryValue> = {}): Observable<Page<T>> {
    return this.http.get<Page<T>>(this.url, { params: toParams(query) });
  }

  get(id: number): Observable<T> {
    return this.http.get<T>(`${this.url}/${id}`);
  }

  create(request: R): Observable<T> {
    return this.http.post<T>(this.url, request);
  }

  update(id: number, request: R): Observable<T> {
    return this.http.put<T>(`${this.url}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
