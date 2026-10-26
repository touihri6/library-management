import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth-guard';

const STAFF = authGuard('LIBRARIAN', 'ADMIN');

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'books' },
  {
    path: 'books',
    title: 'Catalog · Library',
    loadComponent: () => import('./pages/book-list/book-list').then((m) => m.BookList),
  },
  {
    path: 'books/new',
    title: 'New book · Library',
    canActivate: [STAFF],
    loadComponent: () => import('./pages/book-form/book-form').then((m) => m.BookForm),
  },
  {
    path: 'books/:id',
    title: 'Book · Library',
    loadComponent: () => import('./pages/book-detail/book-detail').then((m) => m.BookDetail),
  },
  {
    path: 'books/:id/edit',
    title: 'Edit book · Library',
    canActivate: [STAFF],
    loadComponent: () => import('./pages/book-form/book-form').then((m) => m.BookForm),
  },
  {
    path: 'authors',
    title: 'Authors · Library',
    loadComponent: () => import('./pages/author-list/author-list').then((m) => m.AuthorList),
  },
  {
    path: 'authors/new',
    title: 'New author · Library',
    canActivate: [STAFF],
    loadComponent: () => import('./pages/author-form/author-form').then((m) => m.AuthorForm),
  },
  {
    path: 'authors/:id/edit',
    title: 'Edit author · Library',
    canActivate: [STAFF],
    loadComponent: () => import('./pages/author-form/author-form').then((m) => m.AuthorForm),
  },
  {
    path: 'events',
    title: 'Events · Library',
    loadComponent: () => import('./pages/event-list/event-list').then((m) => m.EventList),
  },
  {
    path: 'events/new',
    title: 'New event · Library',
    canActivate: [STAFF],
    loadComponent: () => import('./pages/event-form/event-form').then((m) => m.EventForm),
  },
  {
    path: 'events/:id/edit',
    title: 'Edit event · Library',
    canActivate: [STAFF],
    loadComponent: () => import('./pages/event-form/event-form').then((m) => m.EventForm),
  },
  {
    path: 'members',
    title: 'Members · Library',
    canActivate: [STAFF],
    loadComponent: () => import('./pages/member-list/member-list').then((m) => m.MemberList),
  },
  {
    path: 'members/new',
    title: 'New member · Library',
    canActivate: [STAFF],
    loadComponent: () => import('./pages/member-form/member-form').then((m) => m.MemberForm),
  },
  {
    path: 'members/:id/edit',
    title: 'Edit member · Library',
    canActivate: [STAFF],
    loadComponent: () => import('./pages/member-form/member-form').then((m) => m.MemberForm),
  },
  {
    path: 'loans',
    title: 'Loans · Library',
    canActivate: [STAFF],
    loadComponent: () => import('./pages/loan-list/loan-list').then((m) => m.LoanList),
  },
  {
    path: 'loans/new',
    title: 'New loan · Library',
    canActivate: [STAFF],
    loadComponent: () => import('./pages/loan-form/loan-form').then((m) => m.LoanForm),
  },
  {
    path: 'users',
    title: 'Users · Library',
    canActivate: [authGuard('ADMIN')],
    loadComponent: () => import('./pages/user-list/user-list').then((m) => m.UserList),
  },
  {
    path: 'users/new',
    title: 'New user · Library',
    canActivate: [authGuard('ADMIN')],
    loadComponent: () => import('./pages/user-form/user-form').then((m) => m.UserForm),
  },
  {
    path: 'profile',
    title: 'My account · Library',
    canActivate: [authGuard()],
    loadComponent: () => import('./pages/profile/profile').then((m) => m.Profile),
  },
  {
    path: 'register',
    title: 'Create an account · Library',
    loadComponent: () => import('./pages/register/register').then((m) => m.Register),
  },
  { path: '**', redirectTo: 'books' },
];
