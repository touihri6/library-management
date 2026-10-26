import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthorApi } from '../../core/api/apis';
import { Auth } from '../../core/auth/auth';
import { Author, Page } from '../../core/models';
import { Alerts } from '../../shared/alerts';
import { Feedback } from '../../shared/feedback';
import { Pager } from '../../shared/pager';

@Component({
  selector: 'app-author-list',
  imports: [RouterLink, Pager, Alerts],
  templateUrl: './author-list.html',
})
export class AuthorList implements OnInit {
  private readonly authorApi = inject(AuthorApi);
  protected readonly auth = inject(Auth);
  protected readonly feedback = new Feedback();

  protected readonly page = signal<Page<Author> | null>(null);
  protected readonly loading = signal(true);
  protected readonly lastName = signal('');
  protected readonly nationality = signal('');

  ngOnInit(): Promise<void> {
    return this.load(0);
  }

  protected async load(page: number): Promise<void> {
    this.loading.set(true);
    await this.feedback.run(async () => {
      this.page.set(
        await firstValueFrom(
          this.authorApi.list({
            lastName: this.lastName().trim(),
            nationality: this.nationality().trim(),
            page,
            size: 12,
          }),
        ),
      );
    });
    this.loading.set(false);
  }

  protected search(lastName: string, nationality: string): void {
    this.lastName.set(lastName);
    this.nationality.set(nationality);
    this.load(0);
  }

  protected async remove(author: Author): Promise<void> {
    if (!confirm(`Delete ${author.firstName} ${author.lastName}?`)) {
      return;
    }
    const done = await this.feedback.run(
      () => firstValueFrom(this.authorApi.delete(author.id)),
      `${author.firstName} ${author.lastName} has been deleted.`,
    );
    if (done) {
      await this.load(this.page()?.page ?? 0);
    }
  }
}
