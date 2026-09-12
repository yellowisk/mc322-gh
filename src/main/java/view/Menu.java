package view;

import domain.entities.demand.Demand;
import domain.entities.machines.conveyor.Conveyor;
import domain.entities.machines.machine.InspectionMachine;
import domain.entities.machines.machine.Machine;
import domain.entities.machines.machine.PackingMachine;
import domain.entities.machines.machine.ProcessingMachine;
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

        private final ProductionManager productionManager = new ProductionManager(
                new RawMaterial("Vidro", 50, "kg", 5, 1), // R$ 1,00
                1000);

    public void start() {
        boolean running = true;

        // Produtos hardcoded
        productionManager.addNewProduct(new CopoDeVidro("Copo", 5));
        productionManager.addNewProduct(new PoteDeVidro("Pote", 10));
        productionManager.addNewProduct(new KitCopoDeVidro("Kit Copo", 15));

        productionManager.addNewConveyor(new Conveyor("Esteira", 20));

        productionManager.addNewMachine(new ProcessingMachine("Máquina de Processamento", 20, 0.3, 34.78));
        productionManager.addNewMachine(new PackingMachine("Máquina de Empacotamento", 20, 0.2, 7.5));
        productionManager.addNewMachine(new InspectionMachine("Máquina de Inspecionamento", 20, 0.1, 15.33));

        while (running) {
            System.out.println("\nTELA 1: ESCOLHER FUNCIONALIDADE");
            System.out.println("[1] Atualizar demandas");
            System.out.println("[2] Fabricar demandas");
            System.out.println("[3] Ver armazém");
            System.out.println("[4] Comprar matéria-prima");
            System.out.println("[0] Sair");

            System.out.print("Escolha: ");

            int option = scanner.nextInt();
            switch (option) {
                case 1 -> updateDemandSubmenu();
                case 2 -> fabricateDemandSubmenu();
                case 3 -> showStorageSubmenu();
                case 4 -> buyRawMaterialSubmenu();
                case 0 -> running = false;
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    private void updateDemandSubmenu() {
        boolean running = true;
        while (running) {
            List<Demand> demands = productionManager.getDemands();

            System.out.println("\nTELA 2: ATUALIZAR DEMANDAS");
            int i = 1;
            for (Demand demand : demands) {
                System.out.printf("[%d] %s (Demanda atual: %d)\n", i++, demand.getProductName(), demand.getAmount());
            }

            System.out.println("Qual demanda deseja ATUALIZAR? (0 para voltar): ");
            int option = scanner.nextInt();

            if (option == 0) {
                break;
            }

            System.out.print("Digite o novo valor da demanda: ");
            int newValue = scanner.nextInt();

            // Atualiza o objeto dentro da lista
            Demand selectedDemand = demands.get(option - 1);
            selectedDemand.setAmount(newValue);

            System.out.println("Demanda atualizada com sucesso!");
            running = false; // Retorna ao menu principal
        }
    }

    private void fabricateDemandSubmenu() {
        boolean running = true;

        productionManager.getConveyor().turnOn();
        for (Machine m: productionManager.getMachines()) {
            m.turnOn();
        }

        while (running) {
            List<Demand> demands = productionManager.getDemands();

            System.out.println("\nTELA 3: FABRICAR DEMANDAS");
            int i = 1;
            for (Demand demand : demands) {
                System.out.printf("[%d] %s (Demanda atual: %d)\n", i++, demand.getProductName(), demand.getAmount());
            }

            System.out.println("Qual demanda deseja FABRICAR? (0 para voltar): ");
            int option = scanner.nextInt();

            if (option == 0) {
                break; // Retorna ao menu principal
            }

            Demand chosenDemand = productionManager.getDemands().get(option - 1);
            productionManager.fabricateDemand(chosenDemand);

            System.out.println("Demanda fabricada com sucesso!");
            running = false; // Retorna ao menu principal
        }

        productionManager.getConveyor().turnOff();
        for (Machine m: productionManager.getMachines()) {
            m.turnOff();
        }
    }

    private void showStorageSubmenu() {
        boolean running = true;
        System.out.println("Ainda não implementado.");
    }

    private void buyRawMaterialSubmenu() {
        boolean running = true;
        System.out.println("Ainda não implementado.");
    }

}
