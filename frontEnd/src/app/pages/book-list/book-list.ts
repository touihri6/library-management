import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { BookApi } from '../../core/api/apis';
import { Auth } from '../../core/auth/auth';
import { Book, GENRES, Genre, Page, label } from '../../core/models';
import { Alerts } from '../../shared/alerts';
import { Feedback } from '../../shared/feedback';
import { Pager } from '../../shared/pager';

@Component({
  selector: 'app-book-list',
  imports: [RouterLink, Pager, Alerts],
  templateUrl: './book-list.html',
})
export class BookList implements OnInit {
  private readonly bookApi = inject(BookApi);
  protected readonly auth = inject(Auth);
  protected readonly feedback = new Feedback();
  protected readonly genres = GENRES;
  protected readonly label = label;

  protected readonly page = signal<Page<Book> | null>(null);
  protected readonly loading = signal(true);
  protected readonly author = signal('');
  protected readonly genre = signal<Genre | ''>('');

  ngOnInit(): Promise<void> {
    return this.load(0);
  }

  protected async load(page: number): Promise<void> {
    this.loading.set(true);
    await this.feedback.run(async () => {
      this.page.set(
        await firstValueFrom(
          this.bookApi.list({ author: this.author().trim(), genre: this.genre(), page, size: 12 }),
        ),
      );
    });
    this.loading.set(false);
  }

  protected search(author: string): void {
    this.author.set(author);
    this.load(0);
  }

  protected setGenre(genre: string): void {
    this.genre.set(genre as Genre | '');
    this.load(0);
  }

  protected async remove(book: Book): Promise<void> {
    if (!confirm(`Delete "${book.title}"?`)) {
      return;
    }
    const done = await this.feedback.run(
      () => firstValueFrom(this.bookApi.delete(book.id)),
      `"${book.title}" has been deleted.`,
    );
    if (done) {
      await this.load(this.page()?.page ?? 0);
    }
  }
}
