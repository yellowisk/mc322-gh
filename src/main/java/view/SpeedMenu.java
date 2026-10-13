package view;

import domain.entities.productionmanager.ProductionManager;

public class SpeedMenu extends Submenu {
    private final ProductionSpeed[] speeds = ProductionSpeed.values();

    public SpeedMenu(Menu menu, ProductionManager gerenciadorProducao) {
        super(menu, gerenciadorProducao, ConsolePrinter.ROXO);
    }

    @Override
    public String icone() {
        return ConsolePrinter.colorir(cor, "⭍");
    }

    @Override
    public String rotulo() {
        return "Velocidade da produção";
    }

    @Override
    public void exibir() {
        while (true) {
            imprimirCabecalho();
            System.out.println(ConsolePrinter.colorir(ConsolePrinter.CINZA, " Deixa a animação da fabricação mais rápida (ou pula ela de vez)\n"));

            for (int i = 0; i < speeds.length; i++) {
                boolean active = speeds[i] == ConsolePrinter.getVelocidadeProducao();
                System.out.printf(ConsolePrinter.CINZA + " %d." + ConsolePrinter.RESET + " %s %s\n",
                        i + 1,
                        active ? ConsolePrinter.colorir(ConsolePrinter.VERDE, "●") : "○",
                        speeds[i].getDescription());
            }
            System.out.println();
            ConsolePrinter.imprimirOpcaoVoltar();

            menu.printFooter();
            int choice = menu.readInt("Qual velocidade deseja " + ConsolePrinter.colorir(cor, "USAR") + "? ");

            if (choice == 0) break;

            ProductionSpeed chosen;
            try {
                chosen = speeds[choice - 1];
            } catch (IndexOutOfBoundsException e) {
                menu.setLastBuffer(ConsolePrinter.textoFalha("Dessa vez não é! Opção inválida!"));
                continue;
            }

            ConsolePrinter.setVelocidadeProducao(chosen);
            menu.setLastBuffer(ConsolePrinter.textoOk("Eitcha, como é ligeiro! Velocidade da produção alterada para %s!", chosen.getDescription()));
        }
    }
}
