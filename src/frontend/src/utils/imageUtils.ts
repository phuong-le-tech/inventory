const ALLOWED_PROTOCOLS = ["https:", "data:"];

export function sanitizeImageUrl(url: string | null | undefined): string | null {
  if (!url) return null;
  try {
    if (url.startsWith("data:image/")) return url;
    const parsed = new URL(url);
    if (ALLOWED_PROTOCOLS.includes(parsed.protocol)) return url;
    return null;
  } catch {
    return null;
  }
}
