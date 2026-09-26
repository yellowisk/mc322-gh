package view;

import domain.entities.demand.Demand;
import domain.entities.productionmanager.ProductionManager;
import domain.interfaces.ProductionStrategy;
import domain.strategies.Ferrari;
import domain.strategies.FilaDoCaixa;
import domain.strategies.Joaozinho;

import java.util.List;

public class StrategyMenu extends Submenu {
    private final List<ProductionStrategy> strategies = List.of(
            new FilaDoCaixa(),
            new Ferrari(),
            new Joaozinho()
    );

    public StrategyMenu(Menu menu, ProductionManager productionManager) {
        super(menu, productionManager, ConsolePrinter.ORANGE);
    }

    @Override
    public String icon() {
        return ConsolePrinter.color(color, "⇄");
    }

    @Override
    public String label() {
        return "Estratégia de produção";
    }

    public ProductionStrategy defaultStrategy() {
        return strategies.getFirst();
    }

    @Override
    public void show() {
        while (true) {
            printHeader();
            printNextDemandPreview();

            for (int i = 0; i < strategies.size(); i++) {
                ProductionStrategy strategy = strategies.get(i);
                boolean active = strategy == productionManager.getCurrentStrategy();
                System.out.printf(ConsolePrinter.GRAY + " %d." + ConsolePrinter.RESET + " %s %s " + ConsolePrinter.GRAY + "(%s)" + ConsolePrinter.RESET + "\n",
                        i + 1,
                        active ? ConsolePrinter.color(ConsolePrinter.GREEN, "●") : "○",
                        strategy.getStrategyName(),
                        strategy.getStrategyRule());
            }
            System.out.println();
            ConsolePrinter.printBackOption();

            menu.printFooter();
            int choice = menu.readInt("Qual estratégia deseja " + ConsolePrinter.color(color, "USAR") + "? ");

            if (choice == 0) break;

            ProductionStrategy chosen;
            try {
                chosen = strategies.get(choice - 1);
            } catch (IndexOutOfBoundsException e) {
                menu.setLastBuffer(ConsolePrinter.failText("Dessa vez não é! Opção inválida!"));
                continue;
            }

            productionManager.setStrategy(chosen);
            menu.setLastBuffer(ConsolePrinter.okText("Estratégia alterada para %s!", chosen.getStrategyName()));
        }
    }

    private void printNextDemandPreview() {
        Demand next = productionManager.peekNextDemand();
        String preview = (next == null)
                ? ConsolePrinter.color(ConsolePrinter.GRAY, "nenhuma demanda elegível")
                : ConsolePrinter.color(color, next.getProductName() + " (" + next.getAmount() + " un)");
        System.out.println("   Próxima demanda a ser fabricada: " + preview + "\n");
    }
}
