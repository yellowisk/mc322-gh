package view;

import domain.entities.machine.Maquina;
import domain.entities.productionmanager.GerenciadorProducao;
import domain.exceptions.PlantaException;

import java.util.List;

/**
 * Submenu de reparo. Lista as máquinas c/ o estado de cada uma (nova, desgastada ou quebrada) e as partes
 * desgastadas, e repara a escolhida, deixando todas as partes novas. O reparo é por demanda: qualquer
 * máquina c/ desgaste pode ser reparada mesmo que não esteja quebrada
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

            imprimirMaquinas();

            ConsolePrinter.imprimirOpcaoVoltar();
            menu.imprimirRodape();

            int opcao = menu.lerInteiro("Qual máquina você deseja " + ConsolePrinter.colorir(cor, "REPARAR") + "? ");

            if (opcao == 0) break;

            Maquina maquinaEscolhida;

            // Processo principal
            try {
                Menu.validarOpcao(opcao, gerenciadorProducao.getMaquinas().size(), "Dessa vez não é! Máquina inválida!");
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
     * Lista as máquinas da linha c/ o estado colorido e, embaixo de cada uma, a árvore das partes desgastadas:
     * <pre>
     *  2. Máquina de Empacotamento   ! Desgastada
     *     ├─ Motor: Desgastado (78/100)
     *     └─ Seladora: lâmina 30% gasta
     * </pre>
     */
    private void imprimirMaquinas() {
        List<Maquina> maquinas = gerenciadorProducao.getMaquinas();
        for (int i = 0; i < maquinas.size(); i++) {
            Maquina m = maquinas.get(i);
            List<String> desgastadas = m.listarPartesDesgastadas();

            String estado;
            if (m.precisaManutencao()) {
                estado = ConsolePrinter.colorir(ConsolePrinter.VERMELHO, "✗ Quebrada");
            } else if (!desgastadas.isEmpty()) {
                estado = ConsolePrinter.colorir(ConsolePrinter.AMARELO, "! Desgastada");
            } else {
                estado = ConsolePrinter.colorir(ConsolePrinter.VERDE, "✓ Nova");
            }
            System.out.printf(" %d. %-26s %s\n", i + 1, m.getNome(), estado);

            for (int j = 0; j < desgastadas.size(); j++) {
                String conector = (j == desgastadas.size() - 1) ? "└─" : "├─";
                System.out.println("    " + ConsolePrinter.colorir(ConsolePrinter.CINZA, conector) + " " + desgastadas.get(j));
            }
        }
        System.out.println();
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
