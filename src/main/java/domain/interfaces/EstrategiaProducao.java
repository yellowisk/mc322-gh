package domain.interfaces;

import domain.entities.demand.Demanda;
import java.util.List;

/**
 * Padrão Strategy p/ escolher qual demanda fabricar a seguir. O GerenciadorProducao guarda a estratégia
 * atual, e o menu de estratégia deixa trocar entre elas
 *
 * @author Heitor Almeida
 */
public interface EstrategiaProducao {
    /**
     * Escolhe a próxima demanda a ser fabricada entre as elegíveis de {@link #selecionaveis(List)}
     *
     * @param demandas todas as demandas cadastradas
     * @param orcamentoDisponivel orçamento disponível, em R$. Nem toda estratégia usa
     * @return demanda escolhida, ou null se nenhuma for elegível
     */
    Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel);

    /**
     * Nome da estratégia
     *
     * @return nome que aparece nos menus, ex.: "Fila do Caixa"
     */
    String getNomeEstrategia();

    /**
     * Regra curta mostrada do lado do nome no menu
     *
     * @return regra, ex.: "ordem de chegada"
     */
    String getRegraEstrategia();

    /**
     * Mesmo filtro p/ as três estratégias: só entram demandas {@link domain.entities.demand.StatusDemanda#PENDENTE}
     * c/ quantidade maior que 0
     *
     * @param demandas todas as demandas cadastradas
     * @return demandas que podem ser escolhidas
     */
    default List<Demanda> selecionaveis(List<Demanda> demandas) {
        return demandas.stream()
                .filter(d -> d.getStatus().ehSelecionavel() && d.getQuantidade() > 0)
                .toList();
    }
}
