package view;

import domain.entities.demand.Demand;
import domain.entities.productionmanager.ProductionManager;
import domain.interfaces.ProductionStrategy;
import domain.strategies.Ferrari;
import domain.strategies.FilaDoCaixa;
import domain.strategies.MaximoDeProdutos;

import java.util.List;

public class StrategyMenu {
    private final List<ProductionStrategy> strategies = List.of(
            new FilaDoCaixa(),
            new Ferrari(),
            new MaximoDeProdutos()
    );

    private final Menu menu;
    private final ProductionManager productionManager;

    public StrategyMenu(Menu menu, ProductionManager productionManager) {
        this.menu = menu;
        this.productionManager = productionManager;
    }

    public ProductionStrategy defaultStrategy() {
        return strategies.getFirst();
    }

    public void show() {
        while (true) {
            ConsolePrinter.clearScreen();
            ConsolePrinter.card(ConsolePrinter.ORANGE + "⇄" + ConsolePrinter.RESET + " ESTRATÉGIA DE PRODUÇÃO");
            System.out.println();

            printNextDemandPreview();

            for (int i = 0; i < strategies.size(); i++) {
                ProductionStrategy strategy = strategies.get(i);
                boolean active = strategy == productionManager.getCurrentStrategy();
                System.out.printf(ConsolePrinter.GRAY + " %d." + ConsolePrinter.RESET + " %s %s " + ConsolePrinter.GRAY + "(%s)" + ConsolePrinter.RESET + "\n",
                        i + 1,
                        active ? ConsolePrinter.GREEN + "●" + ConsolePrinter.RESET : "○",
                        strategy.getStrategyName(),
                        strategy.getStrategyRule());
            }
            System.out.println();
            ConsolePrinter.printBackOption();

            menu.printFooterBlock();
            int choice = menu.readInt("Qual estratégia deseja " + ConsolePrinter.ORANGE + "USAR" + ConsolePrinter.RESET + "? ");

            if (choice == 0) break;

            if (choice > 0 && choice <= strategies.size()) {
                ProductionStrategy chosen = strategies.get(choice - 1);
                productionManager.setStrategy(chosen);
                menu.setLastBuffer(" [" + ConsolePrinter.GREEN + "OK" + ConsolePrinter.RESET + "] Estratégia alterada para "
                        + chosen.getStrategyName() + "!");
            } else {
                menu.setLastBuffer(ConsolePrinter.RED + "Dessa vez não é! Opção inválida!" + ConsolePrinter.RESET);
            }
        }
    }

    private void printNextDemandPreview() {
        Demand next = productionManager.peekNextDemand();
        String preview = (next == null)
                ? ConsolePrinter.GRAY + "nenhuma demanda elegível" + ConsolePrinter.RESET
                : ConsolePrinter.YELLOW + next.getProductName() + " (" + next.getAmount() + " un)" + ConsolePrinter.RESET;
        System.out.println("   Próxima demanda a ser fabricada: " + preview + "\n");
    }
}
