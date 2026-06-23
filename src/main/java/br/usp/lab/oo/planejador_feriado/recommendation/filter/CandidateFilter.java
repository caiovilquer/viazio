package br.usp.lab.oo.planejador_feriado.recommendation.filter;

import java.util.Optional;

/**
 * Padrão Chain of Responsibility: cada filtro decide se rejeita o candidato ou
 * delega ao próximo elo. O motor pergunta apenas ao primeiro elo; a passagem pela
 * cadeia é encapsulada aqui, então incluir/remover um filtro não muda o motor.
 */
public abstract class CandidateFilter {

    private CandidateFilter next;

    /** Encadeia o próximo filtro e o devolve, permitindo {@code a.linkWith(b).linkWith(c)}. */
    public CandidateFilter linkWith(CandidateFilter next) {
        this.next = next;
        return next;
    }

    /**
     * Aplica este filtro e, se passar, delega ao próximo.
     *
     * @return motivo da rejeição, ou vazio se o candidato passou por toda a cadeia
     */
    public final Optional<String> filter(FilterContext context) {
        Optional<String> rejection = check(context);
        if (rejection.isPresent()) {
            return rejection;
        }
        return next != null ? next.filter(context) : Optional.empty();
    }

    /** @return motivo da rejeição se este filtro barra o candidato, senão vazio. */
    protected abstract Optional<String> check(FilterContext context);
}
