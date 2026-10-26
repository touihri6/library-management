import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { EventApi } from '../../core/api/apis';
import { Auth } from '../../core/auth/auth';
import { EVENT_TYPES, EventType, LibraryEvent, Page, label } from '../../core/models';
import { Alerts } from '../../shared/alerts';
import { Feedback } from '../../shared/feedback';
import { Pager } from '../../shared/pager';

@Component({
  selector: 'app-event-list',
  imports: [RouterLink, DatePipe, Pager, Alerts],
  templateUrl: './event-list.html',
})
export class EventList implements OnInit {
  private readonly eventApi = inject(EventApi);
  protected readonly auth = inject(Auth);
  protected readonly feedback = new Feedback();
  protected readonly types = EVENT_TYPES;
  protected readonly label = label;

  protected readonly page = signal<Page<LibraryEvent> | null>(null);
  protected readonly loading = signal(true);
  protected readonly type = signal<EventType | ''>('');

  ngOnInit(): Promise<void> {
    return this.load(0);
  }

  protected async load(page: number): Promise<void> {
    this.loading.set(true);
    await this.feedback.run(async () => {
      this.page.set(
        await firstValueFrom(this.eventApi.list({ type: this.type(), page, size: 12 })),
      );
    });
    this.loading.set(false);
  }

  protected setType(type: string): void {
    this.type.set(type as EventType | '');
    this.load(0);
  }

  protected isPast(event: LibraryEvent): boolean {
    return new Date(event.eventDate).getTime() < Date.now();
  }

  protected async remove(event: LibraryEvent): Promise<void> {
    if (!confirm(`Delete "${event.title}"?`)) {
      return;
    }
    const done = await this.feedback.run(
      () => firstValueFrom(this.eventApi.delete(event.id)),
      `"${event.title}" has been deleted.`,
    );
    if (done) {
      await this.load(this.page()?.page ?? 0);
    }
  }
}
