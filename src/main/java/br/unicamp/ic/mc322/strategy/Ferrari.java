package br.unicamp.ic.mc322.strategy;

import br.unicamp.ic.mc322.model.demand.Demanda;

import java.util.Comparator;
import java.util.List;

/**
 * Estratégia da maior demanda: escolhe a demanda c/ mais unidades, mesmo que não caiba no orçamento.
 * <p>
 * O Ferrari é um influencer rico que uma vez visitou o mercadinho e comprou um monte de produto
 * pra ganhar um balão surpresa. Com certeza o tipo de pessoa que compra o máximo possível
 * (<a href="https://www.instagram.com/p/DcRinHop9NR/">o vídeo</a>)
 *
 * @author Heitor Almeida
 */
public class Ferrari implements EstrategiaProducao {

    /** Cria a estratégia Ferrari. Não guarda estado */
    public Ferrari() {
    }

    /**
     * Pega a demanda elegível c/ a maior quantidade. Ignora o orçamento
     *
     * @param demandas todas as demandas cadastradas
     * @param orcamentoDisponivel não é usado por essa estratégia
     * @return demanda c/ mais unidades, ou null se nenhuma for elegível
     */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        return selecionaveis(demandas).stream()
                .max(Comparator.comparingInt(Demanda::getQuantidade))
                .orElse(null);
    }

    @Override
    public String getNomeEstrategia() {
        return "Ferrari";
    }

    @Override
    public String getRegraEstrategia() {
        return "maior demanda";
    }
}
