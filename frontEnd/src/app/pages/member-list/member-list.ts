import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { MemberApi } from '../../core/api/apis';
import { MEMBERSHIP_TYPES, Member, MembershipType, Page, label } from '../../core/models';
import { Alerts } from '../../shared/alerts';
import { Feedback } from '../../shared/feedback';
import { Pager } from '../../shared/pager';

@Component({
  selector: 'app-member-list',
  imports: [RouterLink, DatePipe, Pager, Alerts],
  templateUrl: './member-list.html',
})
export class MemberList implements OnInit {
  private readonly memberApi = inject(MemberApi);
  protected readonly feedback = new Feedback();
  protected readonly membershipTypes = MEMBERSHIP_TYPES;
  protected readonly label = label;

  protected readonly page = signal<Page<Member> | null>(null);
  protected readonly loading = signal(true);
  protected readonly lastName = signal('');
  protected readonly membershipType = signal<MembershipType | ''>('');

  ngOnInit(): Promise<void> {
    return this.load(0);
  }

  protected async load(page: number): Promise<void> {
    this.loading.set(true);
    await this.feedback.run(async () => {
      this.page.set(
        await firstValueFrom(
          this.memberApi.list({
            lastName: this.lastName().trim(),
            membershipType: this.membershipType(),
            page,
            size: 20,
          }),
        ),
      );
    });
    this.loading.set(false);
  }

  protected search(lastName: string): void {
    this.lastName.set(lastName);
    this.load(0);
  }

  protected setMembershipType(type: string): void {
    this.membershipType.set(type as MembershipType | '');
    this.load(0);
  }

  protected async remove(member: Member): Promise<void> {
    if (!confirm(`Delete the member ${member.firstName} ${member.lastName}?`)) {
      return;
    }
    const done = await this.feedback.run(
      () => firstValueFrom(this.memberApi.delete(member.id)),
      `${member.firstName} ${member.lastName} has been deleted.`,
    );
    if (done) {
      await this.load(this.page()?.page ?? 0);
    }
  }
}
