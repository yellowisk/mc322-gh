package view;

import domain.entities.product.Produto;
import domain.entities.productionmanager.GerenciadorProducao;
import domain.entities.report.Relatorio;
import domain.interfaces.Auditavel;

import java.util.ArrayList;
import java.util.List;

/**
 * Submenu de auditáveis. Mostra o relatório geral da produção e a lista de problemas (máquinas e
 * produtos que precisam de manutenção), e deixa detalhar o diagnóstico de cada máquina e de cada produto
 *
 * @author Glayson Oliveira
 * @author Heitor Almeida
 */
public class MenuAuditoria extends Submenu {

    /**
     * Cria o submenu c/ a cor roxa
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção, de onde vêm as máquinas, os produtos e o relatório
     */
    public MenuAuditoria(Menu menu, GerenciadorProducao gerenciadorProducao) {
        super(menu, gerenciadorProducao, ConsolePrinter.ROXO);
    }

    @Override
    public String icone() {
        return ConsolePrinter.colorir(cor, "⌕");
    }

    @Override
    public String rotulo() {
        return "Auditoria";
    }

    @Override
    public void exibir() {
        boolean rodando = true;
        while (rodando) {
            imprimirCabecalho();

            // Lista de opções
            String[] opcoes = {"Relatório geral", "Detalhar máquinas", "Detalhar produtos", "Voltar"};
            ConsolePrinter.listaOpcoes(opcoes);

            menu.imprimirRodape();

            int opcao = menu.lerInteiro("Escolha: ");

            if (opcao == 0) break;

            switch (opcao) {
                case 1 -> relatorioGeral();
                case 2 -> detalharMaquinas();
                case 3 -> detalharProdutos();
                default -> menu.setUltimaMensagem(ConsolePrinter.textoFalha("Dessa vez não é! Opção inválida!"));
            }
        }
    }

    /** Números acumulados de todas as produções da sessão */
    private void imprimirRelatorioProducao() {
        Relatorio relatorio = gerenciadorProducao.getRelatorio();
        String unidade = gerenciadorProducao.getMateriaPrima().getUnidade();

        System.out.println(ConsolePrinter.NEGRITO + " Relatório da Produção" + ConsolePrinter.RESET);
        System.out.printf(" - Produtos fabricados: %d\n", relatorio.getTotalProdutos());
        System.out.printf("   %s✓ %d aprovados%s | %s! %d precisam de manutenção%s | %s✗ %d rejeitados%s\n",
                ConsolePrinter.VERDE, relatorio.getTotalProdutosAprovados(), ConsolePrinter.RESET,
                ConsolePrinter.AMARELO, relatorio.getTotalProdutosPrecisamManutencao(), ConsolePrinter.RESET,
                ConsolePrinter.VERMELHO, relatorio.getTotalProdutosRejeitados(), ConsolePrinter.RESET);
        System.out.printf(" - Taxa de sucesso: %.0f%%\n", relatorio.getTaxaSucessoProducao() * 100);
        System.out.printf(" - Tempo total de produção: %.2f s\n", relatorio.getTempoTotalProducao());
        System.out.printf(" - Matéria-prima gasta: %.2f %s\n", relatorio.getMateriaPrimaGastaTotal(), unidade);
        System.out.printf(" - Dinheiro gasto com operação das máquinas: R$ %.2f\n\n", relatorio.getDinheiroGasto());
    }

