import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthorApi, BookApi } from '../../core/api/apis';
import { BookRequest, GENRES, Genre, label } from '../../core/models';
import { Alerts } from '../../shared/alerts';
import { Feedback } from '../../shared/feedback';

@Component({
  selector: 'app-book-form',
  imports: [ReactiveFormsModule, RouterLink, Alerts],
  templateUrl: './book-form.html',
})
export class BookForm implements OnInit {
  readonly id = input<string>();

  private readonly bookApi = inject(BookApi);
  private readonly authorApi = inject(AuthorApi);
  private readonly router = inject(Router);
  protected readonly feedback = new Feedback();
  protected readonly genres = GENRES;
  protected readonly label = label;
  protected readonly authorNames = signal<string[]>([]);

  protected readonly form = inject(FormBuilder).nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    author: ['', [Validators.required, Validators.maxLength(120)]],
    isbn: ['', [Validators.required, Validators.pattern(/^[0-9][0-9-]{8,15}[0-9X]$/)]],
    genre: ['FICTION' as Genre, Validators.required],
    publicationYear: [null as number | null, [Validators.min(1450), Validators.max(2100)]],
    price: [0, [Validators.required, Validators.min(0)]],
    availableCopies: [1, [Validators.required, Validators.min(0)]],
  });

  async ngOnInit(): Promise<void> {
    this.authorApi.list({ size: 100 }).subscribe({
      next: (page) => this.authorNames.set(page.content.map((a) => `${a.firstName} ${a.lastName}`)),
      error: () => this.authorNames.set([]),
    });
    const id = this.id();
    if (id) {
      await this.feedback.run(async () => {
        const book = await firstValueFrom(this.bookApi.get(Number(id)));
        this.form.patchValue(book);
      });
    }
  }

  protected async save(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const request: BookRequest = this.form.getRawValue();
    const id = this.id();
    let savedId: number | undefined;
    const done = await this.feedback.run(async () => {
      const saved = await firstValueFrom(
        id ? this.bookApi.update(Number(id), request) : this.bookApi.create(request),
      );
      savedId = saved.id;
    });
    if (done && savedId !== undefined) {
      await this.router.navigate(['/books', savedId]);
    }
  }

  protected invalid(name: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[name];
    return control.touched && control.invalid;
  }
}
