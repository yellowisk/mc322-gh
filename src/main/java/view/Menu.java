package view;

import domain.entities.demand.Demand;
import domain.entities.conveyor.Conveyor;
import domain.entities.machine.InspectionMachine;
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

        while (running) {
            ConsolePrinter.clearScreen();
            ConsolePrinter.card(ConsolePrinter.GRAY + "⌂" + ConsolePrinter.RESET + " FÁBRICA IDEAL");
            System.out.println();

            ConsolePrinter.optionsList(ConsolePrinter.GRAY,
                    ConsolePrinter.YELLOW + "⟳" + ConsolePrinter.RESET + " Atualizar demandas",
                    ConsolePrinter.PURPLE + "⚙" + ConsolePrinter.RESET + " Fabricar demandas",
                    ConsolePrinter.BLUE + "≡" + ConsolePrinter.RESET + " Ver armazém",
                    ConsolePrinter.GREEN + "$" + ConsolePrinter.RESET + " Comprar matéria-prima"
            );

            // Footer
            printFooterBlock();
            int option = readInt("Escolha: ");

            switch (option) {
                case 1 -> updateDemandSubmenu();
                case 2 -> fabricateDemandSubmenu();
                case 3 -> showStorageSubmenu();
                case 4 -> buyRawMaterialSubmenu();
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
            ConsolePrinter.printBackOption();

            // Footer
            printFooterBlock();
            int option = readInt("Qual demanda deseja " + ConsolePrinter.PURPLE + "FABRICAR" + ConsolePrinter.RESET + "? ");

            if (option == 0) break;

            if (option > 0 && option <= demands.size()) {
                Demand chosenDemand = productionManager.getDemands().get(option - 1);
                productionManager.fabricateDemand(chosenDemand);

                System.out.println("\n" + ConsolePrinter.YELLOW + "Pressione ENTER para voltar..." + ConsolePrinter.RESET);
                scanner.nextLine();
                scanner.nextLine();
                running = false;
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

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            }
            scanner.next();
            System.out.println(ConsolePrinter.RED + "Dessa vez não é! Digite um número." + ConsolePrinter.RESET);
        }
    }

    private void printFooterBlock() {
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