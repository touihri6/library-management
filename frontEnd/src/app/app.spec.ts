import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { Auth } from './core/auth/auth';
import { UserRole } from './core/models';
import { fakeAuth } from './testing/fake-auth';

describe('App navigation', () => {
  function render(roles: UserRole[] | null): HTMLElement {
    TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), { provide: Auth, useValue: fakeAuth(roles) }],
    });
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  function links(element: HTMLElement): string[] {
    return Array.from(element.querySelectorAll('.nav a')).map(
      (link) => link.textContent?.trim() ?? '',
    );
  }

  it('shows only the public pages and a sign-in button to visitors', () => {
    const element = render(null);
    expect(links(element)).toEqual(['Catalog', 'Authors', 'Events']);
    expect(element.textContent).toContain('Sign in');
    expect(element.textContent).toContain('Create an account');
  });

  it('adds members and loans for a librarian', () => {
    const element = render(['LIBRARIAN']);
    expect(links(element)).toEqual(['Catalog', 'Authors', 'Events', 'Members', 'Loans']);
    expect(element.textContent).toContain('Sara Librarian');
    expect(element.textContent).toContain('Sign out');
  });

  it('adds user management for an admin', () => {
    expect(links(render(['ADMIN']))).toContain('Users');
  });

  it('keeps a member on the public pages', () => {
    expect(links(render(['MEMBER']))).toEqual(['Catalog', 'Authors', 'Events']);
  });
});
