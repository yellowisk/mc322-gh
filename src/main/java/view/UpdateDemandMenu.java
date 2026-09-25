package view;

import domain.entities.demand.Demand;
import domain.entities.productionmanager.ProductionManager;

public class UpdateDemandMenu extends Submenu {

    public UpdateDemandMenu(Menu menu, ProductionManager productionManager) {
        super(menu, productionManager, ConsolePrinter.YELLOW);
    }

    @Override
    public String icon() {
        return ConsolePrinter.color(color, "⟳");
    }

    @Override
    public String label() {
        return "Atualizar demandas";
    }

    @Override
    public void show() {
        while (true) {
            printHeader();
            ConsolePrinter.listDemands(productionManager);
            ConsolePrinter.printBackOption();

            menu.printFooter();
            int option = menu.readInt("Qual demanda deseja " + ConsolePrinter.color(color, "ATUALIZAR") + "? ");

            if (option == 0) break;

            Demand selectedDemand;
            try {
                selectedDemand = productionManager.getDemands().get(option - 1);
            } catch (IndexOutOfBoundsException e) {
                menu.setLastBuffer(ConsolePrinter.failText("Dessa vez não é! Opção inválida!"));
                continue;
            }

            int newValue = menu.readInt("Digite o novo valor da demanda: ");
            try {
                productionManager.updateDemand(selectedDemand, newValue);
                menu.setLastBuffer(ConsolePrinter.okText("Demanda de %s atualizada para %d!", selectedDemand.getProductName(), newValue));
            } catch (IllegalArgumentException e) {
                menu.setLastBuffer(ConsolePrinter.failText("Dessa vez não é! %s", e.getMessage()));
            }
        }
    }
}
