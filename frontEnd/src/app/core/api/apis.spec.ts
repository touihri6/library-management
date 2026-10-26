import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';
import { BookApi, LoanApi, UserApi } from './apis';

describe('API services', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('skips empty filters in list queries', async () => {
    const response = firstValueFrom(
      TestBed.inject(BookApi).list({ author: '', genre: 'FANTASY', page: 0 }),
    );

    const request = httpMock.expectOne((req) => req.url === '/api/v1/books');
    expect(request.request.params.has('author')).toBe(false);
    expect(request.request.params.get('genre')).toBe('FANTASY');
    expect(request.request.params.get('page')).toBe('0');
    request.flush({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, last: true });
    await response;
  });

  it('returns a loan with PATCH /loans/{id}/return', async () => {
    const response = firstValueFrom(TestBed.inject(LoanApi).returnLoan(4));

    const request = httpMock.expectOne('/api/v1/loans/4/return');
    expect(request.request.method).toBe('PATCH');
    request.flush({});
    await response;
  });

  it('changes a role and toggles an account through user-service', async () => {
    const users = TestBed.inject(UserApi);
    const roleChange = firstValueFrom(users.changeRole(3, 'LIBRARIAN'));
    const disable = firstValueFrom(users.setEnabled(3, false));

    const roleRequest = httpMock.expectOne('/api/v1/users/3/role');
    expect(roleRequest.request.method).toBe('PUT');
    expect(roleRequest.request.body).toEqual({ role: 'LIBRARIAN' });
    roleRequest.flush({});
    httpMock.expectOne('/api/v1/users/3/disable').flush({});
    await Promise.all([roleChange, disable]);
  });
});
