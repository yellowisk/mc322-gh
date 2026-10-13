package view;

import domain.entities.machine.Maquina;
import domain.entities.product.Produto;
import domain.entities.productionmanager.GerenciadorProducao;
import domain.entities.report.Relatorio;
import domain.exceptions.InvalidOptionException;
import domain.interfaces.Auditavel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Submenu de auditáveis. Mostra o relatório geral da produção e a lista de problemas (máquinas e
 * produtos que precisam de manutenção), e deixa detalhar o diagnóstico de cada máquina e de cada produto
 *
 * @author Glayson Oliveira
 * @author Heitor Almeida
 */
public class AuditMenu extends Submenu {

    /**
     * Cria o submenu c/ a cor roxa
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção, de onde vêm as máquinas, os produtos e o relatório
     */
    public AuditMenu(Menu menu, GerenciadorProducao gerenciadorProducao) {
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
        boolean running = true;
        while (running) {
            imprimirCabecalho();

            // Lista de opções
            String[] opcoes = {"Relatório geral", "Detalhar máquinas", "Detalhar produtos", "Voltar"};
            ConsolePrinter.listaOpcoes(opcoes);

            menu.printFooter();

            int option = menu.readInt("Escolha: ");

            if (option == 0) break;

            switch (option) {
                case 1 -> relatorioGeral();
                case 2 -> detalharMaquinas();
                case 3 -> detalharProdutos();
                default -> menu.setLastBuffer(ConsolePrinter.textoFalha("Dessa vez não é! Opção inválida!"));
            }
        }
    }

    /** Números acumulados de todas as produções da sessão */
    private void printProductionReport() {
        Relatorio report = gerenciadorProducao.getRelatorio();
        String unit = gerenciadorProducao.getMateriaPrima().getUnidade();

        System.out.println(ConsolePrinter.NEGRITO + " Relatório da Produção" + ConsolePrinter.RESET);
        System.out.printf(" - Produtos fabricados: %d\n", report.getTotalProdutos());
        System.out.printf("   %s✓ %d aprovados%s | %s! %d precisam de manutenção%s | %s✗ %d rejeitados%s\n",
                ConsolePrinter.VERDE, report.getTotalProdutosAprovados(), ConsolePrinter.RESET,
                ConsolePrinter.AMARELO, report.getTotalProdutosPrecisamManutencao(), ConsolePrinter.RESET,
                ConsolePrinter.VERMELHO, report.getTotalProdutosRejeitados(), ConsolePrinter.RESET);
        System.out.printf(" - Taxa de sucesso: %.0f%%\n", report.getTaxaSucessoProducao() * 100);
        System.out.printf(" - Tempo total de produção: %.2f s\n", report.getTempoTotalProducao());
        System.out.printf(" - Matéria-prima gasta: %.2f %s\n", report.getMateriaPrimaGastaTotal(), unit);
        System.out.printf(" - Dinheiro gasto com operação das máquinas: R$ %.2f\n\n", report.getDinheiroGasto());
    }

    /**
     * Submenu do relatório geral.
     */
    private void relatorioGeral() {
        boolean running = true;

        while (running) {
            imprimirCabecalho();

            System.out.println(ConsolePrinter.colorir(cor," Relatório geral\n"));

            printProductionReport();

            // Lista com todas as entidade auditáveis
            List<Auditavel> auditables = new ArrayList<>();
            auditables.addAll(gerenciadorProducao.getMaquinas());
            auditables.addAll(gerenciadorProducao.getProdutosFabricados());

            int totalAuditables = auditables.size();
            int issuesCount = 0;

            System.out.println(ConsolePrinter.NEGRITO + " Relatório da Fábrica" + ConsolePrinter.RESET);

            for (Auditavel auditable : auditables) {
                if (auditable.precisaManutencao()) {
                    issuesCount++;
                }
            }

            System.out.printf(" - Total de itens auditados: %d\n", totalAuditables);
            System.out.printf(" - Itens precisando de intervenção: %s%d%s\n",
                    issuesCount > 0 ? ConsolePrinter.VERMELHO : ConsolePrinter.VERDE,
                    issuesCount,
                    ConsolePrinter.RESET);

            System.out.println(ConsolePrinter.NEGRITO + " Lista de problemas:" + ConsolePrinter.RESET);

            boolean foundIssues = false;
            for (Auditavel auditable : auditables) {
                if (auditable.precisaManutencao()) {
                    foundIssues = true;
                    /* Cada Auditavel sabe se descrever, então não precisamos saber se é máquina ou produto */
                    System.out.println(ConsolePrinter.VERMELHO + " ✗ " + ConsolePrinter.RESET + auditable.gerarResumoProblema());
                }
            }

            if (!foundIssues) {
                System.out.println(ConsolePrinter.VERDE + " ✓ Nenhum problema encontrado! Tudo operando perfeitamente." + ConsolePrinter.RESET);
            }

            System.out.println();
            ConsolePrinter.imprimirOpcaoVoltar();
            menu.printFooter();

            int option = menu.readInt("Escolha: ");
            if (option == 0) {
                break;
            } else {
                menu.setLastBuffer(ConsolePrinter.textoFalha("Dessa vez não é! Opção inválida!"));
            }
        }
    }

