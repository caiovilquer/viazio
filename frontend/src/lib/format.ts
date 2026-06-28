export function formatBrl(value: number) {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(value);
}

export function formatDate(iso: string) {
  const date = new Date(`${iso}T00:00:00`);
  return new Intl.DateTimeFormat("pt-BR", {
    day: "2-digit",
    month: "short",
  }).format(date);
}

export function formatDateLong(iso: string) {
  const date = new Date(`${iso}T00:00:00`);
  return new Intl.DateTimeFormat("pt-BR", {
    day: "2-digit",
    month: "long",
    year: "numeric",
  }).format(date);
}

export function formatDateRange(fromIso: string, toIso: string) {
  return `${formatDate(fromIso)} – ${formatDate(toIso)}`;
}

export function formatScore(score: number) {
  return Math.round(score);
}

/** Pluralização real em pt-BR — substitui placeholders do backend no estilo "dia(s)". */
export function pluralize(count: number, singular: string, plural: string) {
  return `${count} ${count === 1 ? singular : plural}`;
}

/** Resumo humano da janela a partir de campos estruturados (sem ponto e vírgula / "(s)"). */
export function describeWindow(window: {
  totalDays: number;
  freeDays: number;
  requiredLeaveDays: number;
}) {
  const { totalDays, freeDays, requiredLeaveDays } = window;
  if (requiredLeaveDays <= 0) {
    return `Os ${totalDays} dias já são livres — sem gastar férias.`;
  }
  return `${freeDays} de ${totalDays} dias já são livres · requer ${pluralize(
    requiredLeaveDays,
    "dia",
    "dias",
  )} de férias.`;
}

export function scoreTone(score: number) {
  if (score >= 80) return "excellent";
  if (score >= 60) return "good";
  if (score >= 40) return "fair";
  return "poor";
}

/** Faixas quentes para visualizações de nota (ScoreRing, ClimateChart, mapa). */
export const scoreTierColor: Record<string, string> = {
  excellent: "var(--gold)",
  good: "var(--primary)",
  fair: "var(--chart-3)",
  poor: "var(--chart-5)",
};

interface ExchangeLike {
  currency: string;
  valueInReais: number;
}

/**
 * Escolhe unidade de cotação (1 / 100 / 1.000) para o valor em BRL ficar legível tanto
 * para moedas fortes (1 EUR = R$ 5,92) quanto fracas (1.000 COP = R$ 1,52) —
 * cotar por 1 unidade arredondaria moedas pequenas para R$ 0,00. Retorna null quando
 * não há taxa utilizável (ausente ou zero).
 */
export function exchangeUnit(exchange?: ExchangeLike | null) {
  if (!exchange || !(exchange.valueInReais > 0)) return null;
  const v = exchange.valueInReais;
  const unit = v >= 0.1 ? 1 : v >= 0.01 ? 100 : 1000;
  return {
    unit,
    unitLabel: unit.toLocaleString("pt-BR"),
    amount: v * unit,
    currency: exchange.currency,
  };
}

function resolveExchangeQuote(
  exchange?: ExchangeLike | null,
  originExchange?: ExchangeLike | null,
  originCountryCode?: string,
  destinationCountryCode?: string,
) {
  // Destino é o próprio Brasil (moeda BRL): o backend não envia exchangeToBrl.
  // Nesse caso, cotar o real na moeda de origem quando a origem não é BR.
  if (
    !exchange &&
    destinationCountryCode === "BR" &&
    originExchange &&
    originExchange.valueInReais > 0 &&
    originCountryCode &&
    originCountryCode !== "BR"
  ) {
    const converted = 1 / originExchange.valueInReais;
    const amountFormatted = new Intl.NumberFormat("pt-BR", {
      style: "currency",
      currency: originExchange.currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 4,
    }).format(converted);
    return {
      unitLabel: "1",
      currency: "BRL",
      amountFormatted,
      showFallbackNote: false,
      quote: `1 BRL = ${amountFormatted}`,
    };
  }

  const e = exchangeUnit(exchange);
  if (!e) return null;

  if (originExchange && originExchange.valueInReais > 0) {
    const converted = e.amount / originExchange.valueInReais;
    const amountFormatted = new Intl.NumberFormat("pt-BR", {
      style: "currency",
      currency: originExchange.currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(converted);
    return {
      unitLabel: e.unitLabel,
      currency: e.currency,
      amountFormatted,
      showFallbackNote: false,
      quote: `${e.unitLabel} ${e.currency} = ${amountFormatted}`,
    };
  }

  const amountFormatted = formatBrl(e.amount);
  const showFallbackNote =
    originCountryCode != null && originCountryCode !== "BR";
  const suffix = showFallbackNote ? " (câmbio da origem indisponível)" : "";
  return {
    unitLabel: e.unitLabel,
    currency: e.currency,
    amountFormatted,
    showFallbackNote,
    quote: `${e.unitLabel} ${e.currency} = ${amountFormatted}${suffix}`,
  };
}

/** Cotação de câmbio em uma linha, ex.: "1 EUR = R$ 5,92" ou "1 USD = CA$ 1,36". */
export function formatExchange(
  exchange?: ExchangeLike | null,
  originExchange?: ExchangeLike | null,
  originCountryCode?: string,
  destinationCountryCode?: string,
): string | null {
  return (
    resolveExchangeQuote(
      exchange,
      originExchange,
      originCountryCode,
      destinationCountryCode,
    )?.quote ?? null
  );
}

/** Divide a cotação para cards de estatística: valor na moeda de origem + rótulo da unidade. */
export function formatExchangeParts(
  exchange?: ExchangeLike | null,
  originExchange?: ExchangeLike | null,
  originCountryCode?: string,
  destinationCountryCode?: string,
) {
  const q = resolveExchangeQuote(
    exchange,
    originExchange,
    originCountryCode,
    destinationCountryCode,
  );
  if (!q) return null;
  return {
    amount: q.amountFormatted,
    unitDescription: `por ${q.unitLabel} ${q.currency}`,
    showFallbackNote: q.showFallbackNote,
  };
}

export function formatInOriginCurrency(
  brlValue: number,
  originExchange: ExchangeLike | null | undefined,
  originCountryCode?: string,
): { formatted: string; isFallback: boolean; showFallbackNote: boolean } {
  if (originExchange && originExchange.valueInReais > 0) {
    const converted = brlValue / originExchange.valueInReais;
    const formatted = new Intl.NumberFormat("pt-BR", {
      style: "currency",
      currency: originExchange.currency,
      maximumFractionDigits: 0,
    }).format(converted);
    return { formatted, isFallback: false, showFallbackNote: false };
  }
  return {
    formatted: formatBrl(brlValue),
    isFallback: true,
    showFallbackNote: originCountryCode != null && originCountryCode !== "BR",
  };
}
