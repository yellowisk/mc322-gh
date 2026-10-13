package view;

import domain.entities.machine.Machine;
import domain.entities.productionmanager.ProductionManager;

import java.util.List;

public class RepairMenu extends Submenu {

    public RepairMenu(Menu menu, ProductionManager gerenciadorProducao) {
        super(menu, gerenciadorProducao, ConsolePrinter.AMARELO);
    }

    @Override
    public String icone() {
        return ConsolePrinter.colorir(cor, "⚒");
    }

    @Override
    public String rotulo() {
        return "Reparar máquinas";
    }

    @Override
    public void exibir() {
        while (true) {
            imprimirCabecalho();

            // Lista de Máquinas
            List<Machine> machines = gerenciadorProducao.getMachines();
            for (int i = 0; i < machines.size(); i++) {
                Machine m = machines.get(i);
                String healthColor = m.precisaManutencao() ? ConsolePrinter.VERMELHO : ConsolePrinter.VERDE;
                String health = ConsolePrinter.colorir(healthColor, String.format("❤ %d/%d", m.getSaude(), m.getSaudeMaxima()));
                String broken = m.precisaManutencao() ? " " + ConsolePrinter.colorir(ConsolePrinter.VERMELHO, "(quebrada)") : "";

                System.out.printf(" %d. %-26s %s%s\n", i + 1, m.getNome(), health, broken);
            }
            System.out.println();
            ConsolePrinter.imprimirOpcaoVoltar();
            menu.printFooter();

            int option = menu.readInt("Qual máquina você deseja " + ConsolePrinter.colorir(cor, "REPARAR") + "? ");

            if (option == 0) break;

            Machine chosenMachine;

            // Processo principal
            try {
                chosenMachine = machines.get(option - 1);
                gerenciadorProducao.repairMachine(chosenMachine);
                animateRepair(chosenMachine.getNome()); // Animação
                menu.setLastBuffer(ConsolePrinter.textoOk("%s reparada!", chosenMachine.getNome().toUpperCase()));
            } catch (IndexOutOfBoundsException e) { // Caso o índice seja inválido
                menu.setLastBuffer(ConsolePrinter.textoFalha("Dessa vez não é! Máquina inválida!"));
            } catch (IllegalArgumentException | IllegalStateException e) {
                menu.setLastBuffer(ConsolePrinter.textoFalha("%s", e.getMessage()));
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

                System.out.print("\rReparando " + machineName + " [" + ConsolePrinter.colorir(cor, spaces + "█" + trail) + "]");
                ConsolePrinter.pausar(150);
            }
        }
    }
}
