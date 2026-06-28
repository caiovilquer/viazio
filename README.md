# Planejador de Feriadões e Viagens Curtas

## Integrantes

- Caio Vilquer Carvalho — @caiovilquer
- Caio Cidade Rodrigues — @caiocidade
- Lilian Thauane Laurindo — @lilianlau
- Pedro Lopes Paraíso — @PedroParaiso1

## Descrição do projeto

Este projeto é um sistema web de apoio ao planejamento de viagens curtas e feriadões. A aplicação combina calendários, países, clima, custo relativo, distância e conteúdo do destino para encontrar boas janelas e comparar opções de forma explicável.

A solução oferece interface Thymeleaf, API REST versionada e acesso por terminal. O motor informa notas separadas para janela e destino, confiança dos dados, premissas, pontos de atenção e candidatos descartados. Ele não se apresenta como plataforma de reservas e não trata estimativas como preços comerciais.

**Estado atual:** Java 21, Spring Boot 3.5, 191 testes automatizados sem rede, OpenAPI, métricas Prometheus, imagem Docker e pipeline GitLab CI. A visão técnica consolidada está em [docs/arquitetura.md](docs/arquitetura.md).

---

## Evolução do projeto

### Fase 1

Implementação inicial do backend e integração com as fontes escolhidas. Nessa etapa, o sistema passou a consultar feriados, informações básicas de países e taxas de câmbio, inicialmente sem interface web completa, apenas via terminal.

#### Relatório de Entrega (21/Abril)

Nessa primeira fase foi estabelecida uma arquitetura sólida utilizando **Java 21** e **Spring Boot**. A consulta aos dados pode ser feita pela **API REST** (navegador, `curl`, Swagger) e pelo **terminal**, via **Spring Shell**, usando os mesmos serviços de domínio. Os testes automatizados continuam garantindo os contratos internos e as integrações externas.

### O que foi desenvolvido:

1. **Modelagem de Domínio:** * Criação das entidades principais (`Country`, `Holiday` e `Exchange`) utilizando encapsulamento para garantir a imutabilidade dos dados essenciais.
2. **Integração com fontes de dados (Clients e DTOs):**
   - A camada usa `RestClient` do Spring para fontes dinâmicas e clients estáticos para catálogos embarcados.
   - Isolamos os dados externos utilizando o padrão DTO (Data Transfer Object) com `Records`, garantindo que o núcleo do sistema não seja afetado por mudanças nas APIs.
   - **Fontes integradas:**
     - _Catálogo embarcado mledoze/countries:_ dados demográficos e geográficos estáveis, sem dependência de rede.
     - _Nager.Date:_ Busca de feriados nacionais.
     - _AwesomeAPI:_ Busca de cotação de câmbio em tempo real.
3. **Serviços e API REST:**
   - `CountryService`, `HolidayService`, `ExchangeService` e `TravelService` (visão agregada: país, feriados futuros e câmbio para BRL quando fizer sentido).
   - Controladores REST sob `/api/v1/...` (por exemplo `/api/v1/countries/...`, `/api/v1/holidays/...`, `/api/v1/exchange/...`, `/api/v1/travel/...`). O prefixo de versão é aplicado automaticamente pelo `WebConfig` (Fase 3) a todo `@RestController`, sem precisar repeti-lo em cada `@RequestMapping`.
   - **Tratamento de erros centralizado (`@RestControllerAdvice`):** os serviços lançam exceções de domínio tipadas (`ResourceNotFoundException`, `ExternalApiException`) e o `GlobalExceptionHandler` as traduz em respostas HTTP consistentes — **404** (recurso inexistente), **502** (falha externa), **400** (entrada inválida) e **429** (rate limit) —, sempre com `ApiError` (`timestamp`, `status`, `error`, `code`, `message`, `path`, `traceId` e violações de campo quando aplicável).
4. **Integração via terminal (Spring Shell):**
   - Comandos registrados em `PlanejadorShellCommands`.
   - O comportamento do shell é configurado em `src/main/resources/application.yml` (desligado por padrão) e, para o modo interativo explícito, em `application-shell.yml` (perfil `shell`).
   - Nos testes, o shell fica desligado em `src/test/resources/application.yml`, para a suíte rodar como aplicação web/API sem prompt.
5. **Testes Automatizados:**
   - **Unitários com Mockito (sem rede):** os serviços de domínio são testados isolando suas portas via mocks — `CountryServiceTest` (mock de `CountryClient`), `HolidayServiceTest` (mock de `HolidayClient`), `ExchangeServiceTest` (mock de `ExchangeClient`) e `TravelServiceTest`. Cobrem mapeamento DTO→modelo, ordenação, deduplicação, janelas de data, casos de erro (lista vazia, falha de client, moeda inválida) e o atalho de BRL.
   - **Web/controllers com `@WebMvcTest` + MockMvc (sem rede):** `CountryControllerTest`, `HolidayControllerTest`, `ExchangeControllerTest`, `TravelControllerTest` e o `WebControllerTest` da interface web (Thymeleaf) — todos com os serviços mockados (`@MockitoBean`). Os testes cobrem o caminho feliz e o mapeamento de erros (404/502).
   - **CLI:** `PlanejadorShellCommandsTest` exercita todos os comandos do Spring Shell com serviços mockados.
   - **Integração com APIs reais (`@Tag("integration")`):** `CountryServiceIntegrationTest`, `HolidayServiceIntegrationTest`, `ExchangeServiceIntegrationTest`, `TravelServiceIntegrationTest` — executados apenas com `./mvnw test -Pintegration`.

