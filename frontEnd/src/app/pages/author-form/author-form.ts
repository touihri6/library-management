import { Component, OnInit, inject, input } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthorApi } from '../../core/api/apis';
import { AuthorRequest } from '../../core/models';
import { Alerts } from '../../shared/alerts';
import { Feedback } from '../../shared/feedback';

@Component({
  selector: 'app-author-form',
  imports: [ReactiveFormsModule, RouterLink, Alerts],
  templateUrl: './author-form.html',
})
export class AuthorForm implements OnInit {
  readonly id = input<string>();

  private readonly authorApi = inject(AuthorApi);
  private readonly router = inject(Router);
  protected readonly feedback = new Feedback();

  protected readonly form = inject(FormBuilder).nonNullable.group({
    firstName: ['', [Validators.required, Validators.maxLength(80)]],
    lastName: ['', [Validators.required, Validators.maxLength(80)]],
    nationality: ['', [Validators.required, Validators.maxLength(60)]],
    birthYear: [null as number | null, [Validators.min(1000), Validators.max(2100)]],
    biography: ['', Validators.maxLength(2000)],
  });

  async ngOnInit(): Promise<void> {
    const id = this.id();
    if (id) {
      await this.feedback.run(async () => {
        const author = await firstValueFrom(this.authorApi.get(Number(id)));
        this.form.patchValue({ ...author, biography: author.biography ?? '' });
      });
    }
  }

  protected async save(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: AuthorRequest = { ...value, biography: value.biography.trim() || null };
    const id = this.id();
    const done = await this.feedback.run(() =>
      firstValueFrom(
        id ? this.authorApi.update(Number(id), request) : this.authorApi.create(request),
      ),
    );
    if (done) {
      await this.router.navigate(['/authors']);
    }
  }

  protected invalid(name: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[name];
    return control.touched && control.invalid;
  }
}
