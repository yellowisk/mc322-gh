package view;

import domain.entities.demand.Demand;
import domain.entities.productionmanager.ProductionManager;
import domain.exceptions.InvalidDemandAmountException;
import domain.exceptions.InvalidOptionException;

/**
 * Submenu de atualização de demandas. Escolhe uma demanda e define a nova quantidade, que reseta
 * o progresso dela e dá uma nova ordem de chegada
 *
 * @author Heitor Almeida
 */
public class UpdateDemandMenu extends Submenu {

    /**
     * Cria o submenu c/ a cor amarela
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção
     */
    public UpdateDemandMenu(Menu menu, ProductionManager gerenciadorProducao) {
        super(menu, gerenciadorProducao, ConsolePrinter.AMARELO);
    }

    @Override
    public String icone() {
        return ConsolePrinter.colorir(cor, "⟳");
    }

    @Override
    public String rotulo() {
        return "Atualizar demandas";
    }

    /**
     * Loop do submenu. Quantidade negativa ou opção inválida voltam pra lista c/ a mensagem no rodapé
     */
    @Override
    public void exibir() {
        while (true) {
            imprimirCabecalho();
            ConsolePrinter.listarDemandas(gerenciadorProducao);
            ConsolePrinter.imprimirOpcaoVoltar();

            menu.printFooter();
            int opcao = menu.readInt("Qual demanda deseja " + ConsolePrinter.colorir(cor, "ATUALIZAR") + "? ");

            if (opcao == 0) break;

            try {
                Menu.validarOpcao(opcao, gerenciadorProducao.getDemandas().size(), "Dessa vez não é! Opção inválida!");
            } catch (InvalidOptionException e) {
                menu.setLastBuffer(ConsolePrinter.textoFalha("%s", e.getMessage()));
                continue;
            }
            Demand demandaEscolhida = gerenciadorProducao.getDemandas().get(opcao - 1);

            int novoValor = menu.readInt("Digite o novo valor da demanda: ");
            try {
                gerenciadorProducao.atualizarDemanda(demandaEscolhida, novoValor);
                menu.setLastBuffer(ConsolePrinter.textoOk("Demanda de %s atualizada para %d!", demandaEscolhida.getProductName(), novoValor));
            } catch (InvalidDemandAmountException e) {
                menu.setLastBuffer(ConsolePrinter.textoFalha("Dessa vez não é! %s", e.getMessage()));
            }
        }
    }
}
