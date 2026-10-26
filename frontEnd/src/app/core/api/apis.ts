import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Author,
  AuthorRequest,
  Book,
  BookDetails,
  BookRequest,
  LibraryEvent,
  LibraryEventRequest,
  Loan,
  LoanRequest,
  Member,
  MemberRequest,
  Review,
  ReviewRequest,
  User,
  UserCreateRequest,
  RegisterRequest,
  UserRole,
} from '../models';
import { CrudApi } from './crud-api';

@Injectable({ providedIn: 'root' })
export class BookApi extends CrudApi<Book, BookRequest> {
  constructor() {
    super('books');
  }

  details(id: number): Observable<BookDetails> {
    return this.http.get<BookDetails>(`${this.url}/${id}/details`);
  }
}

@Injectable({ providedIn: 'root' })
export class AuthorApi extends CrudApi<Author, AuthorRequest> {
  constructor() {
    super('authors');
  }
}

@Injectable({ providedIn: 'root' })
export class EventApi extends CrudApi<LibraryEvent, LibraryEventRequest> {
  constructor() {
    super('events');
  }
}

@Injectable({ providedIn: 'root' })
export class ReviewApi extends CrudApi<Review, ReviewRequest> {
  constructor() {
    super('reviews');
  }
}

@Injectable({ providedIn: 'root' })
export class MemberApi extends CrudApi<Member, MemberRequest> {
  constructor() {
    super('members');
  }
}

@Injectable({ providedIn: 'root' })
export class LoanApi extends CrudApi<Loan, LoanRequest> {
  constructor() {
    super('loans');
  }

  returnLoan(id: number): Observable<Loan> {
    return this.http.patch<Loan>(`${this.url}/${id}/return`, null);
  }
}

@Injectable({ providedIn: 'root' })
export class UserApi extends CrudApi<User, UserCreateRequest> {
  constructor() {
    super('users');
  }

  register(request: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${this.url}/register`, request);
  }

  me(): Observable<User> {
    return this.http.get<User>(`${this.url}/me`);
  }

  changeRole(id: number, role: UserRole): Observable<User> {
    return this.http.put<User>(`${this.url}/${id}/role`, { role });
  }

  setEnabled(id: number, enabled: boolean): Observable<User> {
    return this.http.patch<User>(`${this.url}/${id}/${enabled ? 'enable' : 'disable'}`, null);
  }
}
