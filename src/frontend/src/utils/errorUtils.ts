import axios from 'axios';

/**
 * Extract a human-readable error message from an unknown error.
 * Uses axios.isAxiosError() for proper type narrowing instead of unsafe `as` casts.
 */
export function getApiErrorMessage(err: unknown, fallback: string): string {
  if (axios.isAxiosError(err)) {
    const serverMessage = err.response?.data?.error?.message;
    if (typeof serverMessage === 'string' && serverMessage.length > 0) {
      return serverMessage;
    }
  }
  if (err instanceof Error) {
    return err.message;
  }
  return fallback;
}

/**
 * Extract the HTTP status code from an unknown error.
 * Returns undefined for non-Axios errors.
 */
export function getApiErrorStatus(err: unknown): number | undefined {
  if (axios.isAxiosError(err)) {
    return err.response?.status;
  }
  return undefined;
}
