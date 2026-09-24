package view;

import domain.entities.demand.Demand;
import domain.entities.conveyor.Conveyor;
import domain.entities.machine.InspectionMachine;
import domain.entities.machine.Machine;
import domain.entities.machine.PackingMachine;
import domain.entities.machine.ProcessingMachine;
import domain.entities.product.CopoDeVidro;
import domain.entities.product.KitCopoDeVidro;
import domain.entities.product.PoteDeVidro;
import domain.entities.product.Product;
import domain.entities.productionmanager.ProductionManager;
import domain.entities.rawmaterial.RawMaterial;

import java.util.List;
import java.util.Scanner;

public class Menu {
    private final Scanner scanner = new Scanner(System.in);
    private String lastBuffer;
    private final ProductionManager productionManager = new ProductionManager(
            new RawMaterial("Vidro", 50, "kg", 5, 1),
            1000);
    private final StrategyMenu strategyMenu = new StrategyMenu(this, productionManager);

    public void start() {
        boolean running = true;

        printIntroScreen();

        // Produtos hardcoded
        productionManager.addNewProduct(new CopoDeVidro("Copo", 0.5));
        productionManager.addNewProduct(new PoteDeVidro("Pote", 1.2));
        productionManager.addNewProduct(new KitCopoDeVidro("Kit Copo", 3));
        Product.resetIdCounter();

        // Esteira hardcoded
        productionManager.addNewConveyor(new Conveyor("Esteira Ligeira", 20));

        // Máquinas hardcoded
        productionManager.addNewMachine(new ProcessingMachine("Máquina de Processamento", 20, 0.25, 0.78));
        productionManager.addNewMachine(new PackingMachine("Máquina de Empacotamento", 20, 0.15, 0.5));
        productionManager.addNewMachine(new InspectionMachine("Máquina de Inspeção", 20, 0.10, 0.43));

        productionManager.setStrategy(strategyMenu.defaultStrategy());

        while (running) {
            ConsolePrinter.clearScreen();
            ConsolePrinter.card(ConsolePrinter.GRAY + "⌂" + ConsolePrinter.RESET + " FÁBRICA IDEAL");
            System.out.println();

            ConsolePrinter.optionsList(ConsolePrinter.GRAY,
                    ConsolePrinter.YELLOW + "⟳" + ConsolePrinter.RESET + " Atualizar demandas",
                    ConsolePrinter.PURPLE + "⚙" + ConsolePrinter.RESET + " Fabricar demandas",
                    ConsolePrinter.BLUE   + "≡" + ConsolePrinter.RESET + " Ver armazém",
                    ConsolePrinter.GREEN  + "$" + ConsolePrinter.RESET + " Comprar matéria-prima",
                    ConsolePrinter.ORANGE + "⇄" + ConsolePrinter.RESET + " Estratégia de produção",
                    ConsolePrinter.YELLOW + "⚒" + ConsolePrinter.RESET + " Reparar máquinas"
            );

            // Footer
            printFooterBlock();
            int option = readInt("Escolha: ");

            switch (option) {
                case 1 -> updateDemandSubmenu();
                case 2 -> fabricateDemandSubmenu();
                case 3 -> showStorageSubmenu();
                case 4 -> buyRawMaterialSubmenu();
                case 5 -> strategyMenu.show();
                case 6 -> repairSubmenu();
                case 0 -> running = false;
                default -> this.lastBuffer = ConsolePrinter.RED + " Dessa vez não é! Opção inválida!" + ConsolePrinter.RESET;
            }
        }
    }

    private void updateDemandSubmenu() {
        boolean running = true;
        while (running) {
            ConsolePrinter.clearScreen();

            // Header
            ConsolePrinter.card(ConsolePrinter.YELLOW + "⟳" + ConsolePrinter.RESET + " ATUALIZAR DEMANDAS");
            System.out.println();

            // Lista de demandas
            List<Demand> demands = productionManager.getDemands();
            ConsolePrinter.listDemands(productionManager);
            ConsolePrinter.printBackOption();

            // Footer
            printFooterBlock();
            int option = readInt("Qual demanda deseja " + ConsolePrinter.YELLOW + "ATUALIZAR" + ConsolePrinter.RESET + "? ");

            if (option == 0) break;

            if (option > 0 && option <= demands.size()) {
                int newValue = readInt("Digite o novo valor da demanda: ");

                // Aplica novo valor
                Demand selectedDemand = demands.get(option - 1);
                productionManager.updateDemand(selectedDemand, newValue);

                // Log
                this.lastBuffer = String.format(" [" + ConsolePrinter.GREEN + "OK" + ConsolePrinter.RESET + "] Demanda de "
                        + "%s atualizada para %d!", selectedDemand.getProductName(), newValue);
            } else {
                this.lastBuffer = ConsolePrinter.RED + "Dessa vez não é! Opção inválida!" + ConsolePrinter.RESET;
            }
        }
    }

