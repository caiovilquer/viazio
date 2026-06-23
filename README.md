# Planejador de Feriadões e Viagens Curtas

## Integrantes
- Caio Vilquer Carvalho — @caiovilquer
- Caio Cidade Rodrigues — @caiocidade
- Lilian Thauane Laurindo — @lilianlau
- Pedro Lopes Paraíso — @PedroParaiso1

## Descrição do projeto
Este projeto tem como objetivo desenvolver um sistema web para ajudar usuários a planejar viagens curtas e feriadões. A aplicação irá reunir informações de APIs públicas sobre feriados, países e câmbio, permitindo visualizar oportunidades de viagem em períodos curtos e comparar destinos de forma simples e organizada.

A proposta é centralizar informações que normalmente estão espalhadas em diferentes sites, facilitando a consulta de datas, destinos e moedas em uma única plataforma.

---

## Cronograma preliminar

### Fase 1
Implementação inicial do backend e integração com as APIs escolhidas. Nessa etapa, o sistema deverá já ser capaz de consultar feriados, informações básicas de países e taxas de câmbio, inicialmente sem interface web completa, apenas via terminal.

#### Relatório de Entrega (21/Abril)
Nessa primeira fase foi estabelecido uma sólida arquitetura utilizando **Java 21** e **Spring Boot**. A consulta aos dados pode ser feita pela **API REST** (navegador, `curl`, Swagger) e pelo **terminal**, via **Spring Shell**, usando os mesmos serviços de domínio. Os testes automatizados continuam garantindo a integração com as APIs externas.

### O que foi desenvolvido:
1. **Modelagem de Domínio:** * Criação das entidades principais (`Country`, `Holiday` e `Exchange`) utilizando encapsulamento para garantir a imutabilidade dos dados essenciais.
2. **Integração com APIs Externas (Clientes e DTOs):**
   * Desenvolvemos a camada de clientes HTTP utilizando o `RestClient` do Spring.
   * Isolamos os dados externos utilizando o padrão DTO (Data Transfer Object) com `Records`, garantindo que o núcleo do sistema não seja afetado por mudanças nas APIs.
   * **APIs integradas:**
     * *RestCountries:* Busca de dados demográficos e geográficos.
     * *Nager.Date:* Busca de feriados nacionais.
     * *AwesomeAPI:* Busca de cotação de câmbio em tempo real.
3. **Serviços e API REST:**
   * `CountryService`, `HolidayService`, `ExchangeService` e `TravelService` (visão agregada: país, feriados futuros e câmbio para BRL quando fizer sentido).
   * Controladores REST sob `/api/v1/...` (por exemplo `/api/v1/countries/...`, `/api/v1/holidays/...`, `/api/v1/exchange/...`, `/api/v1/travel/...`). O prefixo de versão é aplicado automaticamente pelo `WebConfig` (Fase 3) a todo `@RestController`, sem precisar repeti-lo em cada `@RequestMapping`.
   * **Tratamento de erros centralizado (`@RestControllerAdvice`):** os serviços lançam exceções de domínio tipadas (`ResourceNotFoundException`, `ExternalApiException`) e o `GlobalExceptionHandler` as traduz em respostas HTTP consistentes — **404** (recurso inexistente: país/região/câmbio), **502** (falha de API externa) e **400** (entrada inválida, via `ResponseStatusException`/validação do Spring) —, sempre com um corpo JSON padronizado (`ApiError`: timestamp, status, error, message, path).
4. **Integração via terminal (Spring Shell):**
   * Comandos registrados em `PlanejadorShellCommands`.
   * O comportamento do shell é configurado em `src/main/resources/application.yml` (desligado por padrão) e, para o modo interativo explícito, em `application-shell.yml` (perfil `shell`).
   * Nos testes, o shell fica desligado em `src/test/resources/application.yml`, para a suíte rodar como aplicação web/API sem prompt.
