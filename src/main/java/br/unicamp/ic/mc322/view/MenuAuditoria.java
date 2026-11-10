package br.unicamp.ic.mc322.view;

import br.unicamp.ic.mc322.model.machine.Maquina;
import br.unicamp.ic.mc322.model.product.Produto;
import br.unicamp.ic.mc322.model.productionline.LinhaDeProducao;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.model.report.Relatorio;
import br.unicamp.ic.mc322.exception.OpcaoInvalidaException;
import br.unicamp.ic.mc322.model.Auditavel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Submenu de auditáveis. Mostra o relatório geral da produção e a lista de problemas (máquinas e
 * produtos que precisam de manutenção), a auditoria estrutural da linha em cascata (Linha > Máquina > Partes)
 * e o diagnóstico de cada produto
 *
 * @author Glayson Oliveira
 * @author Heitor Almeida
 */
public class MenuAuditoria extends Submenu {
    /** Opção que expande ou recolhe todas as máquinas da auditoria estrutural */
    private static final int OPCAO_EXPANDIR_TODAS = 99;
    /** Linha de produção auditada na auditoria estrutural */
    private final LinhaDeProducao linha;

    /**
     * Cria o submenu c/ a cor roxa
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção, de onde vêm as máquinas, os produtos e o relatório
     * @param linha linha de produção, p/ a auditoria estrutural
     */
    public MenuAuditoria(Menu menu, GerenciadorProducao gerenciadorProducao, LinhaDeProducao linha) {
        super(menu, gerenciadorProducao, ConsolePrinter.ROXO);
        this.linha = linha;
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
            String[] opcoes = {"Relatório geral", "Auditoria estrutural da linha", "Detalhar produtos", "Voltar"};
            ConsolePrinter.listaOpcoes(opcoes);

            menu.imprimirRodape();

            int opcao = menu.lerInteiro("Escolha: ");

            if (opcao == 0) break;

            switch (opcao) {
                case 1 -> relatorioGeral();
                case 2 -> auditoriaEstrutural();
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
     * Submenu da auditoria estrutural. Mostra a árvore em cascata da linha de produção: a linha, cada máquina
     * c/ o estado e, nas máquinas expandidas, o diagnóstico completo c/ as partes .
     */
    private void auditoriaEstrutural() {
        List<Maquina> maquinas = linha.getMaquinas();
        boolean[] expandidas = new boolean[maquinas.size()];

        while (true) {
            imprimirCabecalho();
            System.out.println(ConsolePrinter.colorir(cor, " Auditoria estrutural da linha\n"));

            System.out.println(linha.gerarRelatorioDiagnostico(i -> expandidas[i]));

            System.out.printf(ConsolePrinter.CINZA + " %d." + ConsolePrinter.RESET + " Expandir/Recolher todas\n", OPCAO_EXPANDIR_TODAS);
            ConsolePrinter.imprimirOpcaoVoltar();
            menu.imprimirRodape();

            int opcao = menu.lerInteiro("Qual máquina deseja " + ConsolePrinter.colorir(cor, "EXPANDIR") + "? ");
            if (opcao == 0) break;

            if (opcao == OPCAO_EXPANDIR_TODAS) {
                boolean expandirTodas = false;
                for (boolean expandida : expandidas) {
                    expandirTodas |= !expandida;
                }
                Arrays.fill(expandidas, expandirTodas);
                continue;
            }

            try {
                Menu.validarOpcao(opcao, expandidas.length, "Dessa vez não é! Máquina inválida!");
                expandidas[opcao - 1] = !expandidas[opcao - 1];
            } catch (OpcaoInvalidaException e) {
                menu.setUltimaMensagem(ConsolePrinter.textoFalha("%s", e.getMessage()));
            }
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
                // Cabeçalho alinhado da tabela
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