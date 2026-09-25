package view;

import domain.entities.demand.Demand;
import domain.entities.productionmanager.ProductionManager;
import domain.exceptions.InsufficientBudgetException;
import domain.exceptions.MachineNeedsRepairException;

import java.util.List;

public class FabricateDemandMenu extends Submenu {

    public FabricateDemandMenu(Menu menu, ProductionManager productionManager) {
        super(menu, productionManager, ConsolePrinter.PURPLE);
    }

    @Override
    public String icon() {
        return ConsolePrinter.color(color, "⚙");
    }

    @Override
    public String label() {
        return "Fabricar demandas";
    }

    @Override
    public void show() {
        while (true) {
            printHeader();
            List<Demand> demands = productionManager.getDemands();
            ConsolePrinter.listDemands(productionManager);

            // The option right after the demands fabricates whatever the active strategy picks
            int nextByStrategy = demands.size() + 1;
            Demand next = productionManager.peekNextDemand();
            String preview;
            if (next == null) {
                preview = ConsolePrinter.color(ConsolePrinter.GRAY, "nenhuma demanda elegível");
            } else {
                preview = next.getProductName() + " (" + next.getAmount() + " un)";
                if (!next.isViable(productionManager.getBudget())) {
                    preview += " " + ConsolePrinter.color(ConsolePrinter.RED, "(orçamento insuficiente)");
                }
            }
            System.out.printf(ConsolePrinter.GRAY + " %d." + ConsolePrinter.RESET + " " + ConsolePrinter.color(color, "▶")
                            + " Próxima pela estratégia " + ConsolePrinter.color(color, "%s") + ": %s\n\n",
                    nextByStrategy, productionManager.getCurrentStrategy().getStrategyName(), preview);
            ConsolePrinter.printBackOption();

            menu.printFooter();
            int option = menu.readInt("Qual demanda deseja " + ConsolePrinter.color(color, "FABRICAR") + "? ");

            if (option == 0) break;

            try {
                if (option == nextByStrategy) {
                    productionManager.runNextProduction();
                } else {
                    Demand selectedDemand;
                    try {
                        selectedDemand = demands.get(option - 1);
                    } catch (IndexOutOfBoundsException e) {
                        menu.setLastBuffer(ConsolePrinter.failText("Dessa vez não é! Opção inválida!"));
                        continue;
                    }
                    productionManager.fabricateDemand(selectedDemand);
                }
            } catch (InsufficientBudgetException | MachineNeedsRepairException e) {
                menu.setLastBuffer(ConsolePrinter.failText("%s", e.getMessage()));
                continue;
            }

            menu.waitEnter();
            break;
        }
    }
}
