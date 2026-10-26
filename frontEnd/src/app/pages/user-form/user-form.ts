import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { UserApi } from '../../core/api/apis';
import { USER_ROLES, UserRole, label } from '../../core/models';
import { accountControls } from '../../shared/account-fields';
import { Alerts } from '../../shared/alerts';
import { Feedback } from '../../shared/feedback';

@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule, RouterLink, Alerts],
  templateUrl: './user-form.html',
})
export class UserForm {
  private readonly userApi = inject(UserApi);
  private readonly router = inject(Router);
  protected readonly feedback = new Feedback();
  protected readonly roles = USER_ROLES;
  protected readonly label = label;

  protected readonly form = inject(FormBuilder).nonNullable.group({
    ...accountControls(),
    role: ['LIBRARIAN' as UserRole, Validators.required],
  });

  protected async save(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const done = await this.feedback.run(() =>
      firstValueFrom(this.userApi.create(this.form.getRawValue())),
    );
    if (done) {
      await this.router.navigate(['/users']);
    }
  }

  protected invalid(name: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[name];
    return control.touched && control.invalid;
  }
}
