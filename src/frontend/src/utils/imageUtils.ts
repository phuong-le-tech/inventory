const ALLOWED_PROTOCOLS = ["https:"];
const SAFE_DATA_IMAGE_TYPES = ["image/png", "image/jpeg", "image/gif", "image/webp"];

export function sanitizeImageUrl(url: string | null | undefined): string | null {
  if (!url) return null;

  // Allow same-origin relative paths (e.g., /api/images/...)
  if (url.startsWith("/")) return url;

  // Handle data: URLs — restrict to safe image types only, explicitly rejecting SVG
  if (url.startsWith("data:")) {
    const semicolonIdx = url.indexOf(";");
    const mimeType = semicolonIdx !== -1 ? url.slice(5, semicolonIdx) : null;
    if (mimeType && SAFE_DATA_IMAGE_TYPES.includes(mimeType)) return url;
    return null;
  }

  try {
    const parsed = new URL(url);
    if (ALLOWED_PROTOCOLS.includes(parsed.protocol)) return url;
    return null;
  } catch {
    return null;
  }
}
