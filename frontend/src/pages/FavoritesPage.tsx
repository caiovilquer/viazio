import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Heart } from 'lucide-react'
import { useFavorites } from '@/lib/favorites'
import { RecommendationCard } from '@/components/results/RecommendationCard'
import { Button } from '@/components/ui/button'

export function FavoritesPage() {
  const favorites = useFavorites()

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <div className="mb-6">
        <h1 className="font-display text-2xl font-semibold sm:text-3xl">Destinos salvos</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          {favorites.length === 0
            ? 'Toque no coração de um destino para guardá-lo aqui.'
            : `${favorites.length} destino${favorites.length === 1 ? '' : 's'} salvo${favorites.length === 1 ? '' : 's'}`}
        </p>
      </div>

      {favorites.length === 0 ? (
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex flex-col items-center gap-4 rounded-2xl border border-border bg-card p-12 text-center text-muted-foreground"
        >
          <Heart className="size-10" />
          <p>Nenhum destino salvo ainda.</p>
          <Button asChild>
            <Link to="/buscar">Buscar destinos</Link>
          </Button>
        </motion.div>
      ) : (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {favorites.map((entry, i) => (
            <RecommendationCard key={entry.countryCode} recommendation={entry.recommendation} rank={i + 1} searchQuery="" />
          ))}
        </div>
      )}
    </div>
  )
}
