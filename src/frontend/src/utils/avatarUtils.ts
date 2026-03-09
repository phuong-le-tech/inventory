const PASTEL_COLORS = [
  'bg-brand',
  'bg-status-verify',
  'bg-status-pending',
  'bg-status-ready',
  'bg-status-prepare',
];

export function getAvatarColor(email: string): string {
  let hash = 0;
  for (let i = 0; i < email.length; i++) {
    hash = email.charCodeAt(i) + ((hash << 5) - hash);
  }
  return PASTEL_COLORS[Math.abs(hash) % PASTEL_COLORS.length];
}
