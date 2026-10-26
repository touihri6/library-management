import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Auth } from './core/auth/auth';
import { label } from './core/models';

@Component({
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly auth = inject(Auth);
  protected readonly roleLabel = computed(
    () => this.auth.profile()?.roles.map(label).join(' · ') ?? '',
  );

  protected login(): void {
    this.auth.login();
  }

  protected logout(): void {
    this.auth.logout();
  }
}
