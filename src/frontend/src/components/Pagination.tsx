import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";

interface PaginationProps {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

function getPageNumbers(current: number, total: number): (number | 'ellipsis')[] {
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i);
  }

  const pages: (number | 'ellipsis')[] = [0];

  if (current > 2) {
    pages.push('ellipsis');
  }

  const start = Math.max(1, current - 1);
  const end = Math.min(total - 2, current + 1);

  for (let i = start; i <= end; i++) {
    pages.push(i);
  }

  if (current < total - 3) {
    pages.push('ellipsis');
  }

  pages.push(total - 1);

  return pages;
}

export function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null;

  const pageNumbers = getPageNumbers(page, totalPages);

  return (
    <div className="flex items-center justify-center gap-1 mt-8">
      <Button
        variant="ghost"
        size="icon"
        className="h-9 w-9"
        onClick={() => onPageChange(Math.max(0, page - 1))}
        disabled={page === 0}
        aria-label="Page précédente"
      >
        <ChevronLeft className="h-4 w-4" />
      </Button>

      {pageNumbers.map((p, idx) =>
        p === 'ellipsis' ? (
          <span key={`ellipsis-${idx}`} className="px-2 text-sm text-muted-foreground">
            ...
          </span>
        ) : (
          <Button
            key={p}
            variant={p === page ? 'default' : 'ghost'}
            size="icon"
            className="h-9 w-9 text-sm"
            onClick={() => onPageChange(p)}
            aria-label={`Page ${p + 1}`}
            aria-current={p === page ? 'page' : undefined}
          >
            {p + 1}
          </Button>
        )
      )}

      <Button
        variant="ghost"
        size="icon"
        className="h-9 w-9"
        onClick={() => onPageChange(Math.min(totalPages - 1, page + 1))}
        disabled={page >= totalPages - 1}
        aria-label="Page suivante"
      >
        <ChevronRight className="h-4 w-4" />
      </Button>
    </div>
  );
}
