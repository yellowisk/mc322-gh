package view;

import domain.entities.machine.Machine;
import domain.entities.product.Product;
import domain.entities.productionmanager.ProductionManager;
import domain.interfaces.Auditable;

import java.util.ArrayList;
import java.util.List;

public class AuditMenu extends Submenu {

    public AuditMenu(Menu menu, ProductionManager productionManager) {
        super(menu, productionManager, ConsolePrinter.PURPLE);
    }

    @Override
    public String icon() {
        return ConsolePrinter.color(color, "≡");
    }

    @Override
    public String label() {
        return "Auditoria";
    }

    @Override
    public void show() {
        boolean running = true;
        while (running) {
            printHeader();

            // Lista de opções
            String[] opcoes = {"Relatório geral", "Detalhar máquinas", "Detalhar produtos", "Voltar"};
            ConsolePrinter.optionsList(opcoes);

            menu.printFooter();

            int option = menu.readInt("Escolha: ");

            if (option == 0) break;

            switch (option) {
                case 1 -> relatorioGeral();
                case 2 -> detalharMaquinas();
                case 3 -> detalharProdutos();
                default -> menu.setLastBuffer(ConsolePrinter.failText("Dessa vez não é! Opção inválida!"));
            }
        }
    }

    /**
     * Submenu do relatório geral.
     */
    private void relatorioGeral() {
        boolean running = true;

        while (running) {
            printHeader();

            // Lista com todas as entidade auditáveis
            List<Auditable> auditables = new ArrayList<>();
            auditables.addAll(productionManager.getMachines());
            auditables.addAll(productionManager.getFabricatedProducts());

            int totalAuditables = auditables.size();
            int issuesCount = 0;

            System.out.println(ConsolePrinter.BOLD + " Relatório da Fábrica" + ConsolePrinter.RESET);

            for (Auditable auditable : auditables) {
                if (auditable.needsMaintenance()) {
                    issuesCount++;
                }
            }

            System.out.printf(" - Total de itens auditados: %d\n", totalAuditables);
            System.out.printf(" - Itens precisando de intervenção: %s%d%s\n",
                    issuesCount > 0 ? ConsolePrinter.RED : ConsolePrinter.GREEN,
                    issuesCount,
                    ConsolePrinter.RESET);

            ConsolePrinter.line();
            System.out.println(ConsolePrinter.BOLD + " Lista de problemas:" + ConsolePrinter.RESET);

            boolean foundIssues = false;
            for (Auditable auditable : auditables) {
                if (auditable.needsMaintenance()) {
                    foundIssues = true;
                    if (auditable instanceof Machine m) {
                        System.out.printf(ConsolePrinter.RED + " ✗ [Máquina]" + ConsolePrinter.RESET + " %s (Saúde: %d/%d)\n", m.getName(), m.getSaude(), m.getSaudeMaxima());
                    } else if (auditable instanceof Product p) {
                        System.out.printf(ConsolePrinter.RED + " ✗ [Produto]" + ConsolePrinter.RESET + " %s #%d (Risco de Rejeição: %.0f%%)\n", p.getName(), p.getId(), p.getRejectionRisk() * 100);
                    }
                }
            }

            if (!foundIssues) {
                System.out.println(ConsolePrinter.GREEN + " ✓ Nenhum problema encontrado! Tudo operando perfeitamente." + ConsolePrinter.RESET);
            }

            System.out.println();
            ConsolePrinter.printBackOption();
            menu.printFooter();

            int option = menu.readInt("Escolha: ");
            if (option == 0) {
                break;
            } else {
                menu.setLastBuffer(ConsolePrinter.failText("Dessa vez não é! Opção inválida!"));
            }
        }
    }

    /**
     * Submenu de detalhar máquinas. Contém opções expansíveis.
     */
    private void detalharMaquinas() {
        boolean running = true;

        // Array de máquinas
        List<Machine> machineList = productionManager.getMachines();
        int[] expandedOptions = new int[machineList.size()]; // Flags de ativação (1 ou 0)
        boolean expandAll = true; // Controle de estado para a opção de Expandir/Recolher tudo

        while (running) {
            printHeader();

            if (machineList.isEmpty()) {
                System.out.println(ConsolePrinter.GRAY + " Não há nenhuma máquina cadastrada." + ConsolePrinter.RESET);
            }

            // Menu: Lista expansível de máquinas
            int i = 0;
            for (Machine m : machineList) {
                System.out.printf(
                        ConsolePrinter.GRAY + " %d." + ConsolePrinter.RESET + " %s " + ConsolePrinter.GRAY + "(%s)" + ConsolePrinter.RESET + "\n",
                        (i + 1), m.getName(), m.getType());

                if (expandedOptions[i] == 1) { // Expansão
                    System.out.print(m.generateDiagnosticReport());
                }
                i++;
            }

            System.out.println();
            if (!machineList.isEmpty()) {
                System.out.printf(ConsolePrinter.GRAY + " 99." + ConsolePrinter.RESET + " Expandir/Recolher todos\n\n");
            }
            ConsolePrinter.printBackOption();

            menu.printFooter();

            // Escolha da opção
            int option = menu.readInt("Escolha: ");

            if (option == 0) break;

            // Tratamento da opção Expandir/Recolher todos
            if (option == 99 && !machineList.isEmpty()) {
                int newState = expandAll ? 1 : 0;
                for (int j = 0; j < expandedOptions.length; j++) {
                    expandedOptions[j] = newState;
                }
                expandAll = !expandAll;
                continue;
            }

            try {
                expandedOptions[option - 1] = expandedOptions[option - 1] ^ 1; // Inverte a flag usando bitwise XOR
            } catch (IndexOutOfBoundsException e) {
                menu.setLastBuffer(ConsolePrinter.failText("Dessa vez não é! Máquina inválida!"));
            }
        }
    }

    private void detalharProdutos() {
        boolean running = true;

        // Array de Produtos
        List<Product> productList = productionManager.getFabricatedProducts();

        while (running) {
            printHeader();

            if (productList.isEmpty()) {
                System.out.println(ConsolePrinter.GRAY + " Nenhum produto foi produzido ainda." + ConsolePrinter.RESET);
            } else {
                // Ccabeçalho alinhado da tabela
                System.out.printf("    " + ConsolePrinter.GRAY + "%-5s %-16s %-6s %-10s %-12s %-10s %-20s" + ConsolePrinter.RESET + "\n",
                        "ID", "Nome", "Lote", "Qualidade", "Risco Acum.", "Rejeição", "Status");
                ConsolePrinter.indentedLine(85);

                // Imprime as linhas da tabela
                for (Product p : productList) {
                    System.out.println(p.generateDiagnosticReport());
                }
            }

            System.out.println();
            ConsolePrinter.printBackOption();

            menu.printFooter();

            int option = menu.readInt("Escolha: ");

            if (option == 0) {
                break;
            } else {
                menu.setLastBuffer(ConsolePrinter.failText("Dessa vez não é! Opção inválida!"));
            }
        }
    }
}