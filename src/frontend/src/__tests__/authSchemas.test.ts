import { describe, it, expect } from 'vitest';
import { signupSchema } from '../schemas/auth.schemas';

describe('signupSchema', () => {
  it('accepts valid signup data', () => {
    const result = signupSchema.safeParse({
      email: 'user@example.com',
      password: 'StrongPass123!',
      confirmPassword: 'StrongPass123!',
    });
    expect(result.success).toBe(true);
  });

  it('rejects invalid email', () => {
    const result = signupSchema.safeParse({
      email: 'not-an-email',
      password: 'StrongPass123!',
      confirmPassword: 'StrongPass123!',
    });
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.error.issues[0].path).toContain('email');
    }
  });

  it('rejects password shorter than 12 characters', () => {
    const result = signupSchema.safeParse({
      email: 'user@example.com',
      password: 'Short1abc',
      confirmPassword: 'Short1abc',
    });
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.error.issues[0].path).toContain('password');
    }
  });

  it('rejects password without uppercase letter', () => {
    const result = signupSchema.safeParse({
      email: 'user@example.com',
      password: 'alllowercase1',
      confirmPassword: 'alllowercase1',
    });
    expect(result.success).toBe(false);
  });

  it('rejects password without lowercase letter', () => {
    const result = signupSchema.safeParse({
      email: 'user@example.com',
      password: 'ALLUPPERCASE1',
      confirmPassword: 'ALLUPPERCASE1',
    });
    expect(result.success).toBe(false);
  });

  it('rejects password without digit', () => {
    const result = signupSchema.safeParse({
      email: 'user@example.com',
      password: 'NoDigitsHere!',
      confirmPassword: 'NoDigitsHere!',
    });
    expect(result.success).toBe(false);
  });

  it('rejects password without special character', () => {
    const result = signupSchema.safeParse({
      email: 'user@example.com',
      password: 'StrongPass1234',
      confirmPassword: 'StrongPass1234',
    });
    expect(result.success).toBe(false);
  });

  it('rejects mismatched passwords', () => {
    const result = signupSchema.safeParse({
      email: 'user@example.com',
      password: 'StrongPass123!',
      confirmPassword: 'Different456!',
    });
    expect(result.success).toBe(false);
    if (!result.success) {
      const confirmError = result.error.issues.find(i => i.path.includes('confirmPassword'));
      expect(confirmError).toBeDefined();
      expect(confirmError!.message).toBe('Les mots de passe ne correspondent pas');
    }
  });

  it('rejects empty confirm password', () => {
    const result = signupSchema.safeParse({
      email: 'user@example.com',
      password: 'StrongPass123!',
      confirmPassword: '',
    });
    expect(result.success).toBe(false);
  });
});
