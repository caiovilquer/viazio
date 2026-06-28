import type { MouseEvent } from "react";
import { motion } from "framer-motion";
import { Heart } from "lucide-react";
import { toast } from "sonner";
import type { TravelRecommendation } from "@/api/types";
import {
  toggleFavorite,
  useIsFavorite,
  type FavoriteContext,
} from "@/lib/favorites";
import { cn } from "@/lib/utils";

export function FavoriteButton({
  recommendation,
  context,
  className,
  size = "md",
}: {
  recommendation: TravelRecommendation;
  context?: FavoriteContext;
  className?: string;
  size?: "sm" | "md";
}) {
  const saved = useIsFavorite(recommendation.countryCode);

  function handleClick(e: MouseEvent) {
    e.preventDefault();
    e.stopPropagation();
    const willSave = !saved;
    toggleFavorite(recommendation, context);
    toast.success(
      willSave
        ? `${recommendation.countryName} salvo`
        : `${recommendation.countryName} removido dos salvos`,
    );
  }

  return (
    <motion.button
      type="button"
      whileTap={{ scale: 0.85 }}
      onClick={handleClick}
      aria-pressed={saved}
      aria-label={
        saved
          ? `Remover ${recommendation.countryName} dos salvos`
          : `Salvar ${recommendation.countryName}`
      }
      className={cn(
        "flex items-center justify-center rounded-full border backdrop-blur transition-[color,background-color,border-color]",
        size === "sm" ? "size-8" : "size-9",
        saved
          ? "border-primary/50 bg-primary/15 text-primary"
          : "border-hairline bg-background/50 text-foreground/80 hover:border-primary/40 hover:text-primary",
        className,
      )}
    >
      <Heart
        className={size === "sm" ? "size-4" : "size-4"}
        fill={saved ? "currentColor" : "none"}
      />
    </motion.button>
  );
}
