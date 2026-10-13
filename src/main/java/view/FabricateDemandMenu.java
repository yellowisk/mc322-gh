package view;

import domain.entities.demand.Demand;
import domain.entities.productionmanager.ProductionManager;
import domain.exceptions.InsufficientBudgetException;
import domain.exceptions.MachineNeedsRepairException;

import java.util.List;

/**
 * Submenu de fabricação. Lista as demandas e deixa escolher uma p/ fabricar, ou fabricar a próxima
 * que a estratégia atual escolher
 *
 * @author Heitor Almeida
 */
public class FabricateDemandMenu extends Submenu {

    /**
     * Cria o submenu c/ a cor roxa
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção
     */
    public FabricateDemandMenu(Menu menu, ProductionManager gerenciadorProducao) {
        super(menu, gerenciadorProducao, ConsolePrinter.ROXO);
    }

    @Override
    public String icone() {
        return ConsolePrinter.colorir(cor, "⚙");
    }

    @Override
    public String rotulo() {
        return "Fabricar demandas";
    }

    /**
     * Loop do submenu. Depois de uma fabricação espera o ENTER e volta pro menu principal. Opção inválida,
     * orçamento insuficiente ou máquina quebrada voltam pra lista c/ a mensagem no rodapé
     */
    @Override
    public void exibir() {
        while (true) {
            imprimirCabecalho();
            List<Demand> demandas = gerenciadorProducao.getDemands();
            ConsolePrinter.listarDemandas(gerenciadorProducao);

            // A opção logo depois das demandas fabrica o que a estratégia atual escolher
            int proximaPelaEstrategia = demandas.size() + 1;
            Demand proxima = gerenciadorProducao.peekNextDemand();
            String previa;
            if (proxima == null) {
                previa = ConsolePrinter.colorir(ConsolePrinter.CINZA, "nenhuma demanda elegível");
            } else {
                previa = proxima.getProductName() + " (" + proxima.getAmount() + " un)";
                if (!proxima.isViable(gerenciadorProducao.getBudget())) {
                    previa += " " + ConsolePrinter.colorir(ConsolePrinter.VERMELHO, "(orçamento insuficiente)");
                }
            }
            System.out.printf(ConsolePrinter.CINZA + " %d." + ConsolePrinter.RESET + " " + ConsolePrinter.colorir(cor, "▶")
                            + " Próxima pela estratégia " + ConsolePrinter.colorir(cor, "%s") + ": %s\n\n",
                    proximaPelaEstrategia, gerenciadorProducao.getCurrentStrategy().getNomeEstrategia(), previa);
            ConsolePrinter.imprimirOpcaoVoltar();

            menu.printFooter();
            int opcao = menu.readInt("Qual demanda deseja " + ConsolePrinter.colorir(cor, "FABRICAR") + "? ");

            if (opcao == 0) break;

            try {
                if (opcao == proximaPelaEstrategia) {
                    gerenciadorProducao.runNextProduction();
                } else {
                    Demand demandaEscolhida;
                    try {
                        demandaEscolhida = demandas.get(opcao - 1);
                    } catch (IndexOutOfBoundsException e) {
                        menu.setLastBuffer(ConsolePrinter.textoFalha("Dessa vez não é! Opção inválida!"));
                        continue;
                    }
                    gerenciadorProducao.fabricateDemand(demandaEscolhida);
                }
            } catch (InsufficientBudgetException | MachineNeedsRepairException e) {
                menu.setLastBuffer(ConsolePrinter.textoFalha("%s", e.getMessage()));
                continue;
            }

            menu.waitEnter();
            break;
        }
    }
}
