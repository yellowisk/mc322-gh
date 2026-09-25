package view;

import domain.entities.machine.Machine;
import domain.entities.productionmanager.ProductionManager;

import java.util.List;

public class RepairMenu extends Submenu {

    public RepairMenu(Menu menu, ProductionManager productionManager) {
        super(menu, productionManager, ConsolePrinter.YELLOW);
    }

    @Override
    public String icon() {
        return ConsolePrinter.color(color, "⚒");
    }

    @Override
    public String label() {
        return "Reparar máquinas";
    }

    @Override
    public void show() {
        while (true) {
            printHeader();

            // Lista de Máquinas
            List<Machine> machines = productionManager.getMachines();
            for (int i = 0; i < machines.size(); i++) {
                Machine m = machines.get(i);
                if (m.precisaManutencao()) {
                    System.out.printf(" %d. %s " + ConsolePrinter.color(ConsolePrinter.RED, "(quebrada)") + "\n", i + 1, m.getName());
                } else {
                    System.out.printf(" %d. %s\n", i + 1, m.getName());
                }
            }
            System.out.println();
            ConsolePrinter.printBackOption();

            menu.printFooter();
            int option = menu.readInt("Qual máquina você deseja " + ConsolePrinter.color(color, "REPARAR") + "? ");

            if (option == 0) break;

            Machine chosenMachine;
            try {
                chosenMachine = machines.get(option - 1);
            } catch (IndexOutOfBoundsException e) {
                menu.setLastBuffer(ConsolePrinter.failText("Dessa vez não é! Máquina inválida!"));
                continue;
            }

            try {
                productionManager.repairMachine(chosenMachine);
                animateRepair(chosenMachine.getName());
                menu.setLastBuffer(ConsolePrinter.okText("%s reparada!", chosenMachine.getName().toUpperCase()));
            } catch (IllegalArgumentException | IllegalStateException e) {
                menu.setLastBuffer(ConsolePrinter.failText("%s", e.getMessage()));
            }
        }
    }

    /**
     * Imprime uma nova linha com uma animação de um bloco se mexendo
     * para simular o reparo em andamento.
     */
    private void animateRepair(String machineName) {
        System.out.println();
        int travelDistance = 5; // Distância que o bloco vai percorrer
        int loops = 3;

        for (int loop = 0; loop < loops; loop++) {
            for (int i = 0; i <= travelDistance; i++) {
                String spaces = " ".repeat(i);
                String trail = " ".repeat(travelDistance - i); // Serve para tampar os blocos das iterações anteriores

                System.out.print("\rReparando " + machineName + " [" + ConsolePrinter.color(color, spaces + "█" + trail) + "]");
                ConsolePrinter.pause(150);
            }
        }
    }
}