5. **Testes Automatizados:**
   * **Unitários com Mockito (sem rede):** os serviços de domínio são testados isolando as dependências externas via mocks — `CountryServiceTest` (mock de `RestCountriesClient`), `HolidayServiceTest` (mock de `NagerDateClient`), `ExchangeServiceTest` (mock de `AwesomeApiClient`) e `TravelServiceTest`. Cobrem mapeamento DTO→modelo, ordenação, deduplicação, janelas de data, casos de erro (lista vazia, falha de cliente, moeda inválida) e o atalho de BRL.
   * **Web/controllers com `@WebMvcTest` + MockMvc (sem rede):** `CountryControllerTest`, `HolidayControllerTest`, `ExchangeControllerTest`, `TravelControllerTest` e o `WebControllerTest` da interface web (Thymeleaf) — todos com os serviços mockados (`@MockitoBean`). Os testes cobrem o caminho feliz e o mapeamento de erros (404/502).
   * **CLI:** `PlanejadorShellCommandsTest` exercita todos os comandos do Spring Shell com serviços mockados.
   * **Integração com APIs reais (`@Tag("integration")`):** `CountryServiceIntegrationTest`, `HolidayServiceIntegrationTest`, `ExchangeServiceIntegrationTest`, `TravelServiceIntegrationTest` — executados apenas com `./mvnw test -Pintegration`.

#### Como usar o terminal (Spring Shell)

**Modo interativo** (prompt JLine + servidor web, tipicamente na porta 8080):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=shell
```

No prompt, use `help` para listar comandos e `quit` para sair. Exemplos:

| Comando | Opções | Função |
|--------|--------|--------|
| `pais-por-codigo` | `--codigo` (ex.: `BR`) | Busca país pelo código ISO 3166-1 alpha-2 |
| `pais-por-nome` | `--nome` (ex.: `brazil`) | Busca país pelo nome (inglês, conforme a API) |
| `feriados` | `--codigo` | Lista feriados públicos futuros no ano para o país |
| `cambio` | `--moeda` (ex.: `USD`) | Cotação de 1 unidade da moeda para BRL |
| `viagem` | `--codigo` | Resumo: país, próximos feriados e câmbio quando não for BRL |

---

### Fase 2
A Fase 2 evoluiu em duas entregas dentro do mesmo escopo: primeiro a **interface web** com o padrão **Facade**; depois o **motor decisor de recomendação** com o padrão **Strategy**, que compara destinos em uma janela de datas e retorna ranking com score e justificativa.

#### Entrega 1 — Interface web e Facade (GoF)

1. **Padrão Facade (`TravelService`):**
   * O `TravelService` expõe operações que coordenam `CountryService`, `HolidayService` e `ExchangeService` — por código ISO (`getOverviewByCountryCode`), por nome/código com detecção automática (`getOverviewByQuery`) ou via API REST (`GET /api/v1/travel/{code}`).
   * **Vantagem:** o `WebController` (e o CLI) não precisam conhecer as três APIs externas, nem tratar falhas de câmbio quando o país usa BRL. Sem o Facade, cada camada repetiria orquestração e lógica de negócio.
   * Consumido por `GET /api/v1/travel/{code}`, `GET /viagem?destino=...` (ou `?codigo=...` legado) e o comando shell `viagem`.

2. **Interface Web com Thymeleaf:**
   * Dependência `spring-boot-starter-thymeleaf` no `pom.xml`.
   * Página inicial (`templates/index.html`) com formulário de busca e atalhos para destinos populares.
   * Página de resultado (`templates/resultado.html`) com país, câmbio e feriados próximos.
   * Estilização via `static/style.css`.

3. **Controller Web (`WebController`):**
   * `@Controller` servindo HTML em `GET /` e `GET /viagem?destino=...`.
   * A busca aceita **código ISO de duas letras** (ex.: `JP`) ou **nome em inglês** (ex.: `japan`); entradas de 2 letras são tratadas como código, demais como nome.
   * O parâmetro legado `?codigo=XX` continua funcionando (atalhos de destinos populares).
   * Tratamento amigável de erros (país não encontrado exibe mensagem clara, sem stack trace).

4. **Testes:** `WebControllerTest` com MockMvc (rota `/`, busca por código/nome, parâmetro legado, normalização).

#### Entrega 2 — Motor decisor e Strategy (GoF)

1. **Motor Decisor de Recomendação (`/api/v1/recommendations`):**
   * `TravelRecommendationEngine` orquestra `CountryService`, `HolidayService` e `ExchangeService` (sem reimplementar integrações).
   * Entrada: janela (`from`/`to`), lista ISO (`countries=JP,FR`) **ou** região (`region=Europe&limit=10`), opcional `maxRate` (câmbio máximo em BRL).
   * Saída: ranking com score 0–100, breakdown por critério e summary (ex.: `JP — score 68: feriadão de 4 dias, câmbio muito favorável`).
   * Detecção de feriadão/ponte no calendário **BR** (`LongWeekendDetector`) + bônus por feriados do **destino** na janela.
   * **Algoritmo de score (total 100 pts):**
     * Feriados/feriadões na janela — máx. **40 pts** (feriadão 4+ dias = 25; 3 dias = 18; feriado isolado = 8 cada, cap 30; bônus destino +5/feriado, cap 10).
     * Câmbio para BRL — máx. **35 pts** (≤1 BRL = 35; ≤3 = 25; ≤5 = 15; >5 = 8; acima de `maxRate` = 0).
     * Dias livres vs dias totais — máx. **25 pts** (fins de semana + feriados públicos BR na janela).
   * Candidatos com falha vão para `skipped` sem derrubar o ranking.

2. **Padrão Strategy (`ScoringStrategy`):**
   * `HolidayWindowStrategy`, `ExchangeRateStrategy` e `FreeDaysRatioStrategy` — cada regra de score isolada e plugável via `List<ScoringStrategy>` no engine.
   * Complementa o Facade: enquanto o `TravelService` agrega dados de **um** país, o motor compara **vários** destinos com regras extensíveis.

3. **Refatoração e testes (pirâmide de testes):**
   * `HolidayDeduplicator` extraído do `TravelService` (lógica reutilizada pelo engine).
   * **Unitários (base, sem rede):** `HolidayDeduplicatorTest`, `TravelServiceTest`, `LongWeekendDetectorTest`, testes das 3 strategies, `TravelRecommendationEngineTest` (Mockito).
   * **Integração enxuta (meio):** `RecommendationControllerTest` (`@WebMvcTest` + MockMvc).
   * **Smoke com APIs reais (topo, `@Tag("integration")`):** testes de serviço reduzidos + `RecommendationIntegrationTest`. Rodam com `./mvnw test -Pintegration`.
   * Padrão default `./mvnw test` executa só unitários e testes web sem rede.

#### Padrões GoF na Fase 2

| Padrão | Classe(s) | Entrega | Papel |
|--------|-----------|---------|-------|
| **Facade** | `TravelService` | 1 | Agrega país + feriados + câmbio de um destino |
| **Strategy** | `ScoringStrategy` + 3 implementações | 2 | Regras de score plugáveis no motor de recomendação |

#### Como usar a API de recomendações
```bash
# Comparar destinos por código ISO
curl "http://localhost:8080/api/v1/recommendations?from=2026-06-01&to=2026-06-30&countries=JP,FR"

