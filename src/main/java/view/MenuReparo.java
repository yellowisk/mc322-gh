package view;

import domain.entities.machine.Maquina;
import domain.entities.productionmanager.GerenciadorProducao;
import domain.exceptions.PlantaException;

import java.util.List;

/**
 * Submenu de reparo. Lista as máquinas c/ a saúde de cada uma e repara a escolhida, deixando
 * a saúde cheia de novo
 *
 * @author Heitor Almeida
 */
public class MenuReparo extends Submenu {

    /**
     * Cria o submenu c/ a cor amarela
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção
     */
    public MenuReparo(Menu menu, GerenciadorProducao gerenciadorProducao) {
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

    /**
     * Loop do submenu. Máquina inválida ou que não precisa de reparo volta pra lista c/ a mensagem no rodapé
     */
    @Override
    public void exibir() {
        while (true) {
            imprimirCabecalho();

            // Lista de Máquinas
            List<Maquina> maquinas = gerenciadorProducao.getMaquinas();
            for (int i = 0; i < maquinas.size(); i++) {
                Maquina m = maquinas.get(i);
                String corSaude = m.precisaManutencao() ? ConsolePrinter.VERMELHO : ConsolePrinter.VERDE;
                String saude = ConsolePrinter.colorir(corSaude, String.format("❤ %d/%d", m.getSaude(), m.getSaudeMaxima()));
                String quebrada = m.precisaManutencao() ? " " + ConsolePrinter.colorir(ConsolePrinter.VERMELHO, "(quebrada)") : "";

                System.out.printf(" %d. %-26s %s%s\n", i + 1, m.getNome(), saude, quebrada);
            }
            System.out.println();
            ConsolePrinter.imprimirOpcaoVoltar();
            menu.imprimirRodape();

            int opcao = menu.lerInteiro("Qual máquina você deseja " + ConsolePrinter.colorir(cor, "REPARAR") + "? ");

            if (opcao == 0) break;

            Maquina maquinaEscolhida;

            // Processo principal
            try {
                Menu.validarOpcao(opcao, maquinas.size(), "Dessa vez não é! Máquina inválida!");
                gerenciadorProducao.repararMaquina(opcao - 1);
                maquinaEscolhida = gerenciadorProducao.getMaquina(opcao - 1);
                animarReparo(maquinaEscolhida.getNome()); // Animação
                menu.setUltimaMensagem(ConsolePrinter.textoOk("%s reparada!", maquinaEscolhida.getNome().toUpperCase()));
            } catch (PlantaException e) {
                menu.setUltimaMensagem(ConsolePrinter.textoFalha("%s", e.getMessage()));
            }
        }
    }

    /**
     * Imprime uma nova linha com uma animação de um bloco se mexendo
     * para simular o reparo em andamento.
     *
     * @param nomeMaquina nome da máquina, aparece do lado da animação
     */
    private void animarReparo(String nomeMaquina) {
        System.out.println();
        int distancia = 5; // Distância que o bloco vai percorrer
        int voltas = 3;

        for (int volta = 0; volta < voltas; volta++) {
            for (int i = 0; i <= distancia; i++) {
                String espacos = " ".repeat(i);
                String rastro = " ".repeat(distancia - i); // Serve para tampar os blocos das iterações anteriores

                System.out.print("\rReparando " + nomeMaquina + " [" + ConsolePrinter.colorir(cor, espacos + "█" + rastro) + "]");
                ConsolePrinter.pausar(150);
            }
        }
    }
}
