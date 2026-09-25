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

    private static final String DEMAND_ROW_FORMAT = "%-16s  %-12s  %-16s  %-16s  %-18s  %-10s";
    private static final int DEMAND_TABLE_WIDTH = 16 + 12 + 16 + 16 + 18 + 10 + 2 * 5;

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static String color(String color, String text) {
        return color + text + RESET;
    }

    /** Success log, returned instead of printed (for the lastBuffer) */
    public static String okText(String format, Object... args) {
        return color(GREEN, "✓") + " " + String.format(format, args) + RESET;
    }

    /** Fail log , returned instead of printed (for the lastBuffer) */
    public static String failText(String format, Object... args) {
        return color(RED, "✗") + " " + String.format(format, args) + RESET;
    }

    public static void ok(String format, Object... args) {
        System.out.print(okText(format, args));
    }

    public static void fail(String format, Object... args) {
        System.out.print(failText(format, args));
    }

    /** Stage header w/ the machine's health and failure odds */
    public static void stageHeader(Machine machine, String format, Object... args) {
        String title = PURPLE + "▸ " + String.format(format.toUpperCase(), args) + RESET;
        String saude = String.format("%d/%d", machine.getSaude(), machine.getSaudeMaxima());
        String chanceFalha = "";
        if (machine.getRawChanceFalha() == machine.getFailureOdd()) {
            chanceFalha = String.format("%.2f", machine.getFailureOdd());
        } else {
            double diffChanceFalha = machine.getFailureOdd() - machine.getRawChanceFalha();
            chanceFalha = String.format("%.2f " + GRAY +"(▴ %.2f)", machine.getFailureOdd(), diffChanceFalha);
        }
        System.out.printf(title + " [ " + RED + "❤ %s"+ RESET + " | " + BLUE + "⚂ %s" + RESET + " ]\n", saude, chanceFalha);
    }

    private static void treeLine(String icon, String color, boolean isLast, String format, Object... args) {
        String connector = isLast ? "└─" : "├─";
        System.out.printf("  %s " + color + "%s" + RESET + " %s\n", connector, icon, String.format(format, args));
        pause(TREE_LINE_DELAY_MS);
    }

    /* Segura o terminal por um instante pro usuário conseguir ler a mensagem */
    public static void pause(int millis) {
        try {
            Thread.sleep(millis);
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

    /** Indented line, goes right under the table headers */
    public static void indentedLine(int size) {
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
        double totalProjectedCost = pm.getProjectedCost(); // also refreshes each demand's cost btw

        System.out.printf("    " + GRAY + DEMAND_ROW_FORMAT + RESET + "\n",
                "Produto", "Demanda", "MP", "Custo", "Status", "Tempo");
        indentedLine(DEMAND_TABLE_WIDTH);

        for (int i = 0; i < demands.size(); i++) {
            Demand demand = demands.get(i);
            double demandCost = demand.getEstimatedCost();

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

            // status already comes colored + padded, so it goes in as a plain %s instead of %-18s
            System.out.printf(GRAY + " %-1s." + RESET + " " + DEMAND_ROW_FORMAT.replace("%-18s", "%s") + RESET + "\n",
                    (i + 1),
                    demand.getProductName(),
                    demand.getAmount() + " un",
                    String.format("%.2f kg", demand.getTotalRawMaterial()),
                    String.format("R$ %.2f", demandCost),
                    status,
                    time
            );
        }

        System.out.println();
        if (totalProjectedCost > 0) {
            double projectedBudget = pm.getBudget() - totalProjectedCost;
            System.out.printf(" Projeção Total de Orçamento Após Custo Operacional: R$ %.2f " + RED + "(▾ R$ -%.2f)" + RESET + "\n",
                    projectedBudget, totalProjectedCost);
            if (projectedBudget < 0) {
                System.out.println("\n" + color(RED, " ⚠ Orçamento insuficiente para fabricar todas as demandas pendentes!"));
            }
        } else {
            System.out.printf(" Projeção Total de Orçamento Após Custo Operacional: R$ %.2f\n" + RESET, pm.getBudget());
        }
        System.out.println();
    }

    public static void listStorage(List<Demand> demands, List<Product> storage) {
        System.out.printf("    " + GRAY + "%-16s   %-10s" + RESET + "\n", "Produto", "Estoque");
        indentedLine(44);

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