# Comparar por região (máx. 10 países por padrão)
curl "http://localhost:8080/api/v1/recommendations?from=2026-06-01&to=2026-06-30&region=Europe&limit=5"

# Com perfil de pesos (economico, clima-perfeito, aventura, cultural, equilibrado)
curl "http://localhost:8080/api/v1/recommendations?from=2026-06-01&to=2026-06-30&countries=JP,FR,AR&profile=economico"

# Ajuste fino de pesos por critério + orçamento + exclusões
curl "http://localhost:8080/api/v1/recommendations?from=2026-06-01&to=2026-06-30&region=Americas&weights=clima:0.4,custo:0.3&maxRate=4.0&exclude=US"

# Melhores janelas de viagem (feriadões) num período, com destinos por janela
curl "http://localhost:8080/api/v1/recommendations/best-windows?from=2026-01-01&to=2026-12-31&minDays=4&countries=AR,CL,PT"
```

#### Como usar a interface web
Rode o servidor:
```bash
./mvnw spring-boot:run
```
Acesse no navegador: [http://localhost:8080](http://localhost:8080)

**Consultar um destino (consulta individual):**
* Digite o **código ISO** (ex.: `JP`, `FR`) ou o **nome em inglês** (ex.: `japan`, `france`) no campo de busca e clique em **Buscar**.
* Ou clique em um dos atalhos de destinos populares (usam código ISO por trás).
* A página de resultado exibe: nome do país, região, capital, idioma, moeda, fuso horário, câmbio para BRL (quando aplicável) e lista de feriados futuros no ano.

**Comparar destinos (motor de recomendação):**
* Na seção **Comparar destinos**, informe a janela de datas (`from` / `to`, máximo 92 dias).
* Escolha o modo de candidatos:
  * **Por países:** códigos ISO separados por vírgula (ex: `JP,FR,US`).
  * **Por região:** selecione `Europe`, `Americas`, `Asia`, `Africa` ou `Oceania`.
* Opcionalmente, defina **câmbio máximo (BRL)** e **limite de resultados** (1–15, padrão 10).
* Clique em **Comparar destinos** para ver o ranking com score (0–100), feriadões no Brasil na janela, breakdown por critério e resumo de cada destino.
* Use **Ver detalhes** em qualquer item do ranking para abrir a consulta individual daquele país (`/viagem?codigo=XX` ou `/viagem?destino=nome`).
* Destinos não avaliados aparecem discretamente na seção *Destinos não avaliados* (campo `skipped`).

#### Como rodar os testes
```bash
# Unitários + web (sem rede) — padrão
./mvnw test

