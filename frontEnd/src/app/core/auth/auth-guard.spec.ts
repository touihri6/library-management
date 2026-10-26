import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { FakeAuth, fakeAuth } from '../../testing/fake-auth';
import { UserRole } from '../models';
import { Auth } from './auth';
import { authGuard } from './auth-guard';

describe('authGuard', () => {
  let auth: FakeAuth;

  function run(guardRoles: UserRole[], userRoles: UserRole[] | null, url = '/loans'): unknown {
    auth = fakeAuth(userRoles);
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: Auth, useValue: auth }],
    });
    return TestBed.runInInjectionContext(() =>
      authGuard(...guardRoles)({} as ActivatedRouteSnapshot, { url } as RouterStateSnapshot),
    );
  }

  it('lets a user with one of the roles in', () => {
    expect(run(['LIBRARIAN', 'ADMIN'], ['ADMIN'])).toBe(true);
  });

  it('sends an anonymous visitor to the Keycloak login, back to the requested page', () => {
    expect(run(['LIBRARIAN'], null, '/loans/new')).toBe(false);
    expect(auth.loginCalls).toEqual(['/loans/new']);
  });

  it('redirects a user without the role to the catalog', () => {
    const result = run(['ADMIN'], ['MEMBER']);
    expect(result).toBeInstanceOf(UrlTree);
    expect(TestBed.inject(Router).serializeUrl(result as UrlTree)).toBe('/books');
  });

  it('only requires a session when no role is given', () => {
    expect(run([], ['MEMBER'])).toBe(true);
  });
});
