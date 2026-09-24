package view;

import domain.entities.demand.Demand;
import domain.entities.demand.DemandStatus;
import domain.entities.machine.Machine;
import domain.entities.product.Product;
import domain.entities.productionmanager.ProductionManager;

import java.util.List;

public class ConsolePrinter {
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String ORANGE = "\u001B[38;5;208m";
    public static final String BLUE = "\u001B[38;5;75m";
    public static final String PURPLE = "\u001B[38;5;141m";
    public static final String GREEN = "\u001B[38;5;114m";
    public static final String YELLOW = "\u001B[38;5;221m";
    public static final String GRAY = "\u001B[38;5;246m";
    public static final String RED = "\033[38;2;234;67;53m";
    private static final int TREE_LINE_DELAY_MS = 125;

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void ok(String format, Object... args) {
        System.out.print("[" + GREEN + "OK" + RESET + "] " + String.format(format, args) + RESET);
    }

    public static void fail(String format, Object... args) {
        System.out.print("[" + RED + "FALHA" + RESET + "] " + String.format(format, args) + RESET);
    }

    public static void info(String format, Object... args) {
        System.out.print("[INFO" + RESET + "] " + String.format(format, args) + RESET);
    }

    public static void step(String format, Object... args) {
        System.out.print(BLUE + "\t" + String.format(format, args) + RESET);
    }

    public static void stageHeader(String format, Object... args) {
        System.out.printf(PURPLE + "▸ %s" + RESET + "\n", String.format(format.toUpperCase(), args));
    }

    public static void stageHeaderDebug(Machine machine, String format, Object... args) {
        String buffer = PURPLE + "▸ " + String.format(format.toUpperCase(), args) + RESET;
        String saude = String.format("%d/%d", machine.getSaude(), machine.getSaudeMaxima());
        String chanceFalha = "";
        if (machine.getRawChanceFalha() == machine.getFailureOdd()) {
            chanceFalha = String.format("%.2f", machine.getFailureOdd());
        } else {
            double diffChanceFalha = machine.getFailureOdd() - machine.getRawChanceFalha();
            chanceFalha = String.format("%.2f " + GRAY +"(▴ %.2f)", machine.getFailureOdd(), diffChanceFalha);
        }
        System.out.printf(buffer + " [ " + RED + "❤ %s"+ RESET + " | " + BLUE + "⚂ %s" + RESET + " ]\n", saude, chanceFalha);
    }

    private static void treeLine(String icon, String color, boolean isLast, String format, Object... args) {
        String connector = isLast ? "└─" : "├─";
        System.out.printf("  %s " + color + "%s" + RESET + " %s\n", connector, icon, String.format(format, args));
        try {
            Thread.sleep(TREE_LINE_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void treeOk(boolean isLast, String format, Object... args) {
        treeLine("✓", GREEN, isLast, format, args);
    }

    public static void treeFail(boolean isLast, String format, Object... args) {
        treeLine("✗", RED, isLast, format, args);
    }

    public static void treeInfo(boolean isLast, String format, Object... args) {
        treeLine("ℹ", GRAY, isLast, format, args);
    }

    public static String centerString(int width, String text) {
        if (text == null || text.length() >= width) return text;
        int leftPadding = (width - text.length()) / 2;
        int rightPadding = width - text.length() - leftPadding;
        return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
    }

    public static void card(String format, Object... args) {
        line();
        System.out.printf(ConsolePrinter.BOLD + " %s\n", String.format(format, args));
        line();
    }

    public static void line() {
        System.out.println(GRAY + "─".repeat(65) + RESET);
    }

    public static void line(int size) {
        System.out.println(GRAY + "    " + "─".repeat(size) + RESET);
    }

    public static void printBackOption() {
        System.out.println(GRAY + " 0. ↩ Voltar" + RESET + "\n");
    }

    public static void optionsList(String color, String... options) {
        for (int i = 0; i < options.length; i++) {
            System.out.printf(color + " %d." + RESET + " %s\n", (i + 1), options[i]);
        }
        System.out.println();
        System.out.printf(GRAY + " 0." + RED + " ⏻ " + RESET + "Sair\n\n" + RESET);
    }

    public static void printOneLineStats(double budget, double rawMaterialQuantity, int fabricatedCount) {
        System.out.printf(GRAY + "[ "
                        + GREEN + "Orçamento: R$ %.2f" + GRAY
                        + " | "
                        + BLUE + "Mat. Prima: %.2f kg"  + GRAY
                        + " | "
                        + PURPLE + "Produtos: %d un" + GRAY
                        + " ]\n" + RESET,
                budget,
                rawMaterialQuantity,
                fabricatedCount
        );
    }

    public static void listDemands(ProductionManager pm) {
        List<Demand> demands = pm.getDemands();
        pm.refreshEstimatedCosts();

        System.out.printf("    " + GRAY + "%-16s   %-7s   %-12s %-15s %-18s %-10s" + RESET + "\n",
                "Produto", "Demanda", "MP", "Custo", "Status", "Tempo");
        line(82);

        double totalProjectedCost = 0;
        for (int i = 0; i < demands.size(); i++) {
            Demand demand = demands.get(i);
            double demandCost = demand.getEstimatedCost();
            totalProjectedCost += demandCost;

            /* Padding the plain text to the Status column's visible width yah */
            String statusLabel;
            String statusColor;
            switch (demand.getStatus()) {
                case COMPLETED -> {
                    statusLabel = "Concluída";
                    statusColor = GREEN;
                }
                case PARTIAL -> {
                    statusLabel = String.format("Parcial (%d/%d)", demand.getProducedAmount(), demand.getAmount());
                    statusColor = YELLOW;
                }
                case CANCELLED -> {
                    statusLabel = "Cancelada";
                    statusColor = RED;
                }
                default -> {
                    statusLabel = "Pendente";
                    statusColor = GRAY;
                }
            }
            String status = statusColor + String.format("%-18s", statusLabel) + RESET;
            String time = demand.getStatus() == DemandStatus.PENDING || demand.getStatus() == DemandStatus.CANCELLED
                    ? "-"
                    : String.format("%.2fs", demand.getTotalProductionTime());

            System.out.printf(GRAY + " %-1s." + RESET + " %-16s   %-7s   %-12s R$ %-12.2f %s %-10s" + RESET + "\n",
                    (i + 1),
                    demand.getProductName(),
                    demand.getAmount() + " un",
                    String.format("%.2f kg", demand.getTotalRawMaterial()),
                    demandCost,
                    status,
                    time
            );
        }

        System.out.println();
        if (totalProjectedCost > 0) {
            System.out.printf(" Projeção Total de Orçamento Após Custo Operacional: R$ %.2f " + RED + "(▾ R$ -%.2f)\n" + RESET,
                    pm.getBudget() - totalProjectedCost, totalProjectedCost);
        } else {
            System.out.printf(" Projeção Total de Orçamento Após Custo Operacional: R$ %.2f\n" + RESET, pm.getBudget());
        }
        System.out.println();
    }

    public static void listStorage(List<Demand> demands, List<Product> storage) {
        System.out.printf("    " + GRAY + "%-16s   %-10s" + RESET + "\n", "Produto", "Estoque");
        line(44);

        for (int i = 0; i < demands.size(); i++) {
            String productName = demands.get(i).getProductName();
            int count = 0;
            for (Product p : storage) {
                if (p.getName().equals(productName)) {
                    count++;
                }
            }

            System.out.printf(GRAY + " %-1s." + RESET + " %-16s   %-7s\n" + RESET,
                    (i + 1),
                    productName,
                    count + " un"
            );
        }

        System.out.println();
    }
}