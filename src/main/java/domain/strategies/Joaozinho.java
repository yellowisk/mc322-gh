package domain.strategies;

import domain.entities.demand.Demanda;
import domain.interfaces.ProductionStrategy;

import java.util.Comparator;
import java.util.List;

/**
 * Estratégia que seleciona a demanda com a maior quantidade
 * de produtos e que caiba no orçamento.
 *
 * Chamamos de Joãozinho porque ele sempre ganha a maior quantidade de copos
 * nos vídeos
 *
 * @author Glayson Oliveira
 */
public class Joaozinho implements ProductionStrategy {
    private final String nome = "Joãozinho";

    /**
     * Pega a demanda elegível c/ a maior quantidade entre as que cabem no orçamento.
     * Usa o custo estimado de cada demanda, que o ProductionManager atualiza antes de chamar
     *
     * @param demandas todas as demandas cadastradas
     * @param orcamentoDisponivel orçamento disponível, em R$
     * @return demanda viável c/ mais unidades, ou null se nenhuma couber no orçamento
     */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        // Entre as demandas que cabem no orçamento, pega a de maior quantidade de produtos
        return selecionaveis(demandas).stream()
                .filter(d -> d.ehViavel(orcamentoDisponivel))
                .max(Comparator.comparingInt(Demanda::getQuantidade))
                .orElse(null);
    }

    @Override
    public String getNomeEstrategia() {
        return this.nome;
    }

    @Override
    public String getRegraEstrategia() {
        return "máximo de produtos no orçamento";
    }
}