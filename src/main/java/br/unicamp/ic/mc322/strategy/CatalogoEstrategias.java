package br.unicamp.ic.mc322.strategy;

import java.util.List;
import java.util.Optional;

/**
 * Estratégias de produção disponíveis. Como as estratégias não guardam estado, cada uma tem uma
 * instância só p/ comparar a estratégia atual c/ {@code ==}
 *
 * @author Heitor Almeida
 */
public final class CatalogoEstrategias {
    /** Estratégias disponíveis, na ordem do menu. A primeira é a padrão */
    private static final List<EstrategiaProducao> ESTRATEGIAS = List.of(
            new FilaDoCaixa(),
            new Ferrari(),
            new Joaozinho()
    );

    /** Só tem métodos static, não é p/ instanciar */
    private CatalogoEstrategias() {
    }

    /**
     * Todas as estratégias, na ordem do menu
     *
     * @return lista imutável das estratégias
     */
    public static List<EstrategiaProducao> todas() {
        return ESTRATEGIAS;
    }

    /**
     * Estratégia usada quando uma fábrica nova começa
     *
     * @return {@link FilaDoCaixa}
     */
    public static EstrategiaProducao padrao() {
        return ESTRATEGIAS.getFirst();
    }

    /**
     * Procura uma estratégia pelo nome, ex.: p/ reconstruir a estratégia salva num arquivo
     *
     * @param nome nome da estratégia, ex.: "Fila do Caixa"
     * @return a estratégia, ou vazio se nenhuma tiver esse nome
     */
    public static Optional<EstrategiaProducao> buscarPorNome(String nome) {
        return ESTRATEGIAS.stream()
                .filter(e -> e.getNomeEstrategia().equalsIgnoreCase(nome))
                .findFirst();
    }
}