# Incluir smoke tests com APIs externas
./mvnw test -Pintegration
```

---

### Fase 3
Finalização do projeto, com melhorias na interface, refinamento das funcionalidades já implementadas, organização da arquitetura do sistema e conclusão da versão final para apresentação.

#### Entrega 1 — Robustez de integração e Decorator (GoF)

As APIs externas gratuitas (RestCountries, Nager.Date, AwesomeAPI) não têm SLA garantido e o motor de recomendação pode repetir a mesma chamada várias vezes ao comparar N destinos (ex.: o calendário de feriados do Brasil é consultado uma vez por candidato). Esta entrega ataca latência e resiliência sem mudar o comportamento observável da API.

1. **Padrão Decorator (`Caching*Client`):**
   * `CountryClient`, `HolidayClient` e `ExchangeClient` passaram a ser interfaces; `RestCountriesClient`, `NagerDateClient` e `AwesomeApiClient` são as implementações reais (chamam a API via `RestClient`).
   * `CachingCountryClient`, `CachingHolidayClient` e `CachingExchangeClient` decoram essas implementações com cache em memória (Caffeine), interceptando as mesmas chamadas sem alterar a interface nem o client real.
   * **Vantagem:** evita chamadas externas redundantes dentro de uma mesma comparação (ex.: feriados do Brasil consultados uma única vez, não uma por candidato), sem acoplar a lógica de cache ao código de integração HTTP.
   * **TTL por volatilidade dos dados:** país 24h (quase nunca muda), feriados 12h (calendário do ano é estável), câmbio 5min (cotação varia rápido — o cache aqui só evita duplicar chamadas dentro da mesma requisição de comparação).
   * Os serviços (`CountryService`, `HolidayService`, `ExchangeService`) dependem da interface, não da implementação concreta; o Spring injeta automaticamente a versão decorada (`@Primary`) em produção.

2. **Resiliência (Resilience4j) e timeouts centralizados:**
   * `@Retry` + `@CircuitBreaker` (`resilience4j-spring-boot3`) em todos os métodos dos clients reais, configurados em `application.yml` (`resilience4j.retry.instances.externalApi`, `resilience4j.circuitbreaker.instances.externalApi`).
   * `RestClientFactory` centraliza a criação dos `RestClient` com timeout de conexão (3s) e leitura (5s) configuráveis via `app.external-apis.connect-timeout`/`read-timeout`, evitando que uma API externa lenta bloqueie a aplicação indefinidamente.

3. **Paralelização do motor de recomendação:**
   * `TravelRecommendationEngine` avaliava cada candidato sequencialmente; agora usa **virtual threads** (Java 21, `Executors.newVirtualThreadPerTaskExecutor()`) para avaliar todos os candidatos em paralelo, reduzindo o tempo total da comparação proporcionalmente ao número de destinos.
   * O tratamento de erro por candidato (skip com mensagem, sem derrubar o ranking) foi preservado integralmente.

4. **Testes:** `CachingCountryClientTest`, `CachingHolidayClientTest`, `CachingExchangeClientTest` (cache hit/miss por chave) e `RestClientFactoryTest`.

#### Entrega 2 — Segurança e API moderna

Como o frontend React (próxima etapa) roda em processo separado (Vite, `localhost:5173`) e o projeto optou por **não** ter login, esta entrega endurece a API por outras frentes: isolamento de origem (CORS), limite de abuso (rate limit), não vazamento de detalhes internos em erros, observabilidade básica (Actuator) e documentação/versionamento formais.

1. **Versionamento (`/api/v1`):**
   * `WebConfig` (`configurePathMatch`) aplica o prefixo `/api/v1` a todo `@RestController` do pacote da aplicação automaticamente — os controllers continuam mapeando apenas o recurso (`/countries`, `/holidays`, etc.), sem repetir o prefixo. Isso permite evoluir para `/api/v2` no futuro sem reescrever cada controller.
   * O `WebController` (HTML) não é afetado, pois usa `@Controller`, não `@RestController`.

2. **CORS restrito (`app.cors.allowed-origins`):**
   * `WebConfig` libera apenas as origens configuradas em `application.yml` (por padrão, `http://localhost:5173`, o dev server do Vite) para `/api/v1/**`. Qualquer outra origem recebe `403` no preflight.

