import { CalendarRange, Compass, Search } from 'lucide-react'

export const navItems = [
  { to: '/', label: 'Início', icon: Compass, end: true },
  { to: '/buscar', label: 'Buscar', icon: Search, end: false },
  { to: '/janelas', label: 'Janelas', icon: CalendarRange, end: false },
] as const
