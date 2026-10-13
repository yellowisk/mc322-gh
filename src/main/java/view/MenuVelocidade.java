package view;

import domain.entities.productionmanager.GerenciadorProducao;
import domain.exceptions.OpcaoInvalidaException;

/**
 * Submenu da velocidade da animação da produção. A escolhida vale p/ o resto da sessão
 *
 * @author Heitor Almeida
 */
public class MenuVelocidade extends Submenu {
    /** Velocidades na ordem do enum */
    private final VelocidadeProducao[] velocidades = VelocidadeProducao.values();

    /**
     * Cria o submenu c/ a cor roxa
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção
     */
    public MenuVelocidade(Menu menu, GerenciadorProducao gerenciadorProducao) {
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

            menu.imprimirRodape();
            int escolha = menu.lerInteiro("Qual velocidade deseja " + ConsolePrinter.colorir(cor, "USAR") + "? ");

            if (escolha == 0) break;
            try {
                Menu.validarOpcao(escolha, velocidades.length, "Dessa vez não é! Opção inválida!");
            } catch (OpcaoInvalidaException e) {
                menu.setUltimaMensagem(ConsolePrinter.textoFalha("%s", e.getMessage()));
                continue;
            }
            VelocidadeProducao escolhida = velocidades[escolha - 1];

            ConsolePrinter.setVelocidadeProducao(escolhida);
            menu.setUltimaMensagem(ConsolePrinter.textoOk("Eitcha, como é ligeiro! Velocidade da produção alterada para %s!", escolhida.getDescricao()));
        }
    }
}
