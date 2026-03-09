import { useCallback, useEffect } from "react";

const DEFAULT_MESSAGE =
  "Vous avez des modifications non enregistrees. Voulez-vous quitter cette page ?";

export function useUnsavedChangesGuard(
  shouldBlock: boolean,
  message: string = DEFAULT_MESSAGE,
) {
  useEffect(() => {
    if (!shouldBlock) return;

    const handleBeforeUnload = (event: BeforeUnloadEvent) => {
      event.preventDefault();
      event.returnValue = "";
    };

    window.addEventListener("beforeunload", handleBeforeUnload);
    return () => window.removeEventListener("beforeunload", handleBeforeUnload);
  }, [shouldBlock]);

  return useCallback(() => {
    if (!shouldBlock) return true;
    return window.confirm(message);
  }, [message, shouldBlock]);
}
