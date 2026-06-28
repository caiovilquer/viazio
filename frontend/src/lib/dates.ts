/** Data local no formato ISO (YYYY-MM-DD), sem deslocamento UTC. */
export function toLocalIsoDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

/** Hoje no fuso local, com deslocamento opcional em dias. */
export function todayIso(offsetDays = 0): string {
  const date = new Date();
  date.setDate(date.getDate() + offsetDays);
  return toLocalIsoDate(date);
}