#### Como usar o terminal (Spring Shell)

**Modo interativo** (prompt JLine + servidor web, tipicamente na porta 8080):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=shell
```

No prompt, use `help` para listar comandos e `quit` para sair. Exemplos:

| Comando           | Opções                   | Função                                                      |
| ----------------- | ------------------------ | ----------------------------------------------------------- |
| `pais-por-codigo` | `--codigo` (ex.: `BR`)   | Busca país pelo código ISO 3166-1 alpha-2                   |
| `pais-por-nome`   | `--nome` (ex.: `brazil`) | Busca país pelo nome (inglês, conforme a API)               |
| `feriados`        | `--codigo`               | Lista feriados públicos futuros no ano para o país          |
| `cambio`          | `--moeda` (ex.: `USD`)   | Cotação de 1 unidade da moeda para BRL                      |
| `viagem`          | `--codigo`               | Resumo: país, próximos feriados e câmbio quando não for BRL |

---

### Fase 2

A Fase 2 evoluiu em duas entregas dentro do mesmo escopo: primeiro a **interface web** com o padrão **Facade**; depois o **motor decisor de recomendação** com o padrão **Strategy**, que compara destinos em uma janela de datas e retorna ranking com score e justificativa.

#### Entrega 1 — Interface web e Facade (GoF)

1. **Padrão Facade (`TravelService`):**
   - O `TravelService` expõe operações que coordenam `CountryService`, `HolidayService` e `ExchangeService` — por código ISO (`getOverviewByCountryCode`), por nome/código com detecção automática (`getOverviewByQuery`) ou via API REST (`GET /api/v1/travel/{code}`).
   - **Vantagem:** o `WebController` (e o CLI) não precisam conhecer as três APIs externas, nem tratar falhas de câmbio quando o país usa BRL. Sem o Facade, cada camada repetiria orquestração e lógica de negócio.
   - Consumido por `GET /api/v1/travel/{code}`, `GET /viagem?destino=...` (ou `?codigo=...` legado) e o comando shell `viagem`.

2. **Interface Web com Thymeleaf:**
   - Dependência `spring-boot-starter-thymeleaf` no `pom.xml`.
   - Página inicial (`templates/index.html`) com formulário de busca e atalhos para destinos populares.
   - Página de resultado (`templates/resultado.html`) com país, câmbio e feriados próximos.
   - Estilização via `static/style.css`.

3. **Controller Web (`WebController`):**
   - `@Controller` servindo HTML em `GET /` e `GET /viagem?destino=...`.
   - A busca aceita **código ISO de duas letras** (ex.: `JP`) ou **nome em inglês** (ex.: `japan`); entradas de 2 letras são tratadas como código, demais como nome.
   - O parâmetro legado `?codigo=XX` continua funcionando (atalhos de destinos populares).
   - Tratamento amigável de erros (país não encontrado exibe mensagem clara, sem stack trace).

4. **Testes:** `WebControllerTest` com MockMvc (rota `/`, busca por código/nome, parâmetro legado, normalização).

#### Entrega 2 — Motor decisor e Strategy (GoF)

1. **Motor Decisor de Recomendação (`/api/v1/recommendations`):**
   - `TravelRecommendationEngine` introduziu a comparação de múltiplos destinos sem duplicar os serviços de domínio.
   - Entrada por lista ISO ou região, com janela de datas e limite de resultados.
   - Saída com ranking, detalhamento por critério e candidatos não avaliados.
   - Candidatos com falha vão para `skipped` sem derrubar o ranking.

2. **Padrão Strategy (`ScoringStrategy`):**
   - Regras de avaliação isoladas e plugáveis via `List<ScoringStrategy>` no engine.
   - Complementa o Facade: enquanto o `TravelService` agrega dados de **um** país, o motor compara **vários** destinos com regras extensíveis.

3. **Refatoração e testes (pirâmide de testes):**
   - `HolidayDeduplicator` extraído do `TravelService` (lógica reutilizada pelo engine).
   - **Unitários (base, sem rede):** `HolidayDeduplicatorTest`, `TravelServiceTest`, `LongWeekendDetectorTest`, testes das quatro strategies e `TravelRecommendationEngineTest` (Mockito).
   - **Integração enxuta (meio):** `RecommendationControllerTest` (`@WebMvcTest` + MockMvc).
   - **Smoke com APIs reais (topo, `@Tag("integration")`):** testes de serviço reduzidos + `RecommendationIntegrationTest`. Rodam com `./mvnw test -Pintegration`.
   - Padrão default `./mvnw test` executa só unitários e testes web sem rede.

#### Padrões GoF na Fase 2

| Padrão       | Classe(s)                          | Entrega | Papel                                              |
| ------------ | ---------------------------------- | ------- | -------------------------------------------------- |
| **Facade**   | `TravelService`                    | 1       | Agrega país + feriados + câmbio de um destino      |
| **Strategy** | `ScoringStrategy` + implementações | 2       | Regras de score plugáveis no motor de recomendação |

#### Como usar a API de recomendações

```bash
# Comparar destinos por código ISO
curl "http://localhost:8080/api/v1/recommendations?from=2026-06-01&to=2026-06-30&countries=JP,FR"

