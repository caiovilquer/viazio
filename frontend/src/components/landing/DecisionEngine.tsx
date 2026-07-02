import { Reveal } from "@/components/shared/Reveal";

interface EngineRow {
  icon: string;
  label: string;
  tag: string;
  description: string;
  source: string;
}

const scoredRows: EngineRow[] = [
  {
    icon: "☀️",
    label: "Clima",
    tag: "peso 25%",
    description:
      "Temperatura média e chance de chuva no período exato da viagem: previsão de até 16 dias, climatologia histórica além disso.",
    source: "Open-Meteo",
  },
  {
    icon: "💰",
    label: "Custo de vida",
    tag: "peso 25%",
    description:
      "Nível de preços do destino comparado ao Brasil, por paridade de poder de compra. Não é só câmbio: é quanto rende o seu real lá.",
    source: "World Bank (PPP)",
  },
  {
    icon: "✈️",
    label: "Distância",
    tag: "peso 25%",
    description:
      "Quilometragem real entre origem e destino, tempo de voo estimado e diferença de fuso horário. Viagens curtas pontuam melhor.",
    source: "Coordenadas Wikidata",
  },
  {
    icon: "🎊",
    label: "Festividades",
    tag: "peso 25%",
    description:
      "Feriados e eventos do destino na mesma janela das suas datas. Um destino animado no seu período pontua mais que um fora de época.",
    source: "Nager.Date",
  },
];

const contextRows: EngineRow[] = [
  {
    icon: "💱",
    label: "Câmbio",
    tag: "informativo",
    description:
      "Cotação da moeda local em reais, atualizada diariamente. Não entra na nota (o poder de compra já está no custo), mas aparece em cada card.",
    source: "AwesomeAPI",
  },
  {
    icon: "📊",
    label: "Confiança dos dados",
    tag: "informativo",
    description:
      "Quando um critério não tem dado disponível para aquele destino, ele não pontua, e a nota final é descontada pela cobertura, não inflada.",
    source: "Motor Viazio",
  },
];

function EngineRowItem({ row }: { row: EngineRow }) {
  return (
    <div className="flex gap-3.5 border-t border-hairline/70 py-4 first:border-t-0 sm:gap-4">
      <span
        aria-hidden
        className="mt-0.5 flex size-9 shrink-0 items-center justify-center rounded-xl border border-hairline bg-surface-2 text-base"
      >
        {row.icon}
      </span>
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-baseline justify-between gap-x-3 gap-y-1">
          <p className="font-display text-[1.05rem] tracking-tight">
            {row.label}
          </p>
          <span className="rounded-full border border-hairline px-2 py-0.5 text-[0.65rem] font-medium uppercase tracking-wide text-muted-foreground">
            {row.tag}
          </span>
        </div>
        <p className="mt-1 text-sm leading-relaxed text-muted-foreground">
          {row.description}
        </p>
        <p className="mt-1.5 text-[0.7rem] uppercase tracking-[0.12em] text-gold/70">
          {row.source}
        </p>
      </div>
    </div>
  );
}

export function DecisionEngine() {
  return (
    <Reveal>
      <div className="overflow-hidden rounded-3xl border border-hairline bg-surface/50 elevate sm:rounded-[1.75rem]">
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-hairline bg-surface-2/40 px-5 py-4 sm:px-7">
          <p className="text-sm font-medium text-foreground/85">
            Perfil equilibrado · pesos ajustáveis na busca
          </p>
          <p className="text-xs text-muted-foreground">
            4 critérios pontuados + 2 sinais de contexto
          </p>
        </div>
        <div className="px-5 sm:px-7">
          {scoredRows.map((row) => (
            <EngineRowItem key={row.label} row={row} />
          ))}
        </div>
        <div className="border-t border-hairline bg-surface-2/20 px-5 sm:px-7">
          {contextRows.map((row) => (
            <EngineRowItem key={row.label} row={row} />
          ))}
        </div>
      </div>
    </Reveal>
  );
}
