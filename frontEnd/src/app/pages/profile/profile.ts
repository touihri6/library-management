import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { UserApi } from '../../core/api/apis';
import { Auth } from '../../core/auth/auth';
import { User, label } from '../../core/models';
import { Alerts } from '../../shared/alerts';
import { Feedback } from '../../shared/feedback';

@Component({
  selector: 'app-profile',
  imports: [DatePipe, Alerts],
  templateUrl: './profile.html',
})
export class Profile implements OnInit {
  private readonly userApi = inject(UserApi);
  protected readonly auth = inject(Auth);
  protected readonly feedback = new Feedback();
  protected readonly label = label;
  protected readonly user = signal<User | null>(null);

  async ngOnInit(): Promise<void> {
    await this.feedback.run(async () => this.user.set(await firstValueFrom(this.userApi.me())));
  }
}