# Avaliar todos os países independentes da região e retornar os 5 melhores
curl "http://localhost:8080/api/v1/recommendations?from=2026-06-01&to=2026-06-30&region=Europe&limit=5"

# Com perfil de pesos (economico, clima-perfeito, aventura, cultural, equilibrado)
curl "http://localhost:8080/api/v1/recommendations?from=2026-06-01&to=2026-06-30&countries=JP,FR,AR&profile=economico"

# Ajuste fino de pesos por critério + exclusões
curl "http://localhost:8080/api/v1/recommendations?from=2026-06-01&to=2026-06-30&region=Americas&weights=weather:0.4,cost:0.3&exclude=US"

# Considerar feriados de uma subdivisão e coordenadas exatas de origem
curl "http://localhost:8080/api/v1/recommendations?from=2026-07-09&to=2026-07-12&countries=AR,CL&originCountry=BR&originSubdivision=BR-SP&originLatitude=-23.55&originLongitude=-46.63"

# Considerar cidade, grupo e teto para os gastos terrestres estimados
curl "http://localhost:8080/api/v1/recommendations?from=2026-09-04&to=2026-09-07&countries=AR,CL,PE&originCity=Brasilia&travelers=2&maxGroundBudget=4500"

# Melhores janelas de viagem (feriadões) num período, com destinos por janela
curl "http://localhost:8080/api/v1/recommendations/best-windows?from=2026-01-01&to=2026-12-31&minDays=4&countries=AR,CL,PT"

# Catálogos e limites para montar um frontend
curl "http://localhost:8080/api/v1/meta"

# Busca estruturada recomendada para aplicações web/mobile
curl -X POST "http://localhost:8080/api/v1/recommendations" \
  -H "Content-Type: application/json" \
  -d '{
    "from": "2026-09-04",
    "to": "2026-09-07",
    "countries": ["AR", "CL", "PE"],
    "profile": "economico",
    "origin": {
      "countryCode": "BR",
      "subdivisionCode": "BR-SP",
      "city": "São Paulo",
      "latitude": -23.5505,
      "longitude": -46.6333
    },
    "travelers": 2,
    "maxGroundBudgetBrl": 5000
  }'
```

#### Como usar a interface web

Rode o servidor:

```bash
./mvnw spring-boot:run
```

Acesse no navegador: [http://localhost:8080](http://localhost:8080)

**Consultar um destino (consulta individual):**

- Digite o **código ISO** (ex.: `JP`, `FR`) ou o **nome em inglês** (ex.: `japan`, `france`) no campo de busca e clique em **Buscar**.
- Ou clique em um dos atalhos de destinos populares (usam código ISO por trás).
- A página de resultado exibe: nome do país, região, capital, idioma, moeda, fuso horário, câmbio para BRL (quando aplicável) e lista de feriados futuros no ano.

**Comparar destinos (motor de recomendação):**

- Na seção **Comparar destinos**, informe a janela de datas (`from` / `to`, máximo 92 dias).
- Escolha o modo de candidatos:
  - **Por países:** códigos ISO separados por vírgula (ex: `JP,FR,US`).
  - **Por região:** selecione `Europe`, `Americas`, `Asia`, `Africa` ou `Oceania`.
- Opcionalmente, escolha perfil, cidade de origem, quantidade de viajantes, orçamento terrestre e **limite de resultados** (1–15, padrão 10).
- Clique em **Comparar destinos** para ver nota da janela, nota do destino, nota combinada, confiança, breakdown, capital de referência, esforço de deslocamento e estimativa terrestre.
- Use **Ver detalhes** em qualquer item do ranking para abrir a consulta individual daquele país (`/viagem?codigo=XX` ou `/viagem?destino=nome`).
- Destinos não avaliados aparecem discretamente na seção _Destinos não avaliados_ (campo `skipped`).

#### Como rodar o frontend (Viazio)

Com o backend já rodando em `http://localhost:8080` (passo anterior), instale as dependências e suba o servidor de desenvolvimento do frontend em outro terminal:

```bash
cd frontend
pnpm install
pnpm dev
```

