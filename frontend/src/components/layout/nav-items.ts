import { CalendarRange, Heart, Home, Search } from "lucide-react";

export const navItems = [
  { to: "/", label: "Início", icon: Home, end: true },
  { to: "/buscar", label: "Buscar", icon: Search, end: false },
  { to: "/janelas", label: "Janelas", icon: CalendarRange, end: false },
  { to: "/salvos", label: "Salvos", icon: Heart, end: false },
] as const;
