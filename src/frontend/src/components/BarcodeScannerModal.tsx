import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import BarcodeScanner, { type ScanStatus } from "./BarcodeScanner";
import { cn } from "@/lib/utils";

interface BarcodeScannerModalProps {
  isOpen: boolean;
  onClose: () => void;
  onScan: (decodedText: string) => void;
}

export default function BarcodeScannerModal({
  isOpen,
  onClose,
  onScan,
}: BarcodeScannerModalProps) {
  const [error, setError] = useState<string | null>(null);
  const [scanStatus, setScanStatus] = useState<ScanStatus>("idle");

  const handleScan = (decodedText: string) => {
    onScan(decodedText);
    onClose();
  };

  const handleError = (message: string) => {
    setError(message);
  };

  return (
    <Dialog
      open={isOpen}
      onOpenChange={(open) => {
        if (!open) {
          setError(null);
          setScanStatus("idle");
          onClose();
        }
      }}
    >
      <DialogContent className={cn(
        "sm:max-w-md transition-shadow duration-300",
        scanStatus === "detected" && "shadow-[0_0_20px_rgba(34,197,94,0.2)]",
      )}>
        <DialogHeader>
          <DialogTitle>Scanner un code</DialogTitle>
          <DialogDescription>
            Placez le code-barres ou QR code devant la caméra.
          </DialogDescription>
        </DialogHeader>

        {error ? (
          <div className="text-center py-8 space-y-2">
            <p className="text-sm text-destructive">{error}</p>
            <p className="text-xs text-muted-foreground">
              Vérifiez que votre navigateur a accès à la caméra.
            </p>
          </div>
        ) : (
          isOpen && (
            <BarcodeScanner
              onScan={handleScan}
              onError={handleError}
              onStatusChange={setScanStatus}
            />
          )
        )}
      </DialogContent>
    </Dialog>
  );
}
