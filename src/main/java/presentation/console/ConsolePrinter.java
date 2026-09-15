package presentation.console;

import domain.entities.demand.Demand;
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
        System.out.print("[ INFO" + RESET + "] " + String.format(format, args) + RESET);
    }

    public static void step(String format, Object... args) {
        System.out.print("\t├─ " + String.format(format, args) + RESET);
    }

    public static void stage(String format, Object... args) {
        System.out.print("▸ Etapa iniciada: " + PURPLE + String.format(format, args).toUpperCase() + RESET + "\n");
    }

    public static String centerString(int width, String text) {
        if (text == null || text.length() >= width) return text;
        int leftPadding = (width - text.length()) / 2;
        int rightPadding = width - text.length() - leftPadding;
        return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
    }

    public static void card(String color, String format, Object... args) {
        line();
        System.out.printf(color + " %s" + RESET + "\n", String.format(format, args));
        line();
    }

    public static void card(String format, Object... args) {
        line();
        System.out.printf(ConsolePrinter.BOLD + " %s\n", String.format(format, args));
        line();
    }

    public static void line() {
        System.out.println(BOLD + "─".repeat(65) + RESET);
    }

    public static void line(String color, int size) {
        System.out.println(color + "    " + "─".repeat(size) + RESET);
    }

    public static void printBackOption() {
        System.out.println(GRAY + " 0. ↩ Voltar" + RESET + "\n");
    }

    public static void optionsList(String color, String... options) {
        for (int i = 0; i < options.length; i++) {
            System.out.printf(color + " %d." + RESET + " %s\n", (i + 1), options[i]);
        }
        System.out.println();
        System.out.printf(GRAY + " 0." + RESET + " ↩ Sair\n\n" + RESET);
    }

    public static void printOneLineStats(ProductionManager pm) {
        System.out.printf(GRAY + "[ "
                        + GREEN + "Budget: R$ %.2f" + GRAY
                        + " | "
                        + BLUE + "Mat. Prima: %d kg" + GRAY
                        + " | "
                        + PURPLE + "Produtos: %d un" + GRAY
                        + " ]\n" + RESET,
                pm.getBudget(),
                pm.getRawMaterial().getQuantity(),
                pm.getFabricatedProducts().size()
        );
    }

    public static void listDemands(ProductionManager pm) {
        List<Demand> demands = pm.getDemands();
        double unitOperationCost = 0;

        for (int i = 0; i < pm.getMachines().size(); i++) {
            unitOperationCost += pm.getMachines().get(i).getOperationCost();
        }

        System.out.printf("    " + GRAY + "%-16s   %-7s   %-7s %-15s" + RESET + "\n", "Produto", "Demanda", "MP", "Custo");
        line(GRAY, 44);

        double totalProjectedCost = 0;
        for (int i = 0; i < demands.size(); i++) {
            Demand demand = demands.get(i);
            double demandCost = demand.getAmount() * unitOperationCost;
            totalProjectedCost += demandCost;

            System.out.printf(GRAY + " %-1s." + RESET + " %-16s   %-7s   %-5s   R$ %-12.2f" + RESET + "\n",
                    (i + 1),
                    demand.getProductName(),
                    demand.getAmount() + " un",
                    demand.getTotalRawMaterial() + " kg",
                    demandCost
            );
        }

        System.out.println();
        if (totalProjectedCost > 0) {
            System.out.printf(" Projeção Total de Custo Operacional: R$ %.2f " + RED + "(▾ R$ -%.2f)\n" + RESET,
                    pm.getBudget() - totalProjectedCost, totalProjectedCost);
        } else {
            System.out.printf(" Projeção Total de Custo Operacional: R$ %.2f\n" + RESET, pm.getBudget());
        }
        System.out.println();
    }
}