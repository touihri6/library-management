import { Injectable, computed, signal } from '@angular/core';
import Keycloak from 'keycloak-js';
import { environment } from '../../../environments/environment';
import { USER_ROLES, UserRole } from '../models';

export interface UserProfile {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  roles: UserRole[];
}

interface KeycloakClaims {
  preferred_username?: string;
  given_name?: string;
  family_name?: string;
  email?: string;
  realm_access?: { roles?: string[] };
}

const MIN_TOKEN_VALIDITY_SECONDS = 30;

@Injectable({ providedIn: 'root' })
export class Auth {
  private readonly keycloak = new Keycloak({ ...environment.keycloak });
  private readonly user = signal<UserProfile | null>(null);
  private readonly unavailable = signal(false);

  readonly profile = this.user.asReadonly();
  readonly identityProviderDown = this.unavailable.asReadonly();
  readonly authenticated = computed(() => this.user() !== null);
  readonly isAdmin = computed(() => this.hasRole('ADMIN'));
  readonly isStaff = computed(() => this.hasAnyRole('LIBRARIAN', 'ADMIN'));
  readonly canReview = computed(() => this.hasAnyRole('MEMBER', 'LIBRARIAN', 'ADMIN'));
  readonly displayName = computed(() => {
    const profile = this.user();
    if (!profile) {
      return '';
    }
    return `${profile.firstName} ${profile.lastName}`.trim() || profile.username;
  });

  async init(): Promise<void> {
    try {
      await this.keycloak.init({
        onLoad: 'check-sso',
        pkceMethod: 'S256',
        checkLoginIframe: false,
      });
    } catch {
      this.unavailable.set(true);
      return;
    }
    this.keycloak.onTokenExpired = () => {
      this.keycloak.updateToken(MIN_TOKEN_VALIDITY_SECONDS).catch(() => this.user.set(null));
    };
    this.keycloak.onAuthRefreshSuccess = () => this.user.set(this.readProfile());
    this.user.set(this.readProfile());
  }

  hasRole(role: UserRole): boolean {
    return this.user()?.roles.includes(role) ?? false;
  }

  hasAnyRole(...roles: UserRole[]): boolean {
    return roles.some((role) => this.hasRole(role));
  }

  async token(): Promise<string | null> {
    if (!this.keycloak.authenticated) {
      return null;
    }
    try {
      await this.keycloak.updateToken(MIN_TOKEN_VALIDITY_SECONDS);
    } catch {
      this.user.set(null);
      return null;
    }
    return this.keycloak.token ?? null;
  }

  login(redirectPath = window.location.pathname, loginHint?: string): Promise<void> {
    return this.keycloak.login({ redirectUri: window.location.origin + redirectPath, loginHint });
  }

  logout(): Promise<void> {
    return this.keycloak.logout({ redirectUri: window.location.origin });
  }

  private readProfile(): UserProfile | null {
    const claims = this.keycloak.tokenParsed as KeycloakClaims | undefined;
    if (!claims) {
      return null;
    }
    const roles = (claims.realm_access?.roles ?? []).filter((role): role is UserRole =>
      USER_ROLES.includes(role as UserRole),
    );
    return {
      username: claims.preferred_username ?? '',
      firstName: claims.given_name ?? '',
      lastName: claims.family_name ?? '',
      email: claims.email ?? '',
      roles,
    };
  }
}
