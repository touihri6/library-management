import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { fakeAuth } from '../../testing/fake-auth';
import { UserRole } from '../models';
import { Auth } from './auth';
import { authInterceptor } from './auth-interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  function setUp(roles: UserRole[] | null): void {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: Auth, useValue: fakeAuth(roles, 'abc.def.ghi') },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  }

  afterEach(() => httpMock.verify());

  it('adds the bearer token to API calls of a signed-in user', async () => {
    setUp(['LIBRARIAN']);
    const response = firstValueFrom(http.get(`${environment.apiUrl}/loans`));
    await Promise.resolve();

    const request = httpMock.expectOne(`${environment.apiUrl}/loans`);
    expect(request.request.headers.get('Authorization')).toBe('Bearer abc.def.ghi');
    request.flush({});
    await response;
  });

  it('sends anonymous API calls without a token', async () => {
    setUp(null);
    const response = firstValueFrom(http.get(`${environment.apiUrl}/books`));
    await Promise.resolve();

    const request = httpMock.expectOne(`${environment.apiUrl}/books`);
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
    await response;
  });

  it('never sends the token to other hosts', async () => {
    setUp(['ADMIN']);
    const response = firstValueFrom(http.get('https://example.com/data'));

    const request = httpMock.expectOne('https://example.com/data');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
    await response;
  });
});