    private void fabricateDemandSubmenu() {
        boolean running = true;
        while (running) {
            ConsolePrinter.clearScreen();

            // Header
            ConsolePrinter.card(ConsolePrinter.PURPLE + "⚙" + ConsolePrinter.RESET + " FABRICAR DEMANDAS");
            System.out.println();

            // Lista as demandas
            List<Demand> demands = productionManager.getDemands();
            ConsolePrinter.listDemands(productionManager);

            // Mostra a próxima demanda escolhida pela estratégia ativa
            int nextByStrategy = demands.size() + 1;
            System.out.printf(ConsolePrinter.GRAY + " %d." + ConsolePrinter.RESET + " " + ConsolePrinter.ORANGE + "▶" + ConsolePrinter.RESET
                            + " Próxima demanda pela estratégia " + ConsolePrinter.ORANGE + "%s" + ConsolePrinter.RESET + "\n\n",
                    nextByStrategy, productionManager.getCurrentStrategy().getStrategyName());
            ConsolePrinter.printBackOption();

            // Footer
            printFooterBlock();
            int option = readInt("Qual demanda deseja " + ConsolePrinter.PURPLE + "FABRICAR" + ConsolePrinter.RESET + "? ");

            if (option == 0) break;

            if (option > 0 && option <= nextByStrategy) {
                try {
                    if (option == nextByStrategy) {
                        productionManager.runNextProduction();
                    } else {
                        productionManager.fabricateDemand(demands.get(option - 1));
                    }

                    System.out.println("\n" + ConsolePrinter.YELLOW + "Pressione ENTER para voltar..." + ConsolePrinter.RESET);
                    scanner.nextLine();
                    scanner.nextLine();
                    running = false;
                } catch (Exception e) {
                    productionManager.setTemMaquinaQuebrada(true);
                    this.lastBuffer = ConsolePrinter.RED + "Dessa vez não é! " + e.getMessage() + ConsolePrinter.RESET;
                }
            } else {
                this.lastBuffer = ConsolePrinter.RED + "Dessa vez não é! Opção inválida!" + ConsolePrinter.RESET;
            }
        }
    }

    private void showStorageSubmenu() {
        boolean running = true;
        while (running) {
            ConsolePrinter.clearScreen();
            ConsolePrinter.card(ConsolePrinter.BLUE + "≡" + ConsolePrinter.RESET + " VER ARMAZÉM");
            System.out.println();

            productionManager.displayStorage();
            ConsolePrinter.printBackOption();
            printFooterBlock();

            int option = readInt("Escolha: ");

            if (option == 0) break;
            else this.lastBuffer = ConsolePrinter.RED + "Dessa vez não é! Opção inválida!" + ConsolePrinter.RESET;
        }
    }

    private void buyRawMaterialSubmenu() {
        boolean running = true;
        while (running) {
            ConsolePrinter.clearScreen();
            ConsolePrinter.card(ConsolePrinter.GREEN + "$" + ConsolePrinter.RESET + " COMPRAR MATÉRIA-PRIMA");
            System.out.println();

            RawMaterial rm = productionManager.getRawMaterial();
            System.out.printf("   Item: " + ConsolePrinter.YELLOW + "%s" + ConsolePrinter.RESET + "\n", rm.getName());
            System.out.printf("   Estoque atual: " + ConsolePrinter.YELLOW + "%.2f %s" + ConsolePrinter.RESET + "\n", rm.getQuantity(), rm.getUnit());
            System.out.printf("   Custo unitário: " + ConsolePrinter.YELLOW + "R$ %.2f / %s" + ConsolePrinter.RESET + "\n\n", rm.getPrice(), rm.getUnit());

            ConsolePrinter.printBackOption();
            printFooterBlock();

            int amount = readInt("Quantos " + rm.getUnit() + " deseja " + ConsolePrinter.GREEN + "COMPRAR" + ConsolePrinter.RESET + "? ");

            if (amount == 0) break;

            if (amount > 0) {
                float totalCost = amount * rm.getPrice();
                System.out.printf("\nVerba projetada: R$ %.2f " + ConsolePrinter.RED + "(▾ R$ -%.2f)\n" + ConsolePrinter.RESET, productionManager.getBudget() - totalCost, totalCost);
                int confirm = readInt("Confirmar compra? [" + ConsolePrinter.GREEN + "1" + ConsolePrinter.RESET + "] Sim / [" + ConsolePrinter.RED + "0" + ConsolePrinter.RESET + "] Não: ");

                if (confirm == 1) {
                    productionManager.buyRawMaterial(amount);
                    System.out.println("\n" + ConsolePrinter.YELLOW + "Pressione ENTER para voltar..." + ConsolePrinter.RESET);
                    scanner.nextLine();
                    scanner.nextLine();
                    running = false;
                } else {
                    this.lastBuffer = ConsolePrinter.YELLOW + "Compra cancelada." + ConsolePrinter.RESET;
                }
            } else {
                this.lastBuffer = ConsolePrinter.RED + "Dessa vez não é! Quantidade inválida!" + ConsolePrinter.RESET;
            }
        }
    }

