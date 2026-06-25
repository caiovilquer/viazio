import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { ArrowRight, CalendarRange, Cloud, Compass, Landmark, MapPinned, Wallet } from 'lucide-react'
import { useMeta } from '@/api/queries'
import { Button } from '@/components/ui/button'
import { ExampleSearchChips } from '@/components/landing/ExampleSearchChips'

const steps = [
  {
    icon: CalendarRange,
    title: 'Diga quando você pode viajar',
    description: 'Escolha as datas e quantos dias de férias você tem disponíveis.',
  },
  {
    icon: Compass,
    title: 'Conte o que importa pra você',
    description: 'Clima, custo, distância ou festividades — escolha um perfil ou personalize os pesos.',
  },
  {
    icon: MapPinned,
    title: 'Receba um ranking explicado',
    description: 'Cruzamos feriados, clima, câmbio e distância para te mostrar os melhores destinos, com score detalhado.',
  },
]

const signals = [
  { icon: Cloud, label: 'Clima' },
  { icon: Wallet, label: 'Custo de vida (PPP)' },
  { icon: MapPinned, label: 'Distância e fuso' },
  { icon: Landmark, label: 'Festividades locais' },
]

export function LandingPage() {
  const { data: meta } = useMeta()

  return (
    <div className="overflow-hidden">
      <section className="relative px-4 pb-20 pt-16 sm:pt-24">
        <div
          aria-hidden
          className="pointer-events-none absolute inset-x-0 -top-32 -z-10 mx-auto h-[36rem] max-w-4xl rounded-full bg-gradient-to-br from-primary/25 via-accent/15 to-transparent blur-3xl"
        />

        <div className="mx-auto max-w-3xl text-center">
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            className="mb-6 inline-flex items-center gap-2 rounded-full border border-border bg-card px-3 py-1 text-xs font-medium text-muted-foreground"
          >
            <Compass className="size-3.5 text-primary" />
            Seu próximo feriadão começa aqui
          </motion.div>

          <motion.h1
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.05 }}
            className="text-balance font-display text-4xl font-semibold tracking-tight sm:text-6xl"
          >
            Transforme dias de <span className="text-primary">folga</span> em viagens{' '}
            <span className="text-primary">inesquecíveis</span>
          </motion.h1>

          <motion.p
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.12 }}
            className="mx-auto mt-5 max-w-xl text-balance text-muted-foreground sm:text-lg"
          >
            Cruzamos feriados, clima, câmbio e custo de vida para encontrar — e explicar — o destino perfeito
            para o seu próximo feriadão.
          </motion.p>

          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="mt-8 flex flex-col items-center justify-center gap-3 sm:flex-row"
          >
            <Button asChild size="lg" className="w-full gap-2 rounded-full text-base shadow-lg shadow-primary/20 sm:w-auto">
              <Link to="/buscar">
                Planejar meu feriadão
                <ArrowRight className="size-4" />
              </Link>
            </Button>
            <Button asChild size="lg" variant="outline" className="w-full rounded-full text-base sm:w-auto">
              <Link to="/janelas">Ver melhores janelas do ano</Link>
            </Button>
          </motion.div>

          <ExampleSearchChips />

          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.35 }}
            className="mt-10 flex flex-wrap items-center justify-center gap-x-6 gap-y-2"
          >
            {signals.map((s) => (
              <span key={s.label} className="flex items-center gap-1.5 text-xs text-muted-foreground">
                <s.icon className="size-3.5" />
                {s.label}
              </span>
            ))}
          </motion.div>
        </div>
      </section>

      <section className="px-4 py-16 sm:py-24">
        <div className="mx-auto max-w-5xl">
          <motion.h2
            initial={{ opacity: 0, y: 16 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true, margin: '-80px' }}
            className="text-center font-display text-2xl font-semibold sm:text-3xl"
          >
            Como funciona
          </motion.h2>

          <div className="mt-10 grid gap-6 sm:grid-cols-3">
            {steps.map((step, i) => (
              <motion.div
                key={step.title}
                initial={{ opacity: 0, y: 24 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true, margin: '-60px' }}
                transition={{ delay: i * 0.1, ease: [0.22, 1, 0.36, 1] }}
                className="rounded-3xl border border-border bg-card p-6 text-center"
              >
                <span className="mx-auto mb-4 flex size-12 items-center justify-center rounded-full bg-primary/10 text-primary">
                  <step.icon className="size-5" />
                </span>
                <p className="font-display text-base font-semibold">{step.title}</p>
                <p className="mt-2 text-sm text-muted-foreground">{step.description}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {meta && (
        <section className="px-4 pb-20">
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="mx-auto max-w-3xl rounded-3xl border border-border bg-card p-6 text-center sm:p-8"
          >
            <p className="font-display text-lg font-semibold">Dados de fontes abertas e confiáveis</p>
            <div className="mt-4 flex flex-wrap justify-center gap-2 text-xs text-muted-foreground">
              {meta.dataSources.map((source) => (
                <span key={source.key} className="rounded-full border border-border px-3 py-1">
                  {source.label}
                </span>
              ))}
            </div>
          </motion.div>
        </section>
      )}
    </div>
  )
}
