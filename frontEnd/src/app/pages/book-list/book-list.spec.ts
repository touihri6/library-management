import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Auth } from '../../core/auth/auth';
import { Book, Page, UserRole } from '../../core/models';
import { fakeAuth } from '../../testing/fake-auth';
import { BookList } from './book-list';

const BOOKS: Page<Book> = {
  content: [
    {
      id: 1,
      title: 'Dune',
      author: 'Frank Herbert',
      isbn: '978-0441172719',
      genre: 'SCIENCE_FICTION',
      publicationYear: 1965,
      price: 9.99,
      availableCopies: 4,
      available: true,
    },
    {
      id: 3,
      title: 'Clean Code',
      author: 'Robert C. Martin',
      isbn: '978-0132350884',
      genre: 'TECHNOLOGY',
      publicationYear: 2008,
      price: 37.9,
      availableCopies: 0,
      available: false,
    },
  ],
  page: 0,
  size: 12,
  totalElements: 2,
  totalPages: 1,
  last: true,
};

describe('BookList', () => {
  async function render(roles: UserRole[] | null): Promise<HTMLElement> {
    TestBed.configureTestingModule({
      imports: [BookList],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Auth, useValue: fakeAuth(roles) },
      ],
    });
    const fixture = TestBed.createComponent(BookList);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController)
      .expectOne((req) => req.url === '/api/v1/books')
      .flush(BOOKS);
    await new Promise((resolve) => setTimeout(resolve));
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('shows the books and their availability to visitors, without staff actions', async () => {
    const element = await render(null);
    expect(element.textContent).toContain('Dune');
    expect(element.textContent).toContain('Available');
    expect(element.textContent).toContain('Unavailable');
    expect(element.textContent).not.toContain('New book');
    expect(element.querySelectorAll('.button.danger')).toHaveLength(0);
  });

  it('lets a librarian add, edit and delete books', async () => {
    const element = await render(['LIBRARIAN']);
    expect(element.textContent).toContain('New book');
    expect(element.querySelectorAll('.button.danger')).toHaveLength(2);
  });
});
