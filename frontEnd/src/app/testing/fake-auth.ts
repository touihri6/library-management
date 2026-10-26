import { computed, signal } from '@angular/core';
import { Auth, UserProfile } from '../core/auth/auth';
import { UserRole } from '../core/models';

export interface FakeAuth extends Partial<Auth> {
  loginCalls: string[];
}

export function fakeAuth(roles: UserRole[] | null, token = 'test-token'): FakeAuth {
  const profile = signal<UserProfile | null>(
    roles === null
      ? null
      : {
          username: 'sara',
          firstName: 'Sara',
          lastName: 'Librarian',
          email: 'sara@library.tn',
          roles,
        },
  );
  const has = (role: UserRole) => roles?.includes(role) ?? false;
  const loginCalls: string[] = [];
  return {
    loginCalls,
    profile: profile.asReadonly(),
    identityProviderDown: signal(false).asReadonly(),
    authenticated: computed(() => profile() !== null),
    isAdmin: computed(() => has('ADMIN')),
    isStaff: computed(() => has('LIBRARIAN') || has('ADMIN')),
    canReview: computed(() => roles !== null && roles.length > 0),
    displayName: computed(() => (profile() ? 'Sara Librarian' : '')),
    hasRole: has,
    hasAnyRole: (...wanted: UserRole[]) => wanted.some(has),
    token: async () => (roles === null ? null : token),
    login: async (path = '/') => {
      loginCalls.push(path);
    },
    logout: async () => undefined,
  };
}
