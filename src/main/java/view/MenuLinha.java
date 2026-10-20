package view;

import domain.entities.machine.Maquina;
import domain.entities.productionline.LinhaDeProducao;
import domain.entities.productionmanager.EtapaProducao;
import domain.entities.productionmanager.GerenciadorProducao;
import domain.exceptions.PlantaException;

import java.util.List;

/**
 * Submenu da linha de produção, o Agrupador das máquinas. Lista as máquinas na ordem das etapas, c/ o estado
 * (nova, desgastada ou quebrada), o custo e as partes desgastadas de cada uma, e repara uma máquina ou a linha
 * inteira. O reparo é por demanda: qualquer máquina c/ desgaste pode ser reparada, sem esperar ela quebrar.
 * O menu só fala c/ a linha, e a linha aciona as máquinas
 *
 * @author Heitor Almeida
 */
public class MenuLinha extends Submenu {
    /** Opção que repara a linha inteira */
    private static final int OPCAO_REPARAR_LINHA = 99;
    /** Linha de produção que o submenu controla */
    private final LinhaDeProducao linha;

    /**
     * Cria o submenu c/ a cor verde
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção
     * @param linha linha de produção da planta
     */
    public MenuLinha(Menu menu, GerenciadorProducao gerenciadorProducao, LinhaDeProducao linha) {
        super(menu, gerenciadorProducao, ConsolePrinter.VERDE);
        this.linha = linha;
    }

    @Override
    public String icone() {
        return ConsolePrinter.colorir(cor, "⇶");
    }

    @Override
    public String rotulo() {
        return "Linha de produção";
    }

    /**
     * Loop do submenu. Máquina inválida ou que não precisa de reparo volta pra lista c/ a mensagem no rodapé
     */
    @Override
    public void exibir() {
        while (true) {
            imprimirCabecalho();
            imprimirResumoLinha();
            imprimirMaquinas();

            System.out.printf(ConsolePrinter.CINZA + " %d." + ConsolePrinter.RESET + " Reparar a linha inteira\n", OPCAO_REPARAR_LINHA);
            ConsolePrinter.imprimirOpcaoVoltar();
            menu.imprimirRodape();

            int opcao = menu.lerInteiro("Qual máquina você deseja " + ConsolePrinter.colorir(cor, "REPARAR") + "? ");

            if (opcao == 0) break;

            try {
                if (opcao == OPCAO_REPARAR_LINHA) {
                    repararLinha();
                } else {
                    repararMaquina(opcao);
                }
            } catch (PlantaException e) {
                menu.setUltimaMensagem(ConsolePrinter.textoFalha("%s", e.getMessage()));
            }
        }
    }

    /** Uma linha c/ o nome da linha, quantas máquinas ela tem, quantas estão desgastadas ou quebradas e o custo */
    private void imprimirResumoLinha() {
        List<Maquina> maquinas = linha.getMaquinas();
        long quebradas = maquinas.stream().filter(Maquina::precisaManutencao).count();
        long desgastadas = maquinas.stream().filter(m -> !m.precisaManutencao() && m.temPartesDesgastadas()).count();
        String separador = ConsolePrinter.colorir(ConsolePrinter.CINZA, " | ");

        StringBuilder resumo = new StringBuilder(String.format(" %s%s%s%s%d máquinas",
                ConsolePrinter.NEGRITO, linha.getNome(), ConsolePrinter.RESET, ConsolePrinter.colorir(ConsolePrinter.CINZA, " — "), maquinas.size()));
        if (quebradas > 0) {
            resumo.append(separador).append(ConsolePrinter.colorir(ConsolePrinter.VERMELHO, "✗ " + quebradas + " quebrada(s)"));
        }
        if (desgastadas > 0) {
            resumo.append(separador).append(ConsolePrinter.colorir(ConsolePrinter.AMARELO, "! " + desgastadas + " desgastada(s)"));
        }
        resumo.append(separador).append("Custo: ")
                .append(ConsolePrinter.colorir(ConsolePrinter.AMARELO, String.format("R$ %.2f/un", linha.calcCustoOperacaoPorUnidade())));
        System.out.println(resumo + "\n");
    }