Acesse no navegador: [http://localhost:5173](http://localhost:5173). O Vite faz proxy de `/api` para o backend e o CORS já está liberado para essa origem (`app.cors.allowed-origins`).

Outros comandos úteis:

```bash
pnpm build    # build de produção (tsc + vite build)
pnpm preview  # serve o build de produção localmente
pnpm lint     # lint (oxlint)
```

#### Como rodar os testes

```bash
# Unitários + web (sem rede) — padrão
./mvnw test

# Incluir smoke tests com APIs externas
./mvnw test -Pintegration
```

#### Como executar em contêiner

```bash
docker build -t planejador-feriado .
docker run --rm -p 8080:8080 planejador-feriado
```

Endpoints operacionais:

- Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- Métricas Prometheus: [http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus)
- OpenAPI: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

### Fase 3

Consolidação do produto, com refinamento do motor decisor, contrato orientado à UX, integrações resilientes, observabilidade e entrega reproduzível.

#### Entrega 1 — Robustez de integração e Decorator (GoF)

As fontes externas gratuitas não têm SLA garantido e o motor de recomendação pode repetir consultas ao comparar muitos destinos. Esta entrega controla latência, concorrência e indisponibilidade sem acoplar essas preocupações aos serviços de domínio.

1. **Padrão Decorator (`Caching*Client`):**
   - `CountryClient`, `HolidayClient`, `ExchangeClient`, `WeatherClient`, `WorldBankIndicatorClient` e `WikipediaClient` definem as portas de dados consumidas pelos serviços.
   - `StaticCountryClient`, `NagerDateClient`, `AwesomeApiClient`, `OpenMeteoClient`, `WorldBankClient` e `WikipediaRestClient` são as implementações concretas.
   - Os `Caching*Client` decoram essas implementações com cache Caffeine, interceptando as mesmas chamadas sem alterar sua interface.
   - **Vantagem:** evita chamadas externas redundantes dentro de uma mesma comparação (ex.: feriados da origem consultados uma única vez, não uma por candidato), sem acoplar a lógica de cache ao código de integração HTTP.
   - **TTL por volatilidade dos dados:** país 24h (quase nunca muda), feriados 12h (calendário do ano é estável), câmbio 5min (cotação varia rápido — o cache aqui só evita duplicar chamadas dentro da mesma requisição de comparação).
   - Os serviços (`CountryService`, `HolidayService`, `ExchangeService`) dependem da interface, não da implementação concreta; o Spring injeta automaticamente a versão decorada (`@Primary`) em produção.

2. **Resiliência (Resilience4j) e timeouts centralizados:**
   - Cada provedor possui estado próprio de `@Retry`, `@CircuitBreaker` e `@Bulkhead`: `holidayApi`, `exchangeApi`, `weatherApi`, `worldBankApi` e `wikipediaApi`. Uma indisponibilidade da Wikipédia, por exemplo, não abre o circuito do Banco Mundial.
   - Bulkheads limitam chamadas concorrentes por integração e evitam que o paralelismo do ranking pressione uma API gratuita além do necessário.
   - `RestClientFactory` centraliza a criação dos `RestClient` com timeout de conexão (3s) e leitura (5s) configuráveis via `app.external-apis.connect-timeout`/`read-timeout`, evitando que uma API externa lenta bloqueie a aplicação indefinidamente.

3. **Paralelização do motor de recomendação:**
   - `TravelRecommendationEngine` avaliava cada candidato sequencialmente; agora usa **virtual threads** (Java 21, `Executors.newVirtualThreadPerTaskExecutor()`) para avaliar todos os candidatos em paralelo, reduzindo o tempo total da comparação proporcionalmente ao número de destinos.
   - O tratamento de erro por candidato (skip com mensagem, sem derrubar o ranking) foi preservado integralmente.

4. **Testes:** `CachingCountryClientTest`, `CachingHolidayClientTest`, `CachingExchangeClientTest` (cache hit/miss por chave) e `RestClientFactoryTest`.

#### Entrega 2 — Segurança e API moderna

Como a API pode ser consumida por interfaces executadas em outro processo e o projeto não exige login, esta entrega endurece o contrato público por outras frentes: CORS restrito, limite de abuso, não vazamento de detalhes internos, rastreabilidade, observabilidade e versionamento formal.

1. **Versionamento (`/api/v1`):**
   - `WebConfig` (`configurePathMatch`) aplica o prefixo `/api/v1` a todo `@RestController` do pacote da aplicação automaticamente — os controllers continuam mapeando apenas o recurso (`/countries`, `/holidays`, etc.), sem repetir o prefixo. Isso permite evoluir para `/api/v2` no futuro sem reescrever cada controller.
   - O `WebController` (HTML) não é afetado, pois usa `@Controller`, não `@RestController`.

2. **CORS restrito (`app.cors.allowed-origins`):**
   - `WebConfig` libera apenas as origens configuradas em `application.yml` (por padrão, `http://localhost:5173`, o dev server do Vite) para `/api/v1/**`. Qualquer outra origem recebe `403` no preflight.

3. **Rate limiting (Bucket4j):**
   - `RateLimitFilter` (registrado via `FilterRegistrationBean` apenas para `/api/v1/*`) aplica um _token bucket_ por IP (`app.rate-limit.capacity`/`refill-per-minute`, padrão 60 req/min). Ao exceder, responde `429 Too Many Requests` com `ApiError`, `Retry-After` e headers de limite.
   - Os buckets usam Caffeine com tamanho máximo e expiração por inatividade, evitando crescimento ilimitado de memória.
   - `X-Forwarded-For` só é aceito quando `app.rate-limit.trust-forwarded-headers=true`; por padrão o endereço remoto é usado, impedindo spoofing direto. Requisições `OPTIONS` não consomem tokens.
   - Protege a aplicação de uso abusivo e, indiretamente, preserva a cota das APIs externas gratuitas que ela consome.

4. **Handler de erros sem vazamento + `traceId`:**
   - `RequestTraceFilter` aceita um `X-Request-Id` seguro ou gera UUID, adiciona `X-Trace-Id` à resposta e mantém o mesmo identificador no MDC e no `ApiError`.
   - Para exceções de domínio a mensagem segura continua sendo devolvida; para qualquer exceção **não mapeada**, o `GlobalExceptionHandler` não repassa `ex.getMessage()` ao cliente — devolve mensagem genérica e loga o stack trace correlacionado.

5. **Observabilidade (Spring Boot Actuator):**
   - `/actuator/health`, `/actuator/info` e `/actuator/prometheus` são expostos; endpoints sensíveis (`env`, `beans`, `heapdump`, etc.) permanecem desligados.
   - `show-details: never` evita vazar dependências no health; probes de liveness/readiness ficam habilitados.
   - Micrometer publica duração, sucesso/erro, candidatos avaliados e resultados devolvidos pelo motor, além das métricas automáticas de JVM, HTTP e Resilience4j.

6. **OpenAPI customizado:**
   - `OpenApiConfig` define título, descrição, versão (`v1`), contato e licença (MIT) exibidos em `/v3/api-docs` e na UI (`/swagger-ui.html`), em vez dos valores genéricos do springdoc.

7. **Testes:** rate limit, confiança de proxy, limite de buckets, preflight CORS, correlação de trace, headers de segurança, métricas, endpoints operacionais e isolamento das configurações Resilience4j.

#### Entrega 3 — Motor de decisão completo

O motor separa conceitos que não devem ser confundidos: oportunidade do calendário, adequação do destino e confiança dos dados. O resultado deixa explícito o que foi medido, o que está faltando e por que um destino ficou acima de outro.

1. **Três notas com responsabilidades distintas:**
   - `destinationScore`: média ponderada dos critérios próprios do destino.
   - `windowScore`: qualidade da janela no calendário de origem, incluindo dias livres, folgas necessárias e feriadões.
   - `tripScore`: combinação de 80% destino + 20% janela, reduzida quando a cobertura de dados é incompleta.
   - `DataQuality` expõe cobertura, confiança, quantidade de critérios disponíveis e lista de dados ausentes.

2. **Quatro critérios de destino (Strategy):**

   | Critério            | Strategy                         | Fonte                                          |
   | ------------------- | -------------------------------- | ---------------------------------------------- |
   | ☀️ Clima            | `WeatherStrategy`                | Open-Meteo Forecast ou climatologia de 10 anos |
   | 💰 Custo de vida    | `CostOfLivingStrategy`           | World Bank (PPP de consumo ÷ câmbio oficial)   |
   | ✈️ Distância        | `DistanceStrategy`               | great-circle (Haversine) a partir da origem    |
   | 🎊 Calendário local | `DestinationFestivitiesStrategy` | Nager.Date (feriados nacionais do destino)     |

   O câmbio nominal continua disponível na resposta para conversão e apresentação, mas não participa do ranking.

3. **Origem configurável e calendário correto:**
   - Origem padrão `BR`, com suporte a `originCountry`, `originSubdivision`, `originLatitude` e `originLongitude`.
   - Feriados regionais só entram quando a subdivisão ISO 3166-2 correspondente é informada; sem subdivisão, apenas feriados nacionais são considerados.
   - `TravelWindowEvaluator` calcula dias livres, dias úteis de folga e qualidade da janela sem contaminar a comparação entre destinos.

4. **Clima com semântica temporal correta:**
   - Janelas dentro dos próximos 16 dias usam previsão real.
   - Datas mais distantes usam os mesmos dias do calendário nos dez anos anteriores, incluindo média, variabilidade e probabilidade de chuva.
   - A resposta identifica `FORECAST` ou `CLIMATOLOGY`, período de referência e tamanho da amostra.

5. **Seleção regional honesta:**
   - O dataset identifica países independentes, membros da ONU e status ISO; territórios não entram automaticamente no ranking regional.
   - `limit` controla somente a quantidade devolvida. Todos os países independentes da região são avaliados antes do corte.
   - Listas explícitas aceitam até 50 países e regiões até 60 candidatos.

6. **Pesos configuráveis: perfis + ajuste fino:**
   - Pesos padrão e **perfis** nomeados (`economico`, `clima-perfeito`, `aventura`, `cultural`, `equilibrado`) ficam em `application.yml` (`app.recommendation`), via `@ConfigurationProperties` (`ScoringProperties`).
   - `WeightResolver` combina **pesos padrão → perfil → ajuste fino por critério** (`?weights=weather:0.4,cost:0.2`), normalizando para somar 1.
   - Endpoint: `?profile=economico` e/ou `?weights=...`.

7. **Filtros e explicabilidade:**
   - `CandidateFilter` mantém a cadeia extensível de restrições obrigatórias; `ExcludedCountriesFilter` processa exclusões explícitas antes do scoring.
   - Descartados aparecem em `skipped` com o motivo.
   - Cada recomendação inclui destaques, pontos de atenção, breakdown completo e resumo com confiança.

8. **Endpoint "melhores janelas" (`GET /api/v1/recommendations/best-windows`):**
   - Dado um período amplo, encontra feriadões e pontes no calendário da origem.
   - Cada janela informa score, dias totais, dias de ponte e dias de folga necessários; opcionalmente inclui os melhores destinos.

9. **Coleta paralela e custo controlado:**
   - Candidatos são avaliados em virtual threads.
   - Falhas de clima, custo ou câmbio degradam somente o dado correspondente.
   - Wikipédia e população são carregadas apenas para os finalistas após o ranking.

10. **Testes:** cobertura do ranking regional completo, irrelevância do câmbio nominal, penalização por baixa cobertura, previsão de curto prazo, climatologia de dez anos, feriados por subdivisão, janela independente do destino e validação HTTP.

> Nota de integração: durante o desenvolvimento, a API gratuita do **RestCountries v3.1 foi descontinuada** (passou a exigir chave) e o indicador único de nível de preços do Banco Mundial (`PA.NUS.PPPC.RF`) foi **arquivado**. Resolvido sem depender de chaves: (a) os dados de países passaram a vir de um **dataset estático embarcado** (`StaticCountryClient`, lendo `resources/data/countries.json`, derivado do projeto MIT mledoze/countries) — países quase não mudam, então a app fica independente de rede para essa informação; (b) o custo de vida é **calculado** como PPP de consumo (`PA.NUS.PRVT.PP`) ÷ câmbio oficial (`PA.NUS.FCRF`) no ano mais recente comum. As demais integrações (Nager.Date, AwesomeAPI, Open-Meteo) seguem ao vivo.

#### Entrega 4 — Enriquecimento e Decorator (GoF)

Cada recomendação combina contexto editorial com informações práticas de decisão. A resposta identifica a cidade usada como referência, esforço aproximado de deslocamento, diferença de fuso e custo terrestre estimado, sem apresentar aproximações como preço comercial.

1. **Catálogo determinístico de destinos (`destination`):**
   - `destinations.json` contém 253 capitais ou sedes de governo de 241 países e territórios, com coordenadas, offsets UTC e indicação da capital principal. O arquivo foi derivado do Wikidata Query Service (CC0) em 23/06/2026.
   - `StaticDestinationCatalog` carrega o catálogo localmente; `DestinationCatalogService` resolve a capital principal e aceita busca de cidade sem diferenciar maiúsculas ou acentos (`Brasília`, `brasilia`).
   - Clima e distância são calculados pelas coordenadas da cidade de referência, em vez do centro geográfico do país. Quando uma cidade personalizada não está no catálogo, a API exige latitude e longitude.

2. **Viabilidade explícita (`TravelFeasibilityService`):**
   - `TravelEffort` informa distância Haversine, faixa estimada de duração do deslocamento, classificação `SHORT`/`MEDIUM`/`LONG` e diferença de fuso.
   - `GroundCostEstimate` projeta gastos terrestres em BRL por pessoa/dia usando a razão de nível de preços PPP entre destino e origem. A base configurável fica em `app.recommendation.estimates.baseline-daily-ground-cost-brl`.
   - A resposta informa anos dos dados, razão de preços, premissa, confiança `LOW`, dias e viajantes. `notIncluded` explicita passagens, bagagem/taxas, seguro e requisitos de entrada.
   - Não são fabricados preços de voo, hotel ou visto. Esses valores só podem ser incorporados como cotação real quando houver um provedor comercial confiável e configurado.

3. **Preferências práticas na API e no ranking:**
   - `originCity` melhora a referência geográfica da origem; `travelers` aceita 1–10 pessoas.
   - `maxGroundBudget` ativa `MaxGroundBudgetFilter`: destinos cuja estimativa terrestre excede o teto são enviados para `skipped` com motivo objetivo.
   - Os mesmos parâmetros são propagados para `best-windows`, mantendo a decisão consistente em cada janela sugerida.

4. **Padrão Facade (`DestinationProfileService`):**
   - Agrega população (Banco Mundial) e resumo/imagem (Wikipédia) em um único `DestinationProfile`. `TravelService` e `TravelRecommendationEngine` não conhecem as fontes individuais.
   - Cada sub-busca degrada isoladamente: falha da Wikipédia não remove população; falha demográfica não remove conteúdo editorial; falha total de enriquecimento não elimina o destino.

5. **Identidade e conteúdo do destino:**
   - `Country#getFlagEmoji()` deriva a bandeira do ISO alpha-2; `getDisplayName()` prioriza o nome em português do dataset estático.
   - `DemographicsService` consulta `SP.POP.TOTL` no Banco Mundial e usa o ano mais recente disponível.
   - `WikipediaRestClient` tenta o resumo em português e depois em inglês. `CachingWikipediaClient` aplica Decorator com TTL de 30 dias e cache negativo.
   - Todas as chamadas externas usam `User-Agent` identificável conforme a política da Wikimedia.

6. **UX da interface existente:**
   - O formulário permite informar cidade de origem, viajantes e orçamento terrestre.
   - Cada card mostra capital de referência, distância, duração aproximada, fuso, custo terrestre, premissa e itens não incluídos.
   - A consulta individual mantém bandeira, população, imagem, resumo e link para a Wikipédia.

7. **Testes:** cobertura de catálogo real, cidades com múltiplas capitais, normalização de acentos, estimativa PPP, ausência de estimativa sem dados, validações de viajantes/orçamento, filtro de teto terrestre, serialização JSON, propagação pelo controller web e preservação da viabilidade após o enriquecimento — **169 testes sem rede** no total.

#### Entrega 5 — Contrato de API orientado à UX

O frontend não precisa duplicar regras, listas ou limites do backend. A API oferece um contrato estruturado para buscas complexas e um catálogo autocontido para montar formulários, validar entradas e explicar as fontes ao usuário.

1. **Busca estruturada (`POST /api/v1/recommendations`):**
   - `RecommendationSearchRequest` substitui query strings extensas por JSON tipado, mantendo o `GET` existente para compatibilidade e testes manuais.
   - Aceita datas, países ou região, limite, perfil, pesos, exclusões, origem aninhada, viajantes e orçamento terrestre.
   - Bean Validation cobre obrigatoriedade, ISO alpha-2/ISO 3166-2, coordenadas, limites numéricos, chaves de critério e valores monetários.
   - O controller normaliza códigos, remove países duplicados e converte as chaves públicas de pesos para `Criterion`.

2. **Metadados para clientes (`GET /api/v1/meta`):**
   - Retorna versão da API e do catálogo, limites operacionais, regiões localizadas, critérios com rótulo/ícone/peso padrão, perfis com pesos efetivos e capacidades disponíveis.
   - Lista todos os países independentes elegíveis, nome em português, bandeira, região, sub-região, cidade padrão e capitais conhecidas.
   - Expõe as fontes e seu modo (`STATIC`, `LIVE`, `LIVE_CACHED`, `LIVE_AND_HISTORICAL`), incluindo a ausência explícita de preços comerciais ao vivo.
   - A resposta é imutável em memória e enviada com `Cache-Control: public, max-age=86400`.

3. **Erros consistentes e acionáveis:**
   - `GlobalExceptionHandler` converte também os erros produzidos pelo próprio Spring MVC; a API deixa de alternar entre `ApiError` e `ProblemDetail`.
   - `code` fornece identificadores estáveis como `VALIDATION_ERROR`, `INVALID_REQUEST`, `RESOURCE_NOT_FOUND`, `EXTERNAL_API_ERROR`, `RATE_LIMIT_EXCEEDED` e `INTERNAL_ERROR`.
   - Erros de corpo JSON incluem `violations` com campo e mensagem. Todo erro recebe `traceId`; erros internos continuam sem expor detalhes técnicos.
   - O rate limit usa o mesmo envelope e informa `Retry-After: 60`.

4. **OpenAPI como contrato verificável:**
   - `RecommendationSearchRequest` e `OriginInput` possuem schema, exemplos e restrições.
   - Controllers de recomendação e metadados têm tags e descrições orientadas ao caso de uso.
   - `OpenApiContractTest` confirma que `/v3/api-docs` publica GET/POST de recomendações, `/api/v1/meta` e o schema estruturado.

5. **Testes:** POST completo e normalização, violações por campo, seleção ambígua de candidatos, envelope de erros, rate limit, catálogo completo, cache de países, serviço/controller de metadados e contrato OpenAPI — **178 testes sem rede** no total.

#### Entrega 6 — Operação e entrega reproduzível

A aplicação pode ser executada e observada de forma previsível fora da máquina de desenvolvimento, com limites explícitos de recurso e validação automatizada a cada alteração.

1. **Segurança HTTP sem autenticação:**
   - `SecurityHeadersFilter` aplica `nosniff`, bloqueio de framing, política de referrer, Permissions Policy e Content Security Policy compatível com a interface Thymeleaf.
   - HSTS é enviado somente em conexões HTTPS, evitando comportamento incorreto no desenvolvimento HTTP local.

2. **Métricas do produto:**
   - `RecommendationMetrics` mede `travel.recommendation.duration`, requisições por resultado, candidatos avaliados e quantidade devolvida.
   - As tags têm cardinalidade controlada (`success`/`error`), sem país, mensagem de erro ou identificador de usuário.
   - Histogramas da duração ficam habilitados para cálculo de percentis no Prometheus.

3. **Comportamento de servidor:**
   - Shutdown gracioso com janela de 20 segundos.
   - Compressão para JSON, HTML, CSS e JavaScript acima de 1 KiB — relevante para o catálogo de `/api/v1/meta`.
   - Forwarded headers permanecem desativados no servidor e só são considerados pelo rate limit quando há configuração explícita de proxy confiável.

4. **Imagem de execução:**
   - `Dockerfile` multi-stage compila com JDK 21 e executa em JRE 21 Alpine.
   - O processo roda como usuário não-root, expõe a porta 8080 e possui healthcheck em `/actuator/health`.
   - `.dockerignore` remove repositório Git, artefatos locais, logs, IDE e documentação do contexto de build.

5. **GitLab CI:**
   - Estágio `test` executa `clean test` e publica relatórios JUnit.
   - Estágio `package` produz o JAR somente após os testes.
   - Estágio `container` faz o build integral do Dockerfile com Docker-in-Docker.
   - Cache Maven e artefatos com expiração de uma semana reduzem tempo sem tornar o pipeline dependente de arquivos locais.

6. **Testes:** headers, HSTS, trace, rate limit, configuração inválida, proxy confiável, cache limitado, métricas, Prometheus, health sem detalhes e anotações de resiliência por provedor — **191 testes sem rede** no total.

#### Padrões GoF na Fase 3

| Padrão                      | Classe(s)                                                                                                                                                    | Papel                                                                                             |
| --------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------- |
| **Decorator**               | `CachingCountryClient`, `CachingHolidayClient`, `CachingExchangeClient`, `CachingWeatherClient`, `CachingWorldBankIndicatorClient`, `CachingWikipediaClient` | Adiciona cache em memória (incl. negativo, no caso da Wikipédia) sem alterar a interface da fonte |
| **Strategy**                | `ScoringStrategy` + 4 implementações                                                                                                                         | Critérios de destino plugáveis, combinados por média ponderada                                    |
| **Chain of Responsibility** | `CandidateFilter`, `ExcludedCountriesFilter`, `MaxGroundBudgetFilter`                                                                                        | Descarta candidatos por restrições explícitas e teto terrestre                                    |
| **Facade**                  | `TravelService`                                                                                                                                              | Agrega país, feriados e câmbio para a visão individual                                            |
| **Facade**                  | `DestinationProfileService`                                                                                                                                  | Agrega população (Banco Mundial) e resumo/imagem da Wikipédia em um `DestinationProfile`          |

#### Entrega 7 — Frontend Viazio (React)

A camada de apresentação ganhou uma aplicação própria em `frontend/`, batizada **Viazio**, consumindo somente o contrato público (`/api/v1`) e sem duplicar regras de negócio do backend.

1. **Stack:** Vite + React 19 + TypeScript, Tailwind CSS v4 (tokens via CSS, sem `tailwind.config.js`), componentes shadcn/ui customizados, Framer Motion para microinterações, React Router v7 para rotas e TanStack Query para cache e sincronização com a API.
2. **Identidade visual:** tema único _dark-first_ (azul-marinho profundo, marfim como texto, dourado como acento raro e coral reservado a momentos de destaque), marca própria (monograma "V" + estrela) e tipografia editorial (Fraunces + Geist), evitando clichês visuais de turismo (avião, mala, pin de mapa).
3. **Telas:** Landing, Buscar (formulário com resumo ao vivo do plano), Resultados (ranking com reordenação instantânea no cliente ao ajustar pesos/perfil, sem nova chamada ao backend), Destino, Comparar (síntese em linguagem natural de quem vence cada critério, além das métricas lado a lado) e Janelas/Salvos (favoritos persistidos em `localStorage`, com o período de busca original preservado para permitir recomparações coerentes).
4. **Reaproveitamento do contrato:** o catálogo de `GET /api/v1/meta` alimenta filtros, perfis e pesos diretamente; um motor de pontuação no cliente espelha a fórmula de scoring do backend para reordenar resultados sem round-trip à API.
5. **Acessibilidade e responsividade:** contraste mínimo AA, foco visível por teclado, suporte a `prefers-reduced-motion` e layout adaptado a partir de telas de ~360px.

## Escopo e limites da versão atual

- O ranking apoia a decisão; ele não reserva nem vende viagens.
- A estimativa terrestre usa PPP e confiança baixa. Passagens, hospedagem cotada, seguro, visto, vacinas e regras de entrada não estão incluídos.
- Clima além do horizonte de 16 dias é climatologia de dez anos, não previsão.
- Distância e duração são aproximações geográficas e não consideram rotas, conexões ou disponibilidade de voos.
- Caches, circuit breakers e rate limits são locais à instância. Uma implantação horizontal só precisa de estado distribuído se exigir limites globais estritos.

Detalhes de componentes, fluxo, fórmulas, fontes, operação e extensões estão em [docs/arquitetura.md](docs/arquitetura.md).
