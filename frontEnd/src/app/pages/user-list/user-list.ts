import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { UserApi } from '../../core/api/apis';
import { Auth } from '../../core/auth/auth';
import { Page, USER_ROLES, User, UserRole, label } from '../../core/models';
import { Alerts } from '../../shared/alerts';
import { Feedback } from '../../shared/feedback';
import { Pager } from '../../shared/pager';

@Component({
  selector: 'app-user-list',
  imports: [RouterLink, Pager, Alerts],
  templateUrl: './user-list.html',
})
export class UserList implements OnInit {
  private readonly userApi = inject(UserApi);
  protected readonly auth = inject(Auth);
  protected readonly feedback = new Feedback();
  protected readonly roles = USER_ROLES;
  protected readonly label = label;

  protected readonly page = signal<Page<User> | null>(null);
  protected readonly loading = signal(true);
  protected readonly role = signal<UserRole | ''>('');

  ngOnInit(): Promise<void> {
    return this.load(0);
  }

  protected async load(page: number): Promise<void> {
    this.loading.set(true);
    await this.feedback.run(async () => {
      this.page.set(await firstValueFrom(this.userApi.list({ role: this.role(), page, size: 20 })));
    });
    this.loading.set(false);
  }

  protected setRole(role: string): void {
    this.role.set(role as UserRole | '');
    this.load(0);
  }

  protected isMe(user: User): boolean {
    return user.username === this.auth.profile()?.username;
  }

  protected async changeRole(user: User, role: string): Promise<void> {
    await this.update(
      () => firstValueFrom(this.userApi.changeRole(user.id, role as UserRole)),
      `${user.username} is now ${label(role)}. The new role applies at the next sign-in.`,
    );
  }

  protected async toggleEnabled(user: User): Promise<void> {
    await this.update(
      () => firstValueFrom(this.userApi.setEnabled(user.id, !user.enabled)),
      `${user.username} has been ${user.enabled ? 'disabled' : 'enabled'}.`,
    );
  }

  protected async remove(user: User): Promise<void> {
    if (!confirm(`Delete the account ${user.username} from Keycloak?`)) {
      return;
    }
    await this.update(
      () => firstValueFrom(this.userApi.delete(user.id)),
      `${user.username} has been deleted.`,
    );
  }

  private async update(action: () => Promise<unknown>, success: string): Promise<void> {
    const done = await this.feedback.run(action, success);
    const notice = this.feedback.notice();
    await this.load(this.page()?.page ?? 0);
    if (done) {
      this.feedback.notice.set(notice);
    }
  }
}
