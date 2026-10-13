package view;

import domain.entities.demand.Demand;
import domain.entities.productionmanager.ProductionManager;
import domain.exceptions.InsufficientBudgetException;
import domain.exceptions.MachineNeedsRepairException;

import java.util.List;

public class FabricateDemandMenu extends Submenu {

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

    @Override
    public void exibir() {
        while (true) {
            imprimirCabecalho();
            List<Demand> demands = gerenciadorProducao.getDemands();
            ConsolePrinter.listarDemandas(gerenciadorProducao);

            // The option right after the demands fabricates whatever the active strategy picks
            int nextByStrategy = demands.size() + 1;
            Demand next = gerenciadorProducao.peekNextDemand();
            String preview;
            if (next == null) {
                preview = ConsolePrinter.colorir(ConsolePrinter.CINZA, "nenhuma demanda elegível");
            } else {
                preview = next.getProductName() + " (" + next.getAmount() + " un)";
                if (!next.isViable(gerenciadorProducao.getBudget())) {
                    preview += " " + ConsolePrinter.colorir(ConsolePrinter.VERMELHO, "(orçamento insuficiente)");
                }
            }
            System.out.printf(ConsolePrinter.CINZA + " %d." + ConsolePrinter.RESET + " " + ConsolePrinter.colorir(cor, "▶")
                            + " Próxima pela estratégia " + ConsolePrinter.colorir(cor, "%s") + ": %s\n\n",
                    nextByStrategy, gerenciadorProducao.getCurrentStrategy().getNomeEstrategia(), preview);
            ConsolePrinter.imprimirOpcaoVoltar();

            menu.printFooter();
            int option = menu.readInt("Qual demanda deseja " + ConsolePrinter.colorir(cor, "FABRICAR") + "? ");

            if (option == 0) break;

            try {
                if (option == nextByStrategy) {
                    gerenciadorProducao.runNextProduction();
                } else {
                    Demand selectedDemand;
                    try {
                        selectedDemand = demands.get(option - 1);
                    } catch (IndexOutOfBoundsException e) {
                        menu.setLastBuffer(ConsolePrinter.textoFalha("Dessa vez não é! Opção inválida!"));
                        continue;
                    }
                    gerenciadorProducao.fabricateDemand(selectedDemand);
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
