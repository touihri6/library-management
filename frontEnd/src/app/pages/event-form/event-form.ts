import { Component, OnInit, inject, input } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { EventApi } from '../../core/api/apis';
import { EVENT_TYPES, EventType, LibraryEventRequest, label } from '../../core/models';
import { Alerts } from '../../shared/alerts';
import { Feedback } from '../../shared/feedback';

@Component({
  selector: 'app-event-form',
  imports: [ReactiveFormsModule, RouterLink, Alerts],
  templateUrl: './event-form.html',
})
export class EventForm implements OnInit {
  readonly id = input<string>();

  private readonly eventApi = inject(EventApi);
  private readonly router = inject(Router);
  protected readonly feedback = new Feedback();
  protected readonly types = EVENT_TYPES;
  protected readonly label = label;

  protected readonly form = inject(FormBuilder).nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', Validators.maxLength(1000)],
    type: ['READING_CLUB' as EventType, Validators.required],
    eventDate: ['', Validators.required],
    location: ['', [Validators.required, Validators.maxLength(120)]],
    capacity: [20, [Validators.required, Validators.min(1)]],
  });

  async ngOnInit(): Promise<void> {
    const id = this.id();
    if (id) {
      await this.feedback.run(async () => {
        const event = await firstValueFrom(this.eventApi.get(Number(id)));
        this.form.patchValue({
          ...event,
          description: event.description ?? '',
          eventDate: event.eventDate.slice(0, 16),
        });
      });
    }
  }

  protected async save(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request: LibraryEventRequest = {
      ...value,
      description: value.description.trim() || null,
    };
    const id = this.id();
    const done = await this.feedback.run(() =>
      firstValueFrom(
        id ? this.eventApi.update(Number(id), request) : this.eventApi.create(request),
      ),
    );
    if (done) {
      await this.router.navigate(['/events']);
    }
  }

  protected invalid(name: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[name];
    return control.touched && control.invalid;
  }
}
