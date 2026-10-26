export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export const GENRES = [
  'FICTION',
  'NON_FICTION',
  'SCIENCE_FICTION',
  'FANTASY',
  'MYSTERY',
  'BIOGRAPHY',
  'HISTORY',
  'SCIENCE',
  'TECHNOLOGY',
] as const;
export type Genre = (typeof GENRES)[number];

export interface Book {
  id: number;
  title: string;
  author: string;
  isbn: string;
  genre: Genre;
  publicationYear: number | null;
  price: number;
  availableCopies: number;
  available: boolean;
}

export interface BookRequest {
  title: string;
  author: string;
  isbn: string;
  genre: Genre;
  publicationYear: number | null;
  price: number;
  availableCopies: number;
}

export interface RatingSummary {
  bookId: number;
  average: number;
  count: number;
}

export interface BookDetails {
  book: Book;
  rating: RatingSummary | null;
}

export interface Author {
  id: number;
  firstName: string;
  lastName: string;
  nationality: string;
  birthYear: number | null;
  biography: string | null;
}

export type AuthorRequest = Omit<Author, 'id'>;

export const EVENT_TYPES = ['READING_CLUB', 'WORKSHOP', 'CONFERENCE', 'BOOK_SIGNING'] as const;
export type EventType = (typeof EVENT_TYPES)[number];

export interface LibraryEvent {
  id: number;
  title: string;
  description: string | null;
  type: EventType;
  eventDate: string;
  location: string;
  capacity: number;
}

export type LibraryEventRequest = Omit<LibraryEvent, 'id'>;

export interface Review {
  id: number;
  bookId: number;
  reviewerName: string;
  rating: number;
  comment: string | null;
  createdAt: string;
}

export interface ReviewRequest {
  bookId: number;
  reviewerName: string;
  rating: number;
  comment: string | null;
}

export const MEMBERSHIP_TYPES = ['STUDENT', 'STANDARD', 'PREMIUM'] as const;
export type MembershipType = (typeof MEMBERSHIP_TYPES)[number];

export interface Member {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string | null;
  membershipType: MembershipType;
  joinDate: string;
}

export type MemberRequest = Omit<Member, 'id'>;

export const LOAN_STATUSES = ['ONGOING', 'LATE', 'RETURNED'] as const;
export type LoanStatus = (typeof LOAN_STATUSES)[number];

export interface Loan {
  id: number;
  bookId: number;
  memberId: number;
  loanDate: string;
  dueDate: string;
  returnDate: string | null;
  status: LoanStatus;
}

export interface LoanRequest {
  bookId: number;
  memberId: number;
  loanDate: string;
  dueDate: string;
}

export const USER_ROLES = ['ADMIN', 'LIBRARIAN', 'MEMBER'] as const;
export type UserRole = (typeof USER_ROLES)[number];

export interface User {
  id: number;
  keycloakId: string;
  username: string;
  email: string | null;
  firstName: string | null;
  lastName: string | null;
  role: UserRole;
  enabled: boolean;
  createdAt: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  password: string;
}

export interface UserCreateRequest extends RegisterRequest {
  role: UserRole;
}

export function label(value: string): string {
  return value
    .toLowerCase()
    .split('_')
    .map((word, index) => (index === 0 ? word.charAt(0).toUpperCase() + word.slice(1) : word))
    .join(' ');
}
