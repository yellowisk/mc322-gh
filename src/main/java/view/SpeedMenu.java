package view;

import domain.entities.productionmanager.GerenciadorProducao;
import domain.exceptions.InvalidOptionException;

/**
 * Submenu da velocidade da animação da produção. A escolhida vale p/ o resto da sessão
 *
 * @author Heitor Almeida
 */
public class SpeedMenu extends Submenu {
    /** Velocidades na ordem do enum */
    private final ProductionSpeed[] velocidades = ProductionSpeed.values();

    /**
     * Cria o submenu c/ a cor roxa
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção
     */
    public SpeedMenu(Menu menu, GerenciadorProducao gerenciadorProducao) {
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

    /**
     * Loop do submenu. Mostra a velocidade ativa marcada c/ ● e troca pela escolhida
     */
    @Override
    public void exibir() {
        while (true) {
            imprimirCabecalho();
            System.out.println(ConsolePrinter.colorir(ConsolePrinter.CINZA, " Deixa a animação da fabricação mais rápida (ou pula ela de vez)\n"));

            for (int i = 0; i < velocidades.length; i++) {
                boolean ativa = velocidades[i] == ConsolePrinter.getVelocidadeProducao();
                System.out.printf(ConsolePrinter.CINZA + " %d." + ConsolePrinter.RESET + " %s %s\n",
                        i + 1,
                        ativa ? ConsolePrinter.colorir(ConsolePrinter.VERDE, "●") : "○",
                        velocidades[i].getDescricao());
            }
            System.out.println();
            ConsolePrinter.imprimirOpcaoVoltar();

            menu.printFooter();
            int escolha = menu.readInt("Qual velocidade deseja " + ConsolePrinter.colorir(cor, "USAR") + "? ");

            if (escolha == 0) break;
            try {
                Menu.validarOpcao(escolha, velocidades.length, "Dessa vez não é! Opção inválida!");
            } catch (InvalidOptionException e) {
                menu.setLastBuffer(ConsolePrinter.textoFalha("%s", e.getMessage()));
                continue;
            }
            ProductionSpeed escolhida = velocidades[escolha - 1];

            ConsolePrinter.setVelocidadeProducao(escolhida);
            menu.setLastBuffer(ConsolePrinter.textoOk("Eitcha, como é ligeiro! Velocidade da produção alterada para %s!", escolhida.getDescricao()));
        }
    }
}
