package br.usp.lab.oo.planejador_feriado.cli;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.exchange.service.ExchangeService;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.holiday.service.HolidayService;
import br.usp.lab.oo.planejador_feriado.travel.model.TravelOverview;
import br.usp.lab.oo.planejador_feriado.travel.service.TravelService;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Comandos Spring Shell (mesmos serviços que o REST). Ative o shell com o perfil {@code shell}
 * ou {@code spring.shell.interactive.enabled=true}. Exemplos: {@code pais-por-codigo --codigo BR},
 * {@code viagem --codigo BR}, {@code help}.
 */
@Command
@Component
public class PlanejadorShellCommands {

    private final CountryService countryService;
    private final HolidayService holidayService;
    private final ExchangeService exchangeService;
    private final TravelService travelService;

    public PlanejadorShellCommands(
            CountryService countryService,
            HolidayService holidayService,
            ExchangeService exchangeService,
            TravelService travelService) {
        this.countryService = countryService;
        this.holidayService = holidayService;
        this.exchangeService = exchangeService;
        this.travelService = travelService;
    }

    @Command(command = "pais-por-codigo", description = "Busca país pelo código ISO 3166-1 alpha-2")
    public String countryByCode(
            @Option(required = true, longNames = "codigo", description = "Ex.: BR, US") String codigo) {
        Country c = countryService.getCountryByCode(codigo.trim());
        return c.toString();
    }

    @Command(command = "pais-por-nome", description = "Busca país pelo nome comum em inglês")
    public String countryByName(
            @Option(required = true, longNames = "nome", description = "Ex.: brazil, germany") String nome) {
        Country c = countryService.getCountryByName(nome.trim());
        return c.toString();
    }

    @Command(command = "feriados", description = "Lista feriados públicos futuros no ano atual para o país")
    public String holidays(
            @Option(required = true, longNames = "codigo", description = "Código ISO, ex.: BR") String codigo) {
        Country c = countryService.getCountryByCode(codigo.trim());
        List<Holiday> list = holidayService.getUpcomingHolidays(c);
        if (list.isEmpty()) {
            return "(nenhum feriado futuro)";
        }
        StringBuilder sb = new StringBuilder();
        for (Holiday h : list) {
            sb.append(h.toString()).append(System.lineSeparator());
        }
        return sb.toString().trim();
    }

    @Command(command = "cambio", description = "Cotação: 1 unidade da moeda para BRL (AwesomeAPI)")
    public String exchange(
            @Option(required = true, longNames = "moeda", description = "Ex.: USD, EUR") String moeda) {
        Exchange ex = exchangeService.getExchangeRate(moeda.trim());
        return ex.toString();
    }

    @Command(command = "viagem", description = "Resumo: país + feriados futuros + câmbio quando não for BRL")
    public String travel(
            @Option(required = true, longNames = "codigo", description = "Código ISO, ex.: BR") String codigo) {
        TravelOverview o = travelService.getOverviewByCountryCode(codigo.trim());
        StringBuilder sb = new StringBuilder();
        sb.append("--- País ---").append(System.lineSeparator());
        sb.append(o.country().toString());
        sb.append(System.lineSeparator()).append("--- Próximos feriados ---").append(System.lineSeparator());
        List<Holiday> list = o.upcomingHolidays();
        if (list.isEmpty()) {
            sb.append("(nenhum)").append(System.lineSeparator());
        } else {
            for (Holiday h : list) {
                sb.append(h.toString()).append(System.lineSeparator());
            }
        }
        sb.append("--- Câmbio para BRL ---").append(System.lineSeparator());
        if (o.exchangeToBrl() == null) {
            sb.append("(não aplicável ou indisponível)").append(System.lineSeparator());
        } else {
            sb.append(o.exchangeToBrl().toString()).append(System.lineSeparator());
        }
        return sb.toString().trim();
    }
}
