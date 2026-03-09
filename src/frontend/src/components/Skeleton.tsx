import { Skeleton as UISkeleton } from '@/components/ui/skeleton';

interface SkeletonProps {
  className?: string;
}

export function Skeleton({ className = '' }: SkeletonProps) {
  return <UISkeleton className={className} />;
}

export function SkeletonText({ className = '' }: SkeletonProps) {
  return <UISkeleton className={`h-4 ${className}`} />;
}

export function SkeletonCard() {
  return (
    <div className="rounded-xl border bg-card overflow-hidden">
      <UISkeleton className="h-48 rounded-none" />
      <div className="p-5 space-y-3">
        <div className="flex justify-between items-start">
          <UISkeleton className="h-4 w-2/3" />
          <UISkeleton className="h-6 w-20 rounded-full" />
        </div>
        <UISkeleton className="h-4 w-1/3" />
        <div className="flex gap-2 pt-2">
          <UISkeleton className="h-10 flex-1 rounded-lg" />
          <UISkeleton className="h-10 w-12 rounded-lg" />
        </div>
      </div>
    </div>
  );
}

export function SkeletonStatCard() {
  return (
    <div className="rounded-xl border bg-card p-5 shadow-sm">
      <div className="flex items-start justify-between mb-4">
        <UISkeleton className="h-4 w-24" />
        <UISkeleton className="h-4 w-4" />
      </div>
      <div className="space-y-2">
        <UISkeleton className="h-9 w-16" />
        <UISkeleton className="h-3 w-32" />
      </div>
    </div>
  );
}
