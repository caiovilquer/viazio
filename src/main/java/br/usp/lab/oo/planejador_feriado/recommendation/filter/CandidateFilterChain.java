package br.usp.lab.oo.planejador_feriado.recommendation.filter;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Monta a cadeia de filtros (Chain of Responsibility) a partir dos filtros
 * registrados no Spring, já ordenados por {@code @Order}, e expõe um único ponto de
 * entrada para o motor. Encadear/desencadear filtros é só adicionar/remover um bean.
 */
@Component
public class CandidateFilterChain {

    private final CandidateFilter head;

    public CandidateFilterChain(List<CandidateFilter> filters) {
        this.head = link(filters);
    }

    private CandidateFilter link(List<CandidateFilter> filters) {
        if (filters.isEmpty()) {
            return null;
        }
        CandidateFilter current = filters.get(0);
        CandidateFilter cursor = current;
        for (int i = 1; i < filters.size(); i++) {
            cursor = cursor.linkWith(filters.get(i));
        }
        return current;
    }

    /** @return motivo da rejeição se algum filtro barrar o candidato, senão vazio. */
    public Optional<String> reject(FilterContext context) {
        if (head == null) {
            return Optional.empty();
        }
        return head.filter(context);
    }
}
