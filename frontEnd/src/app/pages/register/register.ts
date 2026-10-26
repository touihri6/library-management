import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { UserApi } from '../../core/api/apis';
import { Auth } from '../../core/auth/auth';
import { accountControls } from '../../shared/account-fields';
import { Alerts } from '../../shared/alerts';
import { Feedback } from '../../shared/feedback';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink, Alerts],
  templateUrl: './register.html',
})
export class Register {
  private readonly userApi = inject(UserApi);
  protected readonly auth = inject(Auth);
  protected readonly feedback = new Feedback();
  protected readonly registeredUsername = signal<string | null>(null);

  protected readonly form = inject(FormBuilder).nonNullable.group(accountControls());

  protected async register(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const done = await this.feedback.run(() => firstValueFrom(this.userApi.register(value)));
    if (done) {
      this.registeredUsername.set(value.username);
    }
  }

  protected signIn(): void {
    this.auth.login('/books', this.registeredUsername() ?? undefined);
  }

  protected invalid(name: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[name];
    return control.touched && control.invalid;
  }
}
