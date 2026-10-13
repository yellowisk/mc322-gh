package view;

import domain.entities.demand.Demand;
import domain.entities.productionmanager.ProductionManager;
import domain.interfaces.ProductionStrategy;
import domain.strategies.Ferrari;
import domain.strategies.FilaDoCaixa;
import domain.strategies.Joaozinho;

import java.util.List;

/**
 * Submenu de estratégia de produção. Mostra a próxima demanda que seria fabricada e deixa trocar
 * a estratégia atual entre as três disponíveis
 *
 * @author Heitor Almeida
 */
public class StrategyMenu extends Submenu {
    /** Estratégias disponíveis, na ordem do menu. */
    private final List<ProductionStrategy> estrategias = List.of(
            new FilaDoCaixa(),
            new Ferrari(),
            new Joaozinho()
    );

    /**
     * Cria o submenu c/ a cor laranja
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção
     */
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

    /**
     * Estratégia usada quando o simulador começa
     *
     * @return a primeira da lista: {@link domain.strategies.FilaDoCaixa}
     */
    public ProductionStrategy estrategiaPadrao() {
        return estrategias.getFirst();
    }

    /**
     * Loop do submenu. A estratégia ativa aparece marcada c/ ●, e a prévia da próxima demanda atualiza a cada troca
     */
    @Override
    public void exibir() {
        while (true) {
            imprimirCabecalho();
            imprimirPreviaProximaDemanda();

            for (int i = 0; i < estrategias.size(); i++) {
                ProductionStrategy estrategia = estrategias.get(i);
                boolean ativa = estrategia == gerenciadorProducao.getCurrentStrategy();
                System.out.printf(ConsolePrinter.CINZA + " %d." + ConsolePrinter.RESET + " %s %s " + ConsolePrinter.CINZA + "(%s)" + ConsolePrinter.RESET + "\n",
                        i + 1,
                        ativa ? ConsolePrinter.colorir(ConsolePrinter.VERDE, "●") : "○",
                        estrategia.getNomeEstrategia(),
                        estrategia.getRegraEstrategia());
            }
            System.out.println();
            ConsolePrinter.imprimirOpcaoVoltar();

            menu.printFooter();
            int escolha = menu.readInt("Qual estratégia deseja " + ConsolePrinter.colorir(cor, "USAR") + "? ");

            if (escolha == 0) break;

            ProductionStrategy escolhida;
            try {
                escolhida = estrategias.get(escolha - 1);
            } catch (IndexOutOfBoundsException e) {
                menu.setLastBuffer(ConsolePrinter.textoFalha("Dessa vez não é! Opção inválida!"));
                continue;
            }

            gerenciadorProducao.setStrategy(escolhida);
            menu.setLastBuffer(ConsolePrinter.textoOk("Estratégia alterada para %s!", escolhida.getNomeEstrategia()));
        }
    }

    /** Imprime qual demanda a estratégia atual fabricaria agora, ou avisa se nenhuma é elegível */
    private void imprimirPreviaProximaDemanda() {
        Demand proxima = gerenciadorProducao.peekNextDemand();
        String previa = (proxima == null)
                ? ConsolePrinter.colorir(ConsolePrinter.CINZA, "nenhuma demanda elegível")
                : ConsolePrinter.colorir(cor, proxima.getProductName() + " (" + proxima.getAmount() + " un)");
        System.out.println("   Próxima demanda a ser fabricada: " + previa + "\n");
    }
}
