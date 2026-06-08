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
   * Controladores REST sob `/api/...` (por exemplo `/api/countries/...`, `/api/holidays/...`, `/api/exchange/...`, `/api/travel/...`).
4. **Integração via terminal (Spring Shell):**
   * Comandos registrados em `PlanejadorShellCommands`.
   * O comportamento do shell é configurado em `src/main/resources/application.properties` e, para o modo interativo explícito, em `application-shell.properties` (perfil `shell`).
   * Nos testes, o shell fica desligado em `src/test/resources/application.properties`, para a suíte rodar como aplicação web/API sem prompt.
5. **Testes Automatizados:**
   * Testes de integração em **JUnit 5** (`CountryServiceIntegrationTest`, `HolidayServiceIntegrationTest`, `ExchangeServiceIntegrationTest`, `TravelServiceIntegrationTest`).

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
Nesta fase foi implementada a interface web completa do sistema. O usuário agora pode buscar um país pelo código ISO, visualizar informações geográficas, feriados futuros e a cotação da moeda local em BRL — tudo em uma interface visual responsiva e intuitiva.

#### O que foi desenvolvido:

1. **Interface Web com Thymeleaf:**
   * Adicionada a dependência `spring-boot-starter-thymeleaf` ao `pom.xml`.
   * Criada a página inicial (`templates/index.html`) com formulário de busca e atalhos para destinos populares.
   * Criada a página de resultado (`templates/resultado.html`) exibindo país, câmbio e lista de feriados próximos.
   * Estilização via CSS (`static/style.css`).

2. **Controller Web (`WebController`):**
   * Criado `web/WebController.java` com `@Controller` (não `@RestController`) para servir páginas HTML.
   * Rota `GET /` retorna a página inicial.
   * Rota `GET /viagem?codigo=XX` retorna a página de resultado com os dados do país.
   * Tratamento amigável de erros: país não encontrado exibe mensagem clara em vez de stack trace.

3. **Padrão de Projeto GoF — Facade:**
   * O `TravelService` implementa o padrão **Facade**: ele expõe uma única operação (`getOverviewByCountryCode`) que internamente coordena três subsistemas distintos (`CountryService`, `HolidayService` e `ExchangeService`), cada um comunicando com uma API externa diferente.
   * **Vantagem real:** o `WebController` não precisa conhecer nenhuma das três APIs, nem lidar com erros de câmbio quando o país usa BRL, nem saber como buscar feriados por código ISO. Toda essa complexidade fica escondida atrás do `TravelService`. Sem o Facade, o controller teria que fazer três chamadas, tratar exceções individualmente e conter lógica de negócio, o que nao faria sentido com o princípio da responsabilidade única e tornaria o controller frágil a mudanças nas APIs.

4. **Testes Automatizados:**
   * Criado `WebControllerTest.java` com testes de integração usando MockMvc.
   * Testa: rota `/` retorna template correto; busca com código válido retorna dados; busca com código inválido retorna mensagem de erro amigável; código vazio exibe erro; código é normalizado para maiúsculas.

#### Como usar a interface web
Rode o servidor:
```bash
./mvnw spring-boot:run
```
Acesse no navegador: [http://localhost:8080](http://localhost:8080)

* Digite um código de país (ex: `JP`, `FR`, `US`) no campo de busca e clique em **Buscar**.
* Ou clique em um dos atalhos de destinos populares.
* A página de resultado exibe: nome do país, região, capital, idioma, moeda, fuso horário, câmbio para BRL (quando aplicável) e lista de feriados futuros no ano.

---

### Fase 3
Finalização do projeto, com melhorias na interface, refinamento das funcionalidades já implementadas, organização da arquitetura do sistema e conclusão da versão final para apresentação.
- Ideia de implementação: aba a parte que mostra feriados nao oficiais dos países (como festa junina no Brasi), que possibilite que o usuario conheça outras datas que tenham eventos significativos nos países buscados. Combinando API's como Wikidata e Wikipedia (breve explicação do evento)

**Planejamento(Até 06/Julho):** Entrega Final
UX/UI: Deixar com o visual final, garantindo que o sistema seja responsivo, fluido e intuitivo para qualquer pessoa usar.

Reta Final: Revisar se todos os testes automatizados estão passando, finalizar a documentação explicando as vantagens desses padrões e preparar a demonstração para a nossa Apresentação Final. 

