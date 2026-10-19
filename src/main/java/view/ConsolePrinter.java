package view;

import domain.entities.demand.Demanda;
import domain.entities.machine.Maquina;
import domain.entities.productionmanager.GerenciadorProducao;

import java.util.List;

/**
 * Tudo que é impressão no terminal: cores ANSI, logs de sucesso/falha, linhas em árvore da produção,
 * cabeçalhos, tabelas de demandas e de estoque, e as pausas da animação da produção
 *
 * @author Heitor Almeida
 */
public class ConsolePrinter {
    /** Só tem métodos estáticos, não é p/ instanciar */
    private ConsolePrinter() {
    }

    /** Volta a cor e o estilo pro padrão do terminal */
    public static final String RESET = "\u001B[0m";
    /** Deixa o texto em negrito */
    public static final String NEGRITO = "\u001B[1m";
    /** Laranja, usado no título e no cenário */
    public static final String LARANJA = "\u001B[38;5;208m";
    /** Azul, usado em menções e destaques */
    public static final String AZUL = "\u001B[38;5;75m";
    /** Roxo, cor do submenu de velocidade */
    public static final String ROXO = "\u001B[38;5;141m";
    /** Verde, usado nos logs de sucesso */
    public static final String VERDE = "\u001B[38;5;114m";
    /** Amarelo, usado em avisos e produtos que precisam de manutenção */
    public static final String AMARELO = "\u001B[38;5;221m";
    /** Cinza, usado em textos secundários */
    public static final String CINZA = "\u001B[38;5;246m";
    /** Vermelho, usado nos logs de falha */
    public static final String VERMELHO = "\033[38;2;234;67;53m";
    private static final int ATRASO_LINHA_ARVORE_MS = 125;
    private static VelocidadeProducao velocidadeProducao = VelocidadeProducao.NORMAL;

    private static final String FORMATO_LINHA_DEMANDA = "%-16s  %-12s  %-16s  %-16s  %-18s  %-10s";
    private static final int LARGURA_TABELA_DEMANDA = 16 + 12 + 16 + 16 + 18 + 10 + 2 * 5;

    /** Limpa o terminal e põe o cursor no topo */
    public static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Colore um texto e volta pra cor padrão no final
     *
     * @param cor cor, uma das constantes desta classe
     * @param texto texto a pintar
     * @return texto colrido
     */
    public static String colorir(String cor, String texto) {
        return cor + texto + RESET;
    }

    /**
     * Log de sucesso c/ um ✓ verde, retornado p/ a ultimaMensagem do {@link Menu}
     *
     * @param formato formato, igual ao do {@link String#format}
     * @param argumentos argumentos do formato
     * @return mensagem formatada
     */
    public static String textoOk(String formato, Object... argumentos) {
        return colorir(VERDE, "✓") + " " + String.format(formato, argumentos) + RESET;
    }

    /**
     * Log de falha c/ um ✗ vermelho, retornado p/ a ultimaMensagem do {@link Menu}
     *
     * @param formato formato, igual ao do {@link String#format}
     * @param argumentos argumentos do formato
     * @return mensagem formatada
     */
    public static String textoFalha(String formato, Object... argumentos) {
        return colorir(VERMELHO, "✗") + " " + String.format(formato, argumentos) + RESET;
    }

    /**
     * Imprime um log de sucesso, sem quebra de linha
     *
     * @param formato formato, igual ao do {@link String#format}
     * @param argumentos argumentos do formato
     */
    public static void imprimirOk(String formato, Object... argumentos) {
        System.out.print(textoOk(formato, argumentos));
    }

    /**
     * Imprime um log de falha, sem quebra de linha
     *
     * @param formato formato, igual ao do {@link String#format}
     * @param argumentos argumentos do formato
     */
    public static void imprimirFalha(String formato, Object... argumentos) {
        System.out.print(textoFalha(formato, argumentos));
    }

    /**
     * Cabeçalho de uma etapa da produção c/ a saúde e a chance de falha da máquina.
     * Se o desgaste das partes mudou a chance do cenário, mostra a diferença entre parênteses
     *
     * @param maquina máquina da etapa
     * @param formato nome da etapa, sai em caixa alta
     * @param argumentos argumentos do formato
     */
    public static void cabecalhoEtapa(Maquina maquina, String formato, Object... argumentos) {
        String titulo = ROXO + "▸ " + String.format(formato.toUpperCase(), argumentos) + RESET;
        String saude = String.format("%d/%d", maquina.getSaude(), maquina.getSaudeMaxima());
        String chanceFalha = "";
        double diffChanceFalha = maquina.getChanceFalha() - maquina.getChanceFalhaCenario();
        if (Math.abs(diffChanceFalha) < 1e-9) {
            chanceFalha = String.format("%.2f", maquina.getChanceFalha());
        } else {
            String simboloTriangulo = diffChanceFalha < 0 ? "▾" : "▴";
            chanceFalha = String.format("%.2f " + CINZA + "(%s %.2f)", maquina.getChanceFalha(), simboloTriangulo, Math.abs(diffChanceFalha));
        }
        System.out.printf(titulo + " [ " + VERMELHO + "❤ %s"+ RESET + " | " + AZUL + "⚂ %s" + RESET + " ]\n", saude, chanceFalha);
    }

