import { describe, it, expect } from 'vitest';
import { sanitizeImageUrl } from '../utils/imageUtils';

describe('sanitizeImageUrl', () => {
  it('returns null for null, undefined and empty string', () => {
    expect(sanitizeImageUrl(null)).toBeNull();
    expect(sanitizeImageUrl(undefined)).toBeNull();
    expect(sanitizeImageUrl('')).toBeNull();
  });

  it('allows https URLs', () => {
    const url = 'https://example.com/image.png';
    expect(sanitizeImageUrl(url)).toBe(url);
  });

  it('rejects http URLs', () => {
    expect(sanitizeImageUrl('http://example.com/image.png')).toBeNull();
  });

  it('rejects javascript: URLs', () => {
    expect(sanitizeImageUrl('javascript:alert(1)')).toBeNull();
  });

  it('allows same-origin relative paths', () => {
    expect(sanitizeImageUrl('/api/images/foo.png')).toBe('/api/images/foo.png');
    expect(sanitizeImageUrl('/static/img/avatar.jpg')).toBe('/static/img/avatar.jpg');
  });

  it('allows safe data: image types', () => {
    const png = 'data:image/png;base64,abc123';
    const jpeg = 'data:image/jpeg;base64,abc123';
    const gif = 'data:image/gif;base64,abc123';
    const webp = 'data:image/webp;base64,abc123';
    expect(sanitizeImageUrl(png)).toBe(png);
    expect(sanitizeImageUrl(jpeg)).toBe(jpeg);
    expect(sanitizeImageUrl(gif)).toBe(gif);
    expect(sanitizeImageUrl(webp)).toBe(webp);
  });

  it('rejects SVG data URLs', () => {
    expect(sanitizeImageUrl('data:image/svg+xml;base64,abc123')).toBeNull();
    expect(sanitizeImageUrl('data:image/svg+xml,<svg><script>alert(1)</script></svg>')).toBeNull();
  });

  it('rejects non-image data: URLs', () => {
    expect(sanitizeImageUrl('data:text/html,<h1>hi</h1>')).toBeNull();
    expect(sanitizeImageUrl('data:application/javascript,alert(1)')).toBeNull();
  });

  it('rejects data: URLs with no semicolon (malformed)', () => {
    expect(sanitizeImageUrl('data:image/png')).toBeNull();
  });

  it('returns null for invalid URLs', () => {
    expect(sanitizeImageUrl('not a url')).toBeNull();
    expect(sanitizeImageUrl('ftp://example.com/img.png')).toBeNull();
  });
});
