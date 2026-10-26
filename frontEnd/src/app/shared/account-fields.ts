import { Validators } from '@angular/forms';

export const USERNAME_PATTERN = /^[a-zA-Z0-9._-]+$/;

export function accountControls() {
  return {
    username: [
      '',
      [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(50),
        Validators.pattern(USERNAME_PATTERN),
      ],
    ],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    firstName: ['', [Validators.required, Validators.maxLength(80)]],
    lastName: ['', [Validators.required, Validators.maxLength(80)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(100)]],
  };
}