    /**
     * Submenu de detalhar máquinas. Contém opções expansíveis.
     */
    private void detalharMaquinas() {
        boolean running = true;

        // Array de máquinas
        List<Maquina> machineList = gerenciadorProducao.getMaquinas();
        int[] expandedOptions = new int[machineList.size()]; // Flags de ativação (1 ou 0)
        boolean expandAll = true; // Controle de estado para a opção de Expandir/Recolher tudo

        while (running) {
            imprimirCabecalho();
            System.out.println(ConsolePrinter.colorir(cor," Auditoria de máquinas\n"));

            if (machineList.isEmpty()) {
                System.out.println(ConsolePrinter.CINZA + " Não há nenhuma máquina cadastrada." + ConsolePrinter.RESET);
            }

            // Menu: Lista expansível de máquinas
            System.out.println(" Máquinas disponíveis:\n");
            int i = 0;
            for (Maquina m : machineList) {
                System.out.printf(
                        ConsolePrinter.CINZA + "  %d." + ConsolePrinter.RESET + " %s " + ConsolePrinter.CINZA + "(%s)" + ConsolePrinter.RESET + "\n",
                        (i + 1), m.getNome(), m.getTipo());

                if (expandedOptions[i] == 1) { // Expansão
                    System.out.print(m.gerarRelatorioDiagnostico());
                }
                i++;
            }

            System.out.println();
            if (!machineList.isEmpty()) {
                System.out.printf(ConsolePrinter.CINZA + " 99." + ConsolePrinter.RESET + " Expandir/Recolher todos\n\n");
            }
            ConsolePrinter.imprimirOpcaoVoltar();

            menu.printFooter();

            // Escolha da opção
            int option = menu.readInt("Escolha: ");

            if (option == 0) break;

            // Tratamento da opção Expandir/Recolher todos
            if (option == 99 && !machineList.isEmpty()) {
                int newState = expandAll ? 1 : 0;
                Arrays.fill(expandedOptions, newState);
                expandAll = !expandAll;
                continue;
            }

            try {
                Menu.validarOpcao(option, expandedOptions.length, "Dessa vez não é! Máquina inválida!");
                expandedOptions[option - 1] = expandedOptions[option - 1] ^ 1; // Inverte a flag usando bitwise XOR
            } catch (InvalidOptionException e) {
                menu.setLastBuffer(ConsolePrinter.textoFalha("%s", e.getMessage()));
            }
        }
    }

    /**
     * Submenu de detalhar produtos. Mostra a tabela de diagnóstico de todos os produtos do armazém
     * (id, nome, lote, qualidade, risco acumulado, risco de rejeição e status)
     */
    private void detalharProdutos() {
        boolean running = true;

        // Array de Produtos
        List<Produto> productList = gerenciadorProducao.getProdutosFabricados();

        while (running) {
            imprimirCabecalho();
            System.out.println(ConsolePrinter.colorir(cor," Auditoria de produtos\n"));

            if (productList.isEmpty()) {
                System.out.println(ConsolePrinter.CINZA + " Nenhum produto foi produzido ainda." + ConsolePrinter.RESET);
            } else {
                // Ccabeçalho alinhado da tabela
                System.out.println(" Produtos produzidos:\n");
                System.out.printf("    " + ConsolePrinter.CINZA + "%-5s %-16s %-6s %-10s %-12s %-10s %-20s" + ConsolePrinter.RESET + "\n",
                        "ID", "Nome", "Lote", "Qualidade", "Risco Acum.", "Rejeição", "Status");
                ConsolePrinter.linhaRecuada(85);

                // Imprime as linhas da tabela
                for (Produto p : productList) {
                    System.out.println(p.gerarRelatorioDiagnostico());
                }
            }

            System.out.println();
            ConsolePrinter.imprimirOpcaoVoltar();

            menu.printFooter();

            int option = menu.readInt("Escolha: ");

            if (option == 0) {
                break;
            } else {
                menu.setLastBuffer(ConsolePrinter.textoFalha("Dessa vez não é! Opção inválida!"));
            }
        }
    }
}