    /**
     * Imprime uma linha da árvore de logs da produção e retarda a animação
     *
     * @param icone ícone da linha, ex.: "✓"
     * @param cor cor do ícone
     * @param ehUltima true se for a última linha do ramo ({@code └─}), senão usa {@code ├─}
     * @param formato formato, igual ao do {@link String#format}
     * @param argumentos argumentos do formato
     */
    private static void linhaArvore(String icone, String cor, boolean ehUltima, String formato, Object... argumentos) {
        String conector = ehUltima ? "└─" : "├─";
        System.out.printf("  %s " + cor + "%s" + RESET + " %s\n", conector, icone, String.format(formato, argumentos));
        pausarProducao(ATRASO_LINHA_ARVORE_MS);
    }

    /**
     * Segura o terminal por um instante pro usuário conseguir ler a mensagem.
     * Não depende da velocidade da produção ({@link #pausarProducao(int)})
     *
     * @param milis tempo de pausa, em milissegundos
     */
    public static void pausar(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Pausa da animação da produção, ajustada pela velocidade escolhida no menu.
     * Na velocidade instantânea não pausa nada
     *
     * @param milis tempo de pausa na velocidade normal, em milissegundos
     */
    public static void pausarProducao(int milis) {
        int ajustado = (int) Math.round(milis * velocidadeProducao.getFatorPausa());
        if (ajustado > 0) {
            pausar(ajustado);
        }
    }

    /**
     * Velocidade da animação da produção
     *
     * @return velocidade atual
     */
    public static VelocidadeProducao getVelocidadeProducao() {
        return velocidadeProducao;
    }

    /**
     * Troca a velocidade da animação da produção, vale p/ o resto da sessão
     *
     * @param velocidade nova velocidade
     */
    public static void setVelocidadeProducao(VelocidadeProducao velocidade) {
        velocidadeProducao = velocidade;
    }

    /**
     * Linha de sucesso na árvore de logs da produção
     *
     * @param ehUltima true se for a última linha do ramo
     * @param formato formato, igual ao do {@link String#format}
     * @param argumentos argumentos do formato
     */
    public static void arvoreOk(boolean ehUltima, String formato, Object... argumentos) {
        linhaArvore("✓", VERDE, ehUltima, formato, argumentos);
    }

    /**
     * Linha de falha na árvore de logs da produção
     *
     * @param ehUltima true se for a última linha do ramo
     * @param formato formato, igual ao do {@link String#format}
     * @param argumentos argumentos do formato
     */
    public static void arvoreFalha(boolean ehUltima, String formato, Object... argumentos) {
        linhaArvore("✗", VERMELHO, ehUltima, formato, argumentos);
    }

    /**
     * Linha informativa na árvore de logs da produção
     *
     * @param ehUltima true se for a última linha do ramo
     * @param formato formato, igual ao do {@link String#format}
     * @param argumentos argumentos do formato
     */
    public static void arvoreInfo(boolean ehUltima, String formato, Object... argumentos) {
        linhaArvore("ℹ", CINZA, ehUltima, formato, argumentos);
    }

    /**
     * Centraliza um texto preenchendo c/ espaços dos dois lados
     *
     * @param largura largura total
     * @param texto texto a centralizar
     * @return texto centralizado, ou o próprio texto se ele for null ou não couber
     */
    public static String centralizar(int largura, String texto) {
        if (texto == null || texto.length() >= largura) return texto;
        int espacosEsquerda = (largura - texto.length()) / 2;
        int espacosDireita = largura - texto.length() - espacosEsquerda;
        return " ".repeat(espacosEsquerda) + texto + " ".repeat(espacosDireita);
    }

    /**
     * Imprime um texto em negrito entre duas linhas, usado em cabeçalhos e no rodapé
     *
     * @param formato formato, igual ao do {@link String#format}
     * @param argumentos argumentos do formato
     */
    public static void cartao(String formato, Object... argumentos) {
        linha();
        System.out.printf(ConsolePrinter.NEGRITO + " %s\n", String.format(formato, argumentos));
        linha();
    }

    /** Linha horizontal cinza da largura do menu (65) */
    public static void linha() {
        System.out.println(CINZA + "─".repeat(65) + RESET);
    }

    /**
     * Linha recuada, vai logo embaixo do cabeçalho das tabelas
     *
     * @param tamanho largura da linha
     */
    public static void linhaRecuada(int tamanho) {
        System.out.println(CINZA + "    " + "─".repeat(tamanho) + RESET);
    }

    /** Imprime a opção "0. Voltar" dos submenus */
    public static void imprimirOpcaoVoltar() {
        System.out.println(CINZA + " 0. ↩ Voltar" + RESET + "\n");
    }

    /**
     * Imprime uma lista numerada de opções de menu.
     *
     * @param opcoes opções do menu, c/ a de voltar/sair por último
     */
    public static void listaOpcoes(String... opcoes) {
        for (int i = 0; i < opcoes.length - 1; i++) {
            System.out.printf(CINZA + " %d." + RESET + " %s\n", (i + 1), opcoes[i]);
        }
        System.out.println();
        System.out.printf(CINZA + " 0. ↩ %s\n\n" + RESET, opcoes[opcoes.length - 1]);
    }

    /**
     * Linha de resumo da fábrica, mostrada no rodapé dos menus
     *
     * @param orcamento orçamento, em R$
     * @param quantidadeMateriaPrima matéria-prima em estoque, em kg
     * @param quantidadeFabricada quantas unidades estão no armazém
     */
    public static void imprimirResumo(double orcamento, double quantidadeMateriaPrima, int quantidadeFabricada) {
        System.out.printf(CINZA + "[ "
                        + VERDE + "Orçamento: R$ %.2f" + CINZA
                        + " | "
                        + AZUL + "Mat. Prima: %.2f kg"  + CINZA
                        + " | "
                        + ROXO + "Produtos: %d un" + CINZA
                        + " ]\n" + RESET,
                orcamento,
                quantidadeMateriaPrima,
                quantidadeFabricada
        );
    }

    /**
     * Tabela de demandas c/ produto, quantidade, matéria-prima, custo, status e tempo, seguida da
     * projeção do orçamento depois de fabricar todas as pendentes. Avisa se o orçamento não dá
     *
     * @param gerenciador gerenciador da produção, de onde vêm as demandas e o orçamento
     */
    public static void listarDemandas(GerenciadorProducao gerenciador) {
        List<Demanda> demandas = gerenciador.getDemandas();
        double custoProjetadoTotal = gerenciador.getCustoProjetado(); // also refreshes each demand's cost btw

        System.out.printf("    " + CINZA + FORMATO_LINHA_DEMANDA + RESET + "\n",
                "Produto", "Demanda", "MP", "Custo", "Status", "Tempo");
        linhaRecuada(LARGURA_TABELA_DEMANDA);

        for (int i = 0; i < demandas.size(); i++) {
            Demanda demanda = demandas.get(i);
            double custoDemanda = demanda.getCustoEstimado();

            /* Padding the plain text to the Status column's visible width yah */
            String rotuloStatus;
            String corStatus;
            switch (demanda.getStatus()) {
                case CONCLUIDA -> {
                    rotuloStatus = "Concluída";
                    corStatus = VERDE;
                }
                case CANCELADA -> {
                    // shows the progress when it got somewhere before stopping
                    rotuloStatus = demanda.getQuantidadeProduzida() > 0
                            ? String.format("Cancelada (%d/%d)", demanda.getQuantidadeProduzida(), demanda.getQuantidade())
                            : "Cancelada";
                    corStatus = VERMELHO;
                }
                default -> {
                    rotuloStatus = "Pendente";
                    corStatus = CINZA;
                }
            }
            String status = corStatus + String.format("%-18s", rotuloStatus) + RESET;
            String tempo = demanda.getQuantidadeProduzida() == 0
                    ? "-"
                    : String.format("%.2fs", demanda.getTempoTotalProducao());

            // status already comes colored + padded, so it goes in as a plain %s instead of %-18s
            System.out.printf(CINZA + " %-1s." + RESET + " " + FORMATO_LINHA_DEMANDA.replace("%-18s", "%s") + RESET + "\n",
                    (i + 1),
                    demanda.getNomeProduto(),
                    demanda.getQuantidade() + " un",
                    String.format("%.2f kg", demanda.getMateriaPrimaTotal()),
                    String.format("R$ %.2f", custoDemanda),
                    status,
                    tempo
            );
        }

        System.out.println();
        if (custoProjetadoTotal > 0) {
            double orcamentoProjetado = gerenciador.getVerba() - custoProjetadoTotal;
            System.out.printf(" Projeção Total de Orçamento Após Custo Operacional: R$ %.2f " + VERMELHO + "(▾ R$ -%.2f)" + RESET + "\n",
                    orcamentoProjetado, custoProjetadoTotal);
            if (orcamentoProjetado < 0) {
                System.out.println("\n" + colorir(VERMELHO, " ⚠ Orçamento insuficiente para fabricar todas as demandas pendentes!"));
            }
        } else {
            System.out.printf(" Projeção Total de Orçamento Após Custo Operacional: R$ %.2f\n" + RESET, gerenciador.getVerba());
        }
        System.out.println();
    }
}