3. **Rate limiting (Bucket4j):**
   * `RateLimitFilter` (registrado via `FilterRegistrationBean` apenas para `/api/v1/*`) aplica um *token bucket* por IP (`app.rate-limit.capacity`/`refill-per-minute`, padrão 60 req/min). Ao exceder, responde `429 Too Many Requests` com corpo JSON, sem tocar nos serviços de domínio.
   * Protege a aplicação de uso abusivo e, indiretamente, preserva a cota das APIs externas gratuitas que ela consome.

4. **Handler de erros sem vazamento + `traceId`:**
   * `ApiError` ganhou o campo `traceId`. Para exceções de domínio (`ResourceNotFoundException`, `ExternalApiException`) a mensagem já era segura e continua sendo devolvida; para qualquer exceção **não mapeada**, o `GlobalExceptionHandler` não repassa `ex.getMessage()` ao cliente — devolve uma mensagem genérica e loga o stack trace completo no servidor correlacionado pelo `traceId`, permitindo investigar sem expor detalhes internos (stack trace, nomes de classe, etc.) na resposta HTTP.

5. **Observabilidade (Spring Boot Actuator):**
   * Apenas `/actuator/health` e `/actuator/info` são expostos (`management.endpoints.web.exposure.include`); os demais (`env`, `beans`, `heapdump`, etc.) ficam desligados por padrão para não vazar configuração/segredos. `show-details: never` no health evita detalhar o status de cada dependência (ex.: APIs externas) para clientes não autenticados.

6. **OpenAPI customizado:**
   * `OpenApiConfig` define título, descrição, versão (`v1`), contato e licença (MIT) exibidos em `/v3/api-docs` e na UI (`/swagger-ui.html`), em vez dos valores genéricos do springdoc.

7. **Testes:** `RateLimitFilterTest` (consumo de tokens, bloqueio ao exceder, isolamento por IP); todos os testes de controller (`@WebMvcTest`) atualizados para os paths `/api/v1/...`.

#### Entrega 3 — Motor de decisão completo

O ponto fraco do motor anterior: ~55–65 dos 100 pontos vinham do calendário do Brasil (idêntico para todo destino), então o ranking era basicamente "moeda mais barata" — e câmbio nominal engana (1 JPY barato ≠ Japão barato). Esta entrega reconstrói o motor para comparar destinos de forma honesta, explicável e personalizável.

1. **Score normalizado + média ponderada:**
   * Cada `ScoringStrategy` devolve uma nota **0–100** comparável entre critérios (`ScoreEntry`), não mais pontos absolutos que se somam.
   * O score final é a **média ponderada** das notas dos critérios disponíveis. Quando um dado falta (ex.: clima/custo que a API não retornou), o critério fica `available=false` e é **excluído da média** (renormalizada) — em vez de penalizar o destino com zero.
   * O breakdown (`ScoredCriterion`) expõe, por critério: nota 0–100, peso aplicado, **contribuição** efetiva no score, rótulo, ícone e justificativa — tudo pronto para a UI renderizar sem de/para próprio.

2. **Seis critérios (Strategy):**

   | Critério | Strategy | Fonte |
   |----------|----------|-------|
   | 🎉 Feriados e pontes | `HolidayWindowStrategy` | Nager.Date (calendário BR) |
   | ☀️ Clima | `WeatherStrategy` | Open-Meteo Archive (climatologia) |
   | 💰 Custo de vida | `CostOfLivingStrategy` | World Bank (nível de preços PPP) |
   | 💱 Câmbio | `ExchangeRateStrategy` | AwesomeAPI |
   | ✈️ Distância | `DistanceStrategy` | great-circle (Haversine) a partir do Brasil |
   | 🎊 Festividades no destino | `DestinationFestivitiesStrategy` | Nager.Date (feriados do destino) |

   O critério de **custo (PPP)** corrige o engano do câmbio nominal: mede se as coisas são de fato mais baratas que no Brasil ("custo de vida ~70% do Brasil").

