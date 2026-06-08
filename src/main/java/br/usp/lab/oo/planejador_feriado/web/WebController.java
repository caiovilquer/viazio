package br.usp.lab.oo.planejador_feriado.web;

import br.usp.lab.oo.planejador_feriado.travel.model.TravelOverview;
import br.usp.lab.oo.planejador_feriado.travel.service.TravelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    private final TravelService travelService;

    public WebController(TravelService travelService) {
        this.travelService = travelService;
    }

    @GetMapping("/")
    public String index() {
        return "index"; // Renderiza o index.html
    }

  @GetMapping("/viagem")
    public String viagem(@RequestParam(name = "codigo", defaultValue = "") String codigo,
                         Model model) {
 
        if (codigo.isBlank()) {
            model.addAttribute("erro", "Informe o código do país (ex: BR, JP, FR).");
            return "resultado";
        }
 
        try {
            TravelOverview overview = travelService.getOverviewByCountryCode(codigo.trim());
            model.addAttribute("overview", overview);
        } catch (RuntimeException e) {
            model.addAttribute("erro", "País não encontrado para o código: \"" + codigo.trim().toUpperCase() + "\". Verifique se o código ISO está correto (ex: BR, JP, US, FR).");
        }
 
        model.addAttribute("codigoBuscado", codigo.trim().toUpperCase());
        return "resultado";
    }
}
