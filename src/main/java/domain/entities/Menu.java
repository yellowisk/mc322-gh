package domain.entities;

import domain.entities.product.Product;
import domain.entities.machines.machine.Machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// 1. Classe de Entidade Demand
class Demand {
    private String productName;
    private int amount;
    private boolean isDone;

    public Demand(String productName, int amount) {
        this.productName = productName;
        this.amount = amount;
        this.isDone = false;
    }

    public String getProductName() { return productName; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }
}

// 2. Gerenciador de Produção (Estado da Aplicação)
class ProductionManager {
    private List<Demand> demands = new ArrayList<>();
    private List<Product> fabricatedProducts = new ArrayList<>();
    private List<Machine> machines = new ArrayList<>();
    private Product chosenProduct;

    public List<Demand> getDemands() { return demands; }

    public void fabricateDemand(Demand demand) {
        System.out.println("Produto fabricado com sucesso.");
    }

    public void registerDemand(Product produto, int quantity) {
        demands.add(new Demand(produto.getName(), quantity));
    }
}

// 3. Classe do Menu
public class Menu {
    private final Scanner scanner = new Scanner(System.in);

    private final ProductionManager productionManager = new ProductionManager();
    private List<Product> availableProducts = new ArrayList<Product>();

    public void start() {
        boolean running = true;

        // Produtos hardcoded
        availableProducts.add(new Product("Prod1", 5));
        availableProducts.add(new Product("Prod2", 10));
        availableProducts.add(new Product("Prod3", 15));

        // Inicialização das demandas
        for (Product product : availableProducts) {
            productionManager.registerDemand(product, 0);
        }

        while (running) {
            System.out.println("\nTELA 1: ESCOLHER FUNCIONALIDADE");
            System.out.println("[1] Atualizar demandas");
            System.out.println("[2] Fabricar demandas");
            System.out.println("[3] Consultar armazém");
            System.out.println("[4] Comprar matéria-prima");
            System.out.println("[0] Sair");

            System.out.print("Escolha: ");

            int option = scanner.nextInt();
            switch (option) {
                case 1 -> updateDemandSubmenu();
                case 2 -> fabricateDemandSubmenu();
                case 3 -> showStorageSubmenu();
                case 4 -> System.out.println("Submenu Matéria-Prima (Em breve)");
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

            System.out.println("Qual demanda deseja atualizar? (0 para voltar): ");
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
        while (running) {
            List<Demand> demands = productionManager.getDemands();

            System.out.println("\nTELA 3: FABRICAR DEMANDAS");
            int i = 1;
            for (Demand demand : demands) {
                System.out.printf("[%d] %s (Demanda atual: %d)\n", i++, demand.getProductName(), demand.getAmount());
            }
            System.out.println("Qual demanda deseja fabricar? (0 para voltar): ");
            Demand chosenDemand = demands.get((scanner.nextInt() - 1));
            productionManager.fabricateDemand(chosenDemand);
            running = false;
        }
    }

    private void showStorageSubmenu() {
        boolean running = true;
    }

    public static void main(String[] args) {
        new Menu().start();
    }
}
