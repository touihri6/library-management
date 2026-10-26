import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { BookApi, LoanApi, MemberApi } from '../../core/api/apis';
import { Book, Member } from '../../core/models';
import { inDays, today } from '../../shared/dates';
import { Alerts } from '../../shared/alerts';
import { Feedback } from '../../shared/feedback';

const DEFAULT_LOAN_DAYS = 14;

@Component({
  selector: 'app-loan-form',
  imports: [ReactiveFormsModule, RouterLink, Alerts],
  templateUrl: './loan-form.html',
})
export class LoanForm implements OnInit {
  private readonly loanApi = inject(LoanApi);
  private readonly bookApi = inject(BookApi);
  private readonly memberApi = inject(MemberApi);
  private readonly router = inject(Router);
  protected readonly feedback = new Feedback();

  protected readonly books = signal<Book[]>([]);
  protected readonly members = signal<Member[]>([]);
  protected readonly availableBooks = computed(() => this.books().filter((book) => book.available));

  protected readonly form = inject(FormBuilder).group({
    memberId: [null as number | null, Validators.required],
    bookId: [null as number | null, Validators.required],
    loanDate: [today(), Validators.required],
    dueDate: [inDays(DEFAULT_LOAN_DAYS), Validators.required],
  });

  async ngOnInit(): Promise<void> {
    await this.feedback.run(async () => {
      const [books, members] = await Promise.all([
        firstValueFrom(this.bookApi.list({ size: 100 })),
        firstValueFrom(this.memberApi.list({ size: 100 })),
      ]);
      this.books.set(books.content);
      this.members.set(members.content);
    });
  }

  protected async save(): Promise<void> {
    const value = this.form.getRawValue();
    if (
      this.form.invalid ||
      !value.memberId ||
      !value.bookId ||
      !value.loanDate ||
      !value.dueDate
    ) {
      this.form.markAllAsTouched();
      return;
    }
    if (value.dueDate <= value.loanDate) {
      this.feedback.error.set('The due date must be after the loan date.');
      return;
    }
    const done = await this.feedback.run(() =>
      firstValueFrom(
        this.loanApi.create({
          memberId: Number(value.memberId),
          bookId: Number(value.bookId),
          loanDate: value.loanDate!,
          dueDate: value.dueDate!,
        }),
      ),
    );
    if (done) {
      await this.router.navigate(['/loans']);
    }
  }
}
