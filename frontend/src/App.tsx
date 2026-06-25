import { Route, Routes } from 'react-router-dom'
import { AppShell } from '@/components/layout/AppShell'
import { LandingPage } from '@/pages/LandingPage'
import { SearchPage } from '@/pages/SearchPage'
import { ResultsPage } from '@/pages/ResultsPage'
import { DestinationPage } from '@/pages/DestinationPage'
import { ComparePage } from '@/pages/ComparePage'
import { FavoritesPage } from '@/pages/FavoritesPage'
import { BestWindowsPage } from '@/pages/BestWindowsPage'

export default function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<LandingPage />} />
        <Route path="buscar" element={<SearchPage />} />
        <Route path="resultados" element={<ResultsPage />} />
        <Route path="comparar" element={<ComparePage />} />
        <Route path="destino/:countryCode" element={<DestinationPage />} />
        <Route path="janelas" element={<BestWindowsPage />} />
        <Route path="salvos" element={<FavoritesPage />} />
      </Route>
    </Routes>
  )
}
