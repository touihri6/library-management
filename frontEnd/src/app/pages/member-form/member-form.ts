import { Component, OnInit, inject, input } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { MemberApi } from '../../core/api/apis';
import { MEMBERSHIP_TYPES, MemberRequest, MembershipType, label } from '../../core/models';
import { today } from '../../shared/dates';
import { Alerts } from '../../shared/alerts';
import { Feedback } from '../../shared/feedback';

@Component({
  selector: 'app-member-form',
  imports: [ReactiveFormsModule, RouterLink, Alerts],
  templateUrl: './member-form.html',
})
export class MemberForm implements OnInit {
  readonly id = input<string>();

  private readonly memberApi = inject(MemberApi);
  private readonly router = inject(Router);
  protected readonly feedback = new Feedback();
  protected readonly membershipTypes = MEMBERSHIP_TYPES;
  protected readonly label = label;
  protected readonly maxDate = today();

  protected readonly form = inject(FormBuilder).nonNullable.group({
    firstName: ['', [Validators.required, Validators.maxLength(80)]],
    lastName: ['', [Validators.required, Validators.maxLength(80)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    phone: ['', Validators.maxLength(20)],
    membershipType: ['STANDARD' as MembershipType, Validators.required],
    joinDate: [today(), Validators.required],
  });

  async ngOnInit(): Promise<void> {
    const id = this.id();
    if (id) {
      await this.feedback.run(async () => {
        const member = await firstValueFrom(this.memberApi.get(Number(id)));
        this.form.patchValue({ ...member, phone: member.phone ?? '' });
      });
    }
  }

  protected async save(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: MemberRequest = { ...value, phone: value.phone.trim() || null };
    const id = this.id();
    const done = await this.feedback.run(() =>
      firstValueFrom(
        id ? this.memberApi.update(Number(id), request) : this.memberApi.create(request),
      ),
    );
    if (done) {
      await this.router.navigate(['/members']);
    }
  }

  protected invalid(name: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[name];
    return control.touched && control.invalid;
  }
}