    /**
     * Lista as máquinas na ordem das etapas c/ o custo e o estado e, embaixo de cada uma, a árvore das partes
     * desgastadas, p/ o operador ver o que o reparo vai consertar:
     * <pre>
     *  2. Empacotamento  → Máquina de Empacotamento   R$ 0.50/un   ! Desgastada
     *     ├─ Motor: Desgastado (78/100)
     *     └─ Seladora: lâmina 30% gasta
     * </pre>
     */
    private void imprimirMaquinas() {
        for (EtapaProducao etapa : EtapaProducao.values()) {
            Maquina m = linha.getMaquinaDaEtapa(etapa);
            String nomeEtapa = etapa.getNome().substring(0, 1).toUpperCase() + etapa.getNome().substring(1);

            // Custo antes do estado: o estado tem largura variável, então fica no fim p/ não desalinhar as colunas
            System.out.printf(" %d. %-14s " + ConsolePrinter.colorir(ConsolePrinter.CINZA, "→") + " %-26s %s   %s\n",
                    etapa.getCodigo() + 1, nomeEtapa, m.getNome(),
                    ConsolePrinter.colorir(ConsolePrinter.AMARELO, String.format("R$ %.2f/un", m.getCustoOperacao())),
                    ConsolePrinter.estadoMaquina(m));

            List<String> desgastadas = m.listarPartesDesgastadas();
            for (int j = 0; j < desgastadas.size(); j++) {
                String conector = (j == desgastadas.size() - 1) ? "└─" : "├─";
                System.out.println("    " + ConsolePrinter.colorir(ConsolePrinter.CINZA, conector) + " " + desgastadas.get(j));
            }
        }
        System.out.println();
    }

    /**
     * Repara a máquina escolhida pelo número da lista
     *
     * @param opcao número da máquina na lista, começa em 1
     * @throws PlantaException se a opção for inválida ou a máquina estiver nova
     */
    private void repararMaquina(int opcao) {
        Menu.validarOpcao(opcao, linha.getMaquinas().size(), "Dessa vez não é! Máquina inválida!");
        linha.repararMaquina(opcao - 1);

        String nome = linha.getMaquina(opcao - 1).getNome();
        animarReparo(nome);
        menu.setUltimaMensagem(ConsolePrinter.textoOk("%s reparada!", nome.toUpperCase()));
    }

    /**
     * Repara todas as máquinas desgastadas da linha de uma vez
     *
     * @throws PlantaException se todas as máquinas estiverem novas
     */
    private void repararLinha() {
        int reparadas = linha.repararTodas();
        animarReparo(linha.getNome());
        menu.setUltimaMensagem(ConsolePrinter.textoOk("%s reparada! %d máquina(s) voltaram a ficar novas.",
                linha.getNome().toUpperCase(), reparadas));
    }

    /**
     * Imprime uma nova linha com uma animação de um bloco se mexendo
     * para simular o reparo em andamento.
     *
     * @param nome o que está sendo reparado, aparece do lado da animação
     */
    private void animarReparo(String nome) {
        System.out.println();
        int distancia = 5; // Distância que o bloco vai percorrer
        int voltas = 3;

        for (int volta = 0; volta < voltas; volta++) {
            for (int i = 0; i <= distancia; i++) {
                String espacos = " ".repeat(i);
                String rastro = " ".repeat(distancia - i); // Serve para tampar os blocos das iterações anteriores

                System.out.print("\rReparando " + nome + " [" + ConsolePrinter.colorir(cor, espacos + "█" + rastro) + "]");
                ConsolePrinter.pausar(150);
            }
        }
    }
}
