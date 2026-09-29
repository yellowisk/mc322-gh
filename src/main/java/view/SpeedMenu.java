package view;

import domain.entities.productionmanager.ProductionManager;

public class SpeedMenu extends Submenu {
    private final ProductionSpeed[] speeds = ProductionSpeed.values();

    public SpeedMenu(Menu menu, ProductionManager productionManager) {
        super(menu, productionManager, ConsolePrinter.PURPLE);
    }

    @Override
    public String icon() {
        return ConsolePrinter.color(color, "⏩");
    }

    @Override
    public String label() {
        return "Velocidade da produção";
    }

    @Override
    public void show() {
        while (true) {
            printHeader();
            System.out.println(ConsolePrinter.color(ConsolePrinter.GRAY, " Deixa a animação da fabricação mais rápida (ou pula ela de vez)\n"));

            for (int i = 0; i < speeds.length; i++) {
                boolean active = speeds[i] == ConsolePrinter.getProductionSpeed();
                System.out.printf(ConsolePrinter.GRAY + " %d." + ConsolePrinter.RESET + " %s %s\n",
                        i + 1,
                        active ? ConsolePrinter.color(ConsolePrinter.GREEN, "●") : "○",
                        speeds[i].getDescription());
            }
            System.out.println();
            ConsolePrinter.printBackOption();

            menu.printFooter();
            int choice = menu.readInt("Qual velocidade deseja " + ConsolePrinter.color(color, "USAR") + "? ");

            if (choice == 0) break;

            ProductionSpeed chosen;
            try {
                chosen = speeds[choice - 1];
            } catch (IndexOutOfBoundsException e) {
                menu.setLastBuffer(ConsolePrinter.failText("Dessa vez não é! Opção inválida!"));
                continue;
            }

            ConsolePrinter.setProductionSpeed(chosen);
            menu.setLastBuffer(ConsolePrinter.okText("Eitcha, como é ligeiro! Velocidade da produção alterada para %s!", chosen.getDescription()));
        }
    }
}
