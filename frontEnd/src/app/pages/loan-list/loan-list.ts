import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { BookApi, LoanApi, MemberApi } from '../../core/api/apis';
import { LOAN_STATUSES, Loan, LoanStatus, Page, label } from '../../core/models';
import { Alerts } from '../../shared/alerts';
import { Feedback } from '../../shared/feedback';
import { Pager } from '../../shared/pager';

@Component({
  selector: 'app-loan-list',
  imports: [RouterLink, DatePipe, Pager, Alerts],
  templateUrl: './loan-list.html',
})
export class LoanList implements OnInit {
  private readonly loanApi = inject(LoanApi);
  private readonly bookApi = inject(BookApi);
  private readonly memberApi = inject(MemberApi);
  protected readonly feedback = new Feedback();
  protected readonly statuses = LOAN_STATUSES;
  protected readonly label = label;

  protected readonly page = signal<Page<Loan> | null>(null);
  protected readonly loading = signal(true);
  protected readonly status = signal<LoanStatus | ''>('');
  protected readonly bookTitles = signal(new Map<number, string>());
  protected readonly memberNames = signal(new Map<number, string>());

  async ngOnInit(): Promise<void> {
    this.bookApi.list({ size: 100 }).subscribe({
      next: (page) => this.bookTitles.set(new Map(page.content.map((b) => [b.id, b.title]))),
    });
    this.memberApi.list({ size: 100 }).subscribe({
      next: (page) =>
        this.memberNames.set(
          new Map(page.content.map((m) => [m.id, `${m.firstName} ${m.lastName}`])),
        ),
    });
    await this.load(0);
  }

  protected async load(page: number): Promise<void> {
    this.loading.set(true);
    await this.feedback.run(async () => {
      this.page.set(
        await firstValueFrom(
          this.loanApi.list({ status: this.status(), page, size: 20, sort: 'loanDate,desc' }),
        ),
      );
    });
    this.loading.set(false);
  }

  protected setStatus(status: string): void {
    this.status.set(status as LoanStatus | '');
    this.load(0);
  }

  protected bookTitle(loan: Loan): string {
    return this.bookTitles().get(loan.bookId) ?? `Book #${loan.bookId}`;
  }

  protected memberName(loan: Loan): string {
    return this.memberNames().get(loan.memberId) ?? `Member #${loan.memberId}`;
  }

  protected async returnLoan(loan: Loan): Promise<void> {
    const done = await this.feedback.run(
      () => firstValueFrom(this.loanApi.returnLoan(loan.id)),
      `"${this.bookTitle(loan)}" is back on the shelf.`,
    );
    if (done) {
      await this.reloadKeepingNotice();
    }
  }

  protected async remove(loan: Loan): Promise<void> {
    if (!confirm(`Delete the loan of "${this.bookTitle(loan)}"?`)) {
      return;
    }
    const done = await this.feedback.run(
      () => firstValueFrom(this.loanApi.delete(loan.id)),
      'The loan has been deleted.',
    );
    if (done) {
      await this.reloadKeepingNotice();
    }
  }

  private async reloadKeepingNotice(): Promise<void> {
    const notice = this.feedback.notice();
    await this.load(this.page()?.page ?? 0);
    this.feedback.notice.set(notice);
  }
}