    /**
     * Submenu do relatório geral.
     */
    private void relatorioGeral() {
        boolean rodando = true;

        while (rodando) {
            imprimirCabecalho();

            System.out.println(ConsolePrinter.colorir(cor," Relatório geral\n"));

            imprimirRelatorioProducao();

            // Lista com todas as entidade auditáveis
            List<Auditavel> auditaveis = new ArrayList<>();
            auditaveis.addAll(gerenciadorProducao.getMaquinas());
            auditaveis.addAll(gerenciadorProducao.getProdutosFabricados());

            int totalAuditaveis = auditaveis.size();
            int quantidadeProblemas = 0;

            System.out.println(ConsolePrinter.NEGRITO + " Relatório da Fábrica" + ConsolePrinter.RESET);

            for (Auditavel auditavel : auditaveis) {
                if (auditavel.precisaManutencao()) {
                    quantidadeProblemas++;
                }
            }

            System.out.printf(" - Total de itens auditados: %d\n", totalAuditaveis);
            System.out.printf(" - Itens precisando de intervenção: %s%d%s\n",
                    quantidadeProblemas > 0 ? ConsolePrinter.VERMELHO : ConsolePrinter.VERDE,
                    quantidadeProblemas,
                    ConsolePrinter.RESET);

            System.out.println(ConsolePrinter.NEGRITO + " Lista de problemas:" + ConsolePrinter.RESET);

            boolean foundIssues = false;
            for (Auditavel auditavel : auditaveis) {
                if (auditavel.precisaManutencao()) {
                    foundIssues = true;
                    /* Cada Auditavel sabe se descrever, então não precisamos saber se é máquina ou produto */
                    System.out.println(ConsolePrinter.VERMELHO + " ✗ " + ConsolePrinter.RESET + auditavel.gerarResumoProblema());
                }
            }

            if (!foundIssues) {
                System.out.println(ConsolePrinter.VERDE + " ✓ Nenhum problema encontrado! Tudo operando perfeitamente." + ConsolePrinter.RESET);
            }

            System.out.println();
            ConsolePrinter.imprimirOpcaoVoltar();
            menu.imprimirRodape();

            int opcao = menu.lerInteiro("Escolha: ");
            if (opcao == 0) {
                break;
            } else {
                menu.setUltimaMensagem(ConsolePrinter.textoFalha("Dessa vez não é! Opção inválida!"));
            }
        }
    }

    /**
     * Submenu de detalhar máquinas. Contém opções expansíveis.
     */
    private void detalharMaquinas() {
        boolean rodando = true;

        while (rodando) {
            imprimirCabecalho();
            System.out.println(ConsolePrinter.colorir(cor," Auditoria de máquinas\n"));

            System.out.println(gerenciadorProducao.gerarRelatorioDiagnostico());

            ConsolePrinter.imprimirOpcaoVoltar();

            menu.imprimirRodape();

            // Escolha da opção
            int opcao = menu.lerInteiro("Escolha: ");
            if (opcao == 0) break;
        }
    }

    /**
     * Submenu de detalhar produtos. Mostra a tabela de diagnóstico de todos os produtos do armazém
     * (id, nome, lote, qualidade, risco acumulado, risco de rejeição e status)
     */
    private void detalharProdutos() {
        boolean rodando = true;

        // Array de Produtos
        List<Produto> produtos = gerenciadorProducao.getProdutosFabricados();

        while (rodando) {
            imprimirCabecalho();
            System.out.println(ConsolePrinter.colorir(cor," Auditoria de produtos\n"));

            if (produtos.isEmpty()) {
                System.out.println(ConsolePrinter.CINZA + " Nenhum produto foi produzido ainda." + ConsolePrinter.RESET);
            } else {
                // Ccabeçalho alinhado da tabela
                System.out.println(" Produtos produzidos:\n");
                System.out.printf("    " + ConsolePrinter.CINZA + "%-5s %-16s %-6s %-10s %-12s %-10s %-20s" + ConsolePrinter.RESET + "\n",
                        "ID", "Nome", "Lote", "Qualidade", "Risco Acum.", "Rejeição", "Status");
                ConsolePrinter.linhaRecuada(85);

                // Imprime as linhas da tabela
                for (Produto p : produtos) {
                    System.out.println(p.gerarRelatorioDiagnostico());
                }
            }

            System.out.println();
            ConsolePrinter.imprimirOpcaoVoltar();

            menu.imprimirRodape();

            int opcao = menu.lerInteiro("Escolha: ");

            if (opcao == 0) {
                break;
            } else {
                menu.setUltimaMensagem(ConsolePrinter.textoFalha("Dessa vez não é! Opção inválida!"));
            }
        }
    }
}