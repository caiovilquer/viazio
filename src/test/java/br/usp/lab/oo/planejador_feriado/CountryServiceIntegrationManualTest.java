// Teste Manual de Execucao do fluxo da aplicacao (Country)
// Sempre que for utilizá-lo, retire o comentário da linha 12

package br.usp.lab.oo.planejador_feriado;

import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication
public class CountryServiceIntegrationManualTest implements CommandLineRunner {

  private final CountryService service;

  public CountryServiceIntegrationManualTest(CountryService service) {
    this.service = service;
  }

  public static void main(String[] args) {
    SpringApplication.run(CountryServiceIntegrationManualTest.class, args);
  }

  @Override
  public void run(String... args) {
    var country = service.getCountryByName("brazil");
    System.out.println(country);
    country = service.getCountryByCode("BR");
    System.out.println(country);
  }
}
