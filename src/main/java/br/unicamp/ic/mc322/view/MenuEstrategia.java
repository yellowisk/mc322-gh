package br.unicamp.ic.mc322.view;

import br.unicamp.ic.mc322.model.demand.Demanda;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.exception.OpcaoInvalidaException;
import br.unicamp.ic.mc322.strategy.CatalogoEstrategias;
import br.unicamp.ic.mc322.strategy.EstrategiaProducao;

import java.util.List;

/**
 * Submenu de estratégia de produção. Mostra a próxima demanda que seria fabricada e deixa trocar
 * a estratégia atual entre as três disponíveis
 *
 * @author Heitor Almeida
 */
public class MenuEstrategia extends Submenu {
    /** Estratégias disponíveis, na ordem do menu. */
    private final List<EstrategiaProducao> estrategias = CatalogoEstrategias.todas();

    /**
     * Cria o submenu c/ a cor laranja
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção
     */
    public MenuEstrategia(Menu menu, GerenciadorProducao gerenciadorProducao) {
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
     * Loop do submenu. A estratégia ativa aparece marcada c/ ●, e a prévia da próxima demanda atualiza a cada troca
     */
    @Override
    public void exibir() {
        while (true) {
            imprimirCabecalho();
            imprimirPreviaProximaDemanda();

            for (int i = 0; i < estrategias.size(); i++) {
                EstrategiaProducao estrategia = estrategias.get(i);
                boolean ativa = estrategia == gerenciadorProducao.getEstrategiaAtual();
                System.out.printf(ConsolePrinter.CINZA + " %d." + ConsolePrinter.RESET + " %s %s " + ConsolePrinter.CINZA + "(%s)" + ConsolePrinter.RESET + "\n",
                        i + 1,
                        ativa ? ConsolePrinter.colorir(ConsolePrinter.VERDE, "●") : "○",
                        estrategia.getNomeEstrategia(),
                        estrategia.getRegraEstrategia());
            }
            System.out.println();
            ConsolePrinter.imprimirOpcaoVoltar();

            menu.imprimirRodape();
            int escolha = menu.lerInteiro("Qual estratégia deseja " + ConsolePrinter.colorir(cor, "USAR") + "? ");

            if (escolha == 0) break;
            try {
                Menu.validarOpcao(escolha, estrategias.size(), "Dessa vez não é! Opção inválida!");
            } catch (OpcaoInvalidaException e) {
                menu.setUltimaMensagem(ConsolePrinter.textoFalha("%s", e.getMessage()));
                continue;
            }
            EstrategiaProducao escolhida = estrategias.get(escolha - 1);

            gerenciadorProducao.setEstrategiaAtual(escolhida);
            menu.setUltimaMensagem(ConsolePrinter.textoOk("Estratégia alterada para %s!", escolhida.getNomeEstrategia()));
        }
    }

    /** Imprime qual demanda a estratégia atual fabricaria agora, ou avisa se nenhuma é elegível */
    private void imprimirPreviaProximaDemanda() {
        Demanda proxima = gerenciadorProducao.espiarProximaDemanda();
        String previa = (proxima == null)
                ? ConsolePrinter.colorir(ConsolePrinter.CINZA, "nenhuma demanda elegível")
                : ConsolePrinter.colorir(cor, proxima.getNomeProduto() + " (" + proxima.getQuantidade() + " un)");
        System.out.println("   Próxima demanda a ser fabricada: " + previa + "\n");
    }
}
