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

    public StrategyMenu(Menu menu, ProductionManager gerenciadorProducao) {
        super(menu, gerenciadorProducao, ConsolePrinter.LARANJA);
    }

    @Override
    public String icone() {
        return ConsolePrinter.colorir(cor, "⇄");
    }

    @Override
    public String rotulo() {
        return "Estratégia de produção";
    }

    public ProductionStrategy defaultStrategy() {
        return strategies.getFirst();
    }

    @Override
    public void exibir() {
        while (true) {
            imprimirCabecalho();
            printNextDemandPreview();

            for (int i = 0; i < strategies.size(); i++) {
                ProductionStrategy strategy = strategies.get(i);
                boolean active = strategy == gerenciadorProducao.getCurrentStrategy();
                System.out.printf(ConsolePrinter.CINZA + " %d." + ConsolePrinter.RESET + " %s %s " + ConsolePrinter.CINZA + "(%s)" + ConsolePrinter.RESET + "\n",
                        i + 1,
                        active ? ConsolePrinter.colorir(ConsolePrinter.VERDE, "●") : "○",
                        strategy.getNomeEstrategia(),
                        strategy.getRegraEstrategia());
            }
            System.out.println();
            ConsolePrinter.imprimirOpcaoVoltar();

            menu.printFooter();
            int choice = menu.readInt("Qual estratégia deseja " + ConsolePrinter.colorir(cor, "USAR") + "? ");

            if (choice == 0) break;

            ProductionStrategy chosen;
            try {
                chosen = strategies.get(choice - 1);
            } catch (IndexOutOfBoundsException e) {
                menu.setLastBuffer(ConsolePrinter.textoFalha("Dessa vez não é! Opção inválida!"));
                continue;
            }

            gerenciadorProducao.setStrategy(chosen);
            menu.setLastBuffer(ConsolePrinter.textoOk("Estratégia alterada para %s!", chosen.getNomeEstrategia()));
        }
    }

    private void printNextDemandPreview() {
        Demand next = gerenciadorProducao.peekNextDemand();
        String preview = (next == null)
                ? ConsolePrinter.colorir(ConsolePrinter.CINZA, "nenhuma demanda elegível")
                : ConsolePrinter.colorir(cor, next.getProductName() + " (" + next.getAmount() + " un)");
        System.out.println("   Próxima demanda a ser fabricada: " + preview + "\n");
    }
}
