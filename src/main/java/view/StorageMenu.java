package view;

import domain.entities.product.Product;
import domain.entities.productionmanager.ProductionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StorageMenu extends Submenu {

    public StorageMenu(Menu menu, ProductionManager productionManager) {
        super(menu, productionManager, ConsolePrinter.BLUE);
    }

    @Override
    public String icon() {
        return ConsolePrinter.color(color, "≡");
    }

    @Override
    public String label() {
        return "Ver armazém";
    }

    @Override
    public void show() {
        List<Product> fabricatedProducts = productionManager.getFabricatedProducts();

        // Agrupamento de produtos por listas
        List<Integer> batchNumbers = new ArrayList<>();
        List<List<Product>> batchLists = new ArrayList<>();
        for (Product p : fabricatedProducts) {
            int batchNum = p.getBatch();
            int index = batchNumbers.indexOf(batchNum);

            if (index == -1) {
                // Se o lote ainda não existe nas listas, cria um novo
                batchNumbers.add(batchNum);
                List<Product> newList = new ArrayList<>();
                newList.add(p);
                batchLists.add(newList);
            } else {
                // Se já existe, adiciona o produto na lista correspondente ao índice
                batchLists.get(index).add(p);
            }
        }

        // Arrays de controle para expansão (1 ou 0)
        int[] expandedOptions = new int[batchNumbers.size()];
        boolean expandAll = true;

        while (true) {
            printHeader();
            System.out.println(" Lista de lotes: ");

            if (batchNumbers.isEmpty()) {
                System.out.println(ConsolePrinter.GRAY + " O armazém está vazio. Nenhum produto finalizado." + ConsolePrinter.RESET + "\n");
            } else {
                for (int i = 0; i < batchNumbers.size(); i++) {
                    int batchId = batchNumbers.get(i);
                    List<Product> productsInBatch = batchLists.get(i);

                    // Linha principal do lote (não expansível)
                    System.out.printf(ConsolePrinter.GRAY + " %d." + ConsolePrinter.RESET + " Lote #%d: %s (★ %.2f) [Total: %d un]\n",
                            (i + 1), batchId,
                            productsInBatch.getFirst().getName(),
                            productsInBatch.getFirst().getQuality(),
                            productsInBatch.size());

                    // Forma expandida: lista os produtos internos do lote
                    if (expandedOptions[i] == 1) {
                        for (Product p : productsInBatch) {
                            String treeSymbol = (p == productsInBatch.getLast()) ? "└─" : "├─";
                            System.out.printf("    " + ConsolePrinter.GRAY + " %s %s #%d, Risco %s, %s\n" + ConsolePrinter.RESET,
                                    treeSymbol,
                                    p.getName(), p.getId(), String.format("%.0f%%", p.getRejectionRisk() * 100),
                                    p.needsMaintenance()
                                            ? ConsolePrinter.color(ConsolePrinter.RED, "✗ Alto risco")
                                            : ConsolePrinter.color(ConsolePrinter.GREEN, "✓ OK")
                                    );
                        }
                    }
                }
            }

            System.out.println();
            if (!batchNumbers.isEmpty()) {
                System.out.printf(ConsolePrinter.GRAY + " 99." + ConsolePrinter.RESET + " Expandir/Recolher todos\n\n");
            }

            ConsolePrinter.printBackOption();
            menu.printFooter();

            int option = menu.readInt("Escolha: ");

            if (option == 0) break;

            // Opção 99 para Expandir/Recolher tudo
            if (option == 99 && !batchNumbers.isEmpty()) {
                int newState = expandAll ? 1 : 0;
                Arrays.fill(expandedOptions, newState);
                expandAll = !expandAll;
                continue;
            }

            try {
                expandedOptions[option - 1] ^= 1; // Inverte a flag usando bitwise XOR
            } catch (IndexOutOfBoundsException e) {
                menu.setLastBuffer(ConsolePrinter.failText("Dessa vez não é! Opção de lote inválida!"));
            }
        }
    }
}