3. **Pesos configuráveis: perfis + ajuste fino:**
   * Pesos padrão e **perfis** nomeados (`economico`, `clima-perfeito`, `aventura`, `cultural`, `equilibrado`) ficam em `application.yml` (`app.recommendation`), via `@ConfigurationProperties` (`ScoringProperties`).
   * `WeightResolver` combina **pesos padrão → perfil → ajuste fino por critério** (`?weights=clima:0.4,custo:0.2`), normalizando para somar 1. A resposta **ecoa** o perfil e os pesos aplicados, para a UI exibir/ajustar.
   * Endpoint: `?profile=economico` e/ou `?weights=...`.

4. **Filtros de candidatos (Chain of Responsibility):**
   * `CandidateFilter` encadeia filtros que descartam candidatos **antes** do scoring: `ExcludedCountriesFilter` (exclusões do usuário) e `MaxExchangeRateFilter` (orçamento). Incluir/remover um filtro é só adicionar/remover um bean — o motor só conhece o primeiro elo.
   * Descartados aparecem em `skipped` com o motivo.

5. **Endpoint "melhores janelas" (`GET /api/v1/recommendations/best-windows`):**
   * Em vez de exigir que o usuário adivinhe as datas, dado um período amplo o sistema encontra os **melhores feriadões/pontes** do calendário BR (reaproveitando o `LongWeekendDetector`), ranqueados por qualidade do timing; opcionalmente rankeia os melhores destinos **dentro de cada janela** (reaproveitando o motor).

6. **Coleta paralela e degradação graciosa:**
   * As strategies são funções puras sobre um `RecommendationContext` rico; todo o I/O (país, feriados, câmbio, clima, custo, distância) acontece no motor, por candidato, em **virtual threads**. Falha de enriquecimento (clima/custo) vira critério indisponível, não derruba o candidato.

7. **Testes:** strategies novas (`WeatherStrategyTest`, `DistanceStrategyTest`, `CostOfLivingStrategyTest`, `DestinationFestivitiesStrategyTest`), `WeightResolverTest`, `CandidateFilterChainTest`, `GeoCalculatorTest`, `BestWindowsServiceTest`, além do `TravelRecommendationEngineTest` e dos controllers atualizados — **129 testes** no total.

> Nota de integração: a API gratuita do **RestCountries v3.1 foi descontinuada** (passou a exigir chave). O motor, o cache, os filtros e as demais integrações (Nager.Date, AwesomeAPI, Open-Meteo, World Bank) seguem funcionando; a fonte de dados de países será migrada (dataset estático embarcado ou API com chave) numa etapa dedicada.

#### Padrões GoF na Fase 3

| Padrão | Classe(s) | Papel |
|--------|-----------|-------|
| **Decorator** | `CachingCountryClient`, `CachingHolidayClient`, `CachingExchangeClient`, `CachingWeatherClient`, `CachingCostOfLivingClient` | Adiciona cache em memória sobre os clients HTTP reais sem alterar sua interface |
| **Strategy** | `ScoringStrategy` + 6 implementações | Critérios de score plugáveis, combinados por média ponderada |
| **Chain of Responsibility** | `CandidateFilter` + filtros | Descarta candidatos (orçamento, exclusões) antes do scoring |
| **Facade** | `TravelRecommendationEngine` | Orquestra os serviços de domínio e a coleta de dados externos |

**Funcionalidades planejadas:**
- Migração da fonte de dados de países (dataset estático embarcado ou API com chave).
- Filtro de visto na cadeia de candidatos.
- Descrição e imagem do destino via Wikipedia.
- Frontend React consumindo `/api/v1/recommendations`.

**Planejamento (até 06/Julho):** Entrega Final
UX/UI: Deixar com o visual final, garantindo que o sistema seja responsivo, fluido e intuitivo para qualquer pessoa usar.

Reta Final: Revisar se todos os testes automatizados estão passando, finalizar a documentação explicando as vantagens desses padrões e preparar a demonstração para a nossa Apresentação Final.

