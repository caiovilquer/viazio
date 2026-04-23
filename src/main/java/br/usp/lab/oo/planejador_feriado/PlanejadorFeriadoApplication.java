package br.usp.lab.oo.planejador_feriado;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

@SpringBootApplication
@CommandScan("br.usp.lab.oo.planejador_feriado.cli")
public class PlanejadorFeriadoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanejadorFeriadoApplication.class, args);
	}

}