    private void repairSubmenu() {
        boolean running = true;

        while (running) {
            // Header
            ConsolePrinter.clearScreen();
            ConsolePrinter.card(ConsolePrinter.YELLOW + "⚒" + ConsolePrinter.RESET + " REPARAR MÁQUINAS");
            System.out.println();

            // Lista de máquinas
            int i = 1;
            for (Machine m: productionManager.getMachines()) {
                if (m.precisaManutencao()) {
                    System.out.printf(" %d. %s " + ConsolePrinter.RED + "(quebrada)" + ConsolePrinter.RESET + "\n", i++, m.getName());
                } else {
                    System.out.printf(" %d. %s\n", i++, m.getName());
                }
            }

            // Footer
            System.out.println();
            ConsolePrinter.printBackOption();
            printFooterBlock();

            int option = readInt("Qual máquina você deseja " + ConsolePrinter.YELLOW + "REPARAR" + ConsolePrinter.RESET + "? ");

            if (option == 0) break;

            try {
                // Processo de reparo
                Machine chosenMachine = productionManager.getMachines().get(option - 1);
                productionManager.repairMachine(chosenMachine);
                animateRepair(chosenMachine.getName()); // Animação

                this.lastBuffer = ConsolePrinter.GREEN + chosenMachine.getName().toUpperCase() + " reparada!" + ConsolePrinter.RESET;
            } catch (IndexOutOfBoundsException e) {
                this.lastBuffer = ConsolePrinter.RED + "Dessa vez não é! Máquina inválida!" + ConsolePrinter.RESET;
            } catch (Exception e) {
                this.lastBuffer = ConsolePrinter.RED + e.getMessage() + ConsolePrinter.RESET;
            }
        }
    }

    /**
     * Imprime uma nova linha com uma animação de um bloco se mexendo
     * para simular o reparo em andamento.
     */
    private void animateRepair(String machineName) {
        System.out.println();
        int travelDistance = 5; // Distância que o bloco vai percorrer
        int loops = 3;

        try {
            for (int loop = 0; loop < loops; loop++) {
                for (int i = 0; i <= travelDistance; i++) {
                    String spaces = " ".repeat(i);
                    String trail = " ".repeat(travelDistance - i); // Serve para tampar os blocos das iterações anteriores

                    System.out.print("\rReparando " + machineName + " [" + ConsolePrinter.YELLOW + spaces + "█" + trail + ConsolePrinter.RESET + "]");
                    Thread.sleep(150);
                }
            }
        } catch (InterruptedException e) {
            // Try-catch obrigatório por conta do Thread.sleep()
            Thread.currentThread().interrupt();
        }
    }

    private void printIntroScreen() {
        ConsolePrinter.clearScreen();
        ConsolePrinter.line();
        System.out.println(ConsolePrinter.BOLD + ConsolePrinter.ORANGE
                + ConsolePrinter.centerString(65, "FÁBRICA IDEAL") + ConsolePrinter.RESET);
        System.out.println(ConsolePrinter.GRAY
                + ConsolePrinter.centerString(65, "\"E, sim, cliente ganhou um balão de presente! :D\"")
                + ConsolePrinter.RESET);
        ConsolePrinter.line();
        System.out.println();
        System.out.println("   Bem-vindos à " + ConsolePrinter.ORANGE + "Fábrica Ideal" + ConsolePrinter.RESET + "! Aqui cliente não pediu");
        System.out.println("   150 kg de vidro e, sim, ganha produtos ótimos de presente!");
        System.out.println("   Os balões de presente ficam no " + ConsolePrinter.BLUE + "@merc.adinhoideall" + ConsolePrinter.RESET + ", nosso");
        System.out.println("   fiel cliente!");
        System.out.println();
        System.out.println("   " + ConsolePrinter.GREEN + "Desenvolvido por:" + ConsolePrinter.RESET);
        System.out.println("     • Heitor Almeida    " + ConsolePrinter.GRAY + "RA: 245293" + ConsolePrinter.RESET);
        System.out.println("     • Glayson Oliveira  " + ConsolePrinter.GRAY + "RA: 281213" + ConsolePrinter.RESET);
        System.out.println();
        ConsolePrinter.line();
        System.out.println();
        System.out.println(ConsolePrinter.YELLOW + "Pressione ENTER para começar..." + ConsolePrinter.RESET);
        scanner.nextLine();
    }

    int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            }
            scanner.next();
            System.out.println(ConsolePrinter.RED + "Dessa vez não é! Digite um número." + ConsolePrinter.RESET);
        }
    }

    void setLastBuffer(String message) {
        this.lastBuffer = message;
    }

    void printFooterBlock() {
        if (this.lastBuffer != null) {
            ConsolePrinter.card(this.lastBuffer);
            this.lastBuffer = null;
        } else {
            ConsolePrinter.line();
        }
        productionManager.displayBudget();
        System.out.println();
    }
}