package view;

import domain.entities.demand.Demand;
import domain.entities.machines.conveyor.Conveyor;
import domain.entities.machines.machine.InspectionMachine;
import domain.entities.machines.machine.PackingMachine;
import domain.entities.machines.machine.ProcessingMachine;
import domain.entities.product.CopoDeVidro;
import domain.entities.product.KitCopoDeVidro;
import domain.entities.product.PoteDeVidro;
import domain.entities.product.Product;
import domain.entities.productionmanager.ProductionManager;
import domain.entities.rawmaterial.RawMaterial;
import presentation.console.ConsolePrinter;

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

        // Produtos hardcoded
        productionManager.addNewProduct(new CopoDeVidro("Copo", 5));
        productionManager.addNewProduct(new PoteDeVidro("Pote", 10));
        productionManager.addNewProduct(new KitCopoDeVidro("Kit Copo", 15));


        // Esteira hardcoded
        productionManager.addNewConveyor(new Conveyor("Esteira", 20));

        // Máquinas hardcoded
        productionManager.addNewMachine(new ProcessingMachine("Máquina de Processamento", 20, 0.25, 0.78));
        productionManager.addNewMachine(new PackingMachine("Máquina de Empacotamento", 20, 0.15, 0.5));
        productionManager.addNewMachine(new InspectionMachine("Máquina de Inspecionamento", 20, 0.10, 0.43));

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
            System.out.print("Escolha: ");
            int option = scanner.nextInt();

            switch (option) {
                case 1 -> updateDemandSubmenu();
                case 2 -> fabricateDemandSubmenu();
                case 3 -> showStorageSubmenu();
                case 4 -> buyRawMaterialSubmenu();
                case 0 -> running = false;
                default -> this.lastBuffer = ConsolePrinter.RED + "Opção inválida!" + ConsolePrinter.RESET;
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
            System.out.print("Qual demanda deseja " + ConsolePrinter.YELLOW + "ATUALIZAR" + ConsolePrinter.RESET + "? ");
            int option = scanner.nextInt();

            if (option == 0) break;

            if (option > 0 && option <= demands.size()) {
                System.out.print("Digite o novo valor da demanda: ");
                int newValue = scanner.nextInt();

                // Aplica novo valor
                Demand selectedDemand = demands.get(option - 1);
                selectedDemand.setAmount(newValue);
                Product demandProduct = productionManager.getProductByName(selectedDemand.getProductName());
                selectedDemand.setTotalRawMaterial(demandProduct.getRawMaterialPerUnit() * selectedDemand.getAmount());

                // Log
                this.lastBuffer = String.format(" [" + ConsolePrinter.GREEN + "OK" + ConsolePrinter.RESET + "] Demanda de "
                        + "%s atualizada para %d!", selectedDemand.getProductName(), newValue);
            } else {
                this.lastBuffer = ConsolePrinter.RED + "Opção inválida!" + ConsolePrinter.RESET;
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
            System.out.print("Qual demanda deseja " + ConsolePrinter.PURPLE + "FABRICAR" + ConsolePrinter.RESET + "? ");
            int option = scanner.nextInt();

            if (option == 0) break;

            if (option > 0 && option <= demands.size()) {
                Demand chosenDemand = productionManager.getDemands().get(option - 1);
                productionManager.fabricateDemand(chosenDemand);

                System.out.println("\n" + ConsolePrinter.YELLOW + "Pressione ENTER para voltar..." + ConsolePrinter.RESET);
                scanner.nextLine();
                scanner.nextLine();
                running = false;
            } else {
                this.lastBuffer = ConsolePrinter.RED + "Opção inválida!" + ConsolePrinter.RESET;
            }
        }
    }

    private void showStorageSubmenu() {
        boolean running = true;
        while (running) {
            ConsolePrinter.clearScreen();
            ConsolePrinter.card(ConsolePrinter.BLUE + "≡" + ConsolePrinter.RESET + " VER ARMAZÉM");
            System.out.println();

            RawMaterial rm = productionManager.getRawMaterial();
            System.out.printf("   Estoque de Matéria-Prima: " + ConsolePrinter.GREEN + "%d %s\n\n" + ConsolePrinter.RESET, rm.getQuantity(), rm.getUnit());

            List<Product> storage = productionManager.getFabricatedProducts();
            System.out.println("   " + ConsolePrinter.ORANGE + "Produtos no Armazém:" + ConsolePrinter.RESET);

            for (Demand d : productionManager.getDemands()) {
                String pName = d.getProductName();
                int count = 0;
                for (Product p : storage) {
                    if (p.getName().equals(pName)) {
                        count++;
                    }
                }
                System.out.printf("   - %-15s : %d unidades\n", pName, count);
            }

            System.out.println();

            ConsolePrinter.printBackOption();
            printFooterBlock();

            System.out.print("Escolha: ");
            int option = scanner.nextInt();

            if (option == 0) break;
            else this.lastBuffer = ConsolePrinter.RED + "Opção inválida!" + ConsolePrinter.RESET;
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
            System.out.printf("   Estoque atual: " + ConsolePrinter.YELLOW + "%d %s" + ConsolePrinter.RESET + "\n", rm.getQuantity(), rm.getUnit());
            System.out.printf("   Custo unitário: " + ConsolePrinter.YELLOW + "R$ %.2f / %s" + ConsolePrinter.RESET + "\n\n", rm.getPrice(), rm.getUnit());

            ConsolePrinter.printBackOption();
            printFooterBlock();

            System.out.print("Quantos " + rm.getUnit() + " deseja " + ConsolePrinter.GREEN + "COMPRAR" + ConsolePrinter.RESET + "? ");
            int amount = scanner.nextInt();

            if (amount == 0) break;

            if (amount > 0) {
                float totalCost = amount * rm.getPrice();
                System.out.printf("\nVerba projetada: R$ %.2f " + ConsolePrinter.RED + "(▾ R$ -%.2f)\n" + ConsolePrinter.RESET, productionManager.getBudget() - totalCost, totalCost);
                System.out.print("Confirmar compra? [" + ConsolePrinter.GREEN + "1" + ConsolePrinter.RESET + "] Sim / [" + ConsolePrinter.RED + "0" + ConsolePrinter.RESET + "] Não: ");
                int confirm = scanner.nextInt();

                if (confirm == 1) {
                    ConsolePrinter.clearScreen();
                    productionManager.buyRawMaterial(amount);
                    System.out.println("\n" + ConsolePrinter.YELLOW + "Pressione ENTER para continuar..." + ConsolePrinter.RESET);
                    scanner.nextLine();
                    scanner.nextLine();
                } else {
                    this.lastBuffer = ConsolePrinter.YELLOW + "Compra cancelada." + ConsolePrinter.RESET;
                }
            } else {
                this.lastBuffer = ConsolePrinter.RED + "Quantidade inválida!" + ConsolePrinter.RESET;
            }
        }
    }

    private void printFooterBlock() {
        if (this.lastBuffer != null) {
            ConsolePrinter.card(this.lastBuffer);
            this.lastBuffer = null;
        } else {
            ConsolePrinter.line();
        }
        ConsolePrinter.printOneLineStats(productionManager);
        System.out.println();
    }
}