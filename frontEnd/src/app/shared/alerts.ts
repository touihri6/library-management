import { Component, input } from '@angular/core';
import { Feedback } from './feedback';

@Component({
  selector: 'app-alerts',
  template: `
    @if (feedback().notice()) {
      <div class="alert success" role="status">{{ feedback().notice() }}</div>
    }
    @if (feedback().error()) {
      <div class="alert error" role="alert">{{ feedback().error() }}</div>
    }
  `,
})
export class Alerts {
  readonly feedback = input.required<Feedback>();
}
