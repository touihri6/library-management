import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { BookApi, LoanApi, UserApi } from '../../core/api/apis';
import { Auth } from '../../core/auth/auth';
import { Loan, User, label } from '../../core/models';
import { Alerts } from '../../shared/alerts';
import { Feedback } from '../../shared/feedback';

@Component({
  selector: 'app-profile',
  imports: [DatePipe, RouterLink, Alerts],
  templateUrl: './profile.html',
})
export class Profile implements OnInit {
  private readonly userApi = inject(UserApi);
  private readonly loanApi = inject(LoanApi);
  private readonly bookApi = inject(BookApi);
  protected readonly auth = inject(Auth);
  protected readonly feedback = new Feedback();
  protected readonly label = label;
  protected readonly user = signal<User | null>(null);
  protected readonly loans = signal<Loan[] | null>(null);
  protected readonly titles = signal<Record<number, string>>({});

  async ngOnInit(): Promise<void> {
    await this.feedback.run(async () => this.user.set(await firstValueFrom(this.userApi.me())));
    if (this.auth.isMember()) {
      await this.loadLoans();
    }
  }

  private async loadLoans(): Promise<void> {
    const notice = this.feedback.notice();
    await this.feedback.run(async () => {
      const loans = (await firstValueFrom(this.loanApi.mine())).content;
      this.loans.set(loans);
      const ids = [...new Set(loans.map((loan) => loan.bookId))];
      const books = await Promise.all(
        ids.map((id) => firstValueFrom(this.bookApi.get(id)).catch(() => null)),
      );
      const titles: Record<number, string> = {};
      books.forEach((book, index) => (titles[ids[index]] = book?.title ?? `Book #${ids[index]}`));
      this.titles.set(titles);
    });
    this.feedback.notice.set(notice);
  }
}
