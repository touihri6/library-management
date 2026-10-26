import { Component, OnInit, inject, input, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { BookApi, ReviewApi } from '../../core/api/apis';
import { Auth } from '../../core/auth/auth';
import { BookDetails, Review, label } from '../../core/models';
import { Alerts } from '../../shared/alerts';
import { Feedback } from '../../shared/feedback';

@Component({
  selector: 'app-book-detail',
  imports: [RouterLink, ReactiveFormsModule, DatePipe, DecimalPipe, Alerts],
  templateUrl: './book-detail.html',
})
export class BookDetail implements OnInit {
  readonly id = input.required<string>();

  private readonly bookApi = inject(BookApi);
  private readonly reviewApi = inject(ReviewApi);
  private readonly router = inject(Router);
  protected readonly auth = inject(Auth);
  protected readonly feedback = new Feedback();
  protected readonly label = label;
  protected readonly ratings = [5, 4, 3, 2, 1];

  protected readonly details = signal<BookDetails | null>(null);
  protected readonly reviews = signal<Review[]>([]);

  protected readonly reviewForm = inject(FormBuilder).nonNullable.group({
    reviewerName: [this.auth.displayName(), [Validators.required, Validators.maxLength(80)]],
    rating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
    comment: ['', Validators.maxLength(1000)],
  });

  ngOnInit(): Promise<void> {
    return this.load();
  }

  protected stars(value: number): string {
    const rounded = Math.round(value);
    return '★'.repeat(rounded) + '☆'.repeat(5 - rounded);
  }

  protected async load(): Promise<void> {
    const bookId = Number(this.id());
    await this.feedback.run(async () => {
      const [details, reviews] = await Promise.all([
        firstValueFrom(this.bookApi.details(bookId)),
        firstValueFrom(this.reviewApi.list({ bookId, size: 50, sort: 'createdAt,desc' })),
      ]);
      this.details.set(details);
      this.reviews.set(reviews.content);
    });
  }

  protected async submitReview(): Promise<void> {
    if (this.reviewForm.invalid) {
      this.reviewForm.markAllAsTouched();
      return;
    }
    const value = this.reviewForm.getRawValue();
    const done = await this.feedback.run(
      () =>
        firstValueFrom(
          this.reviewApi.create({
            bookId: Number(this.id()),
            reviewerName: value.reviewerName.trim(),
            rating: Number(value.rating),
            comment: value.comment.trim() || null,
          }),
        ),
      'Thank you, your review has been published.',
    );
    if (done) {
      this.reviewForm.patchValue({ comment: '' });
      this.reviewForm.markAsUntouched();
      const notice = this.feedback.notice();
      await this.load();
      this.feedback.notice.set(notice);
    }
  }

  protected async removeReview(review: Review): Promise<void> {
    if (!confirm(`Delete the review of ${review.reviewerName}?`)) {
      return;
    }
    const done = await this.feedback.run(() => firstValueFrom(this.reviewApi.delete(review.id)));
    if (done) {
      await this.load();
      this.feedback.notice.set('The review has been deleted.');
    }
  }

  protected async removeBook(): Promise<void> {
    const book = this.details()?.book;
    if (!book || !confirm(`Delete "${book.title}"?`)) {
      return;
    }
    const done = await this.feedback.run(() => firstValueFrom(this.bookApi.delete(book.id)));
    if (done) {
      await this.router.navigate(['/books']);
    }
  }
}
