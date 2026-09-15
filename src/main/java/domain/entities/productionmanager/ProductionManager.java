package domain.entities.productionmanager;

import domain.entities.demand.Demand;
import domain.entities.machines.conveyor.Conveyor;
import domain.entities.machines.machine.Machine;
import domain.entities.product.Product;
import domain.entities.product.ProductStatus;
import domain.entities.rawmaterial.RawMaterial;
import presentation.console.ConsolePrinter;

import java.util.ArrayList;
import java.util.List;

public class ProductionManager {
    private List<Demand> demands = new ArrayList<>();
    private List<Product> fabricatedProducts = new ArrayList<>();
    private List<Product> availableProducts = new ArrayList<>();
    private List<Machine> machines = new ArrayList<>();
    private Conveyor conveyor;
    private Product chosenProduct;
    private RawMaterial rawMaterial;
    private float budget;

    public ProductionManager(RawMaterial rawMaterial, float budget) {
        this.rawMaterial = rawMaterial;
        this.budget = budget;
    }

    public void addNewProduct(Product newProduct) {
        this.availableProducts.add(newProduct);
        this.registerDemand(newProduct, 0); // init the product demand
    }

    public void registerDemand(Product product, int amount) {
        this.demands.add(new Demand(product, amount));
    }

    public void updateDemand(Demand demand, int newValue) {
        demand.setAmount(newValue);
    }

    public void addNewMachine(Machine machine) {
        this.machines.add(machine);
    }

    public void addNewConveyor(Conveyor conveyor) {
        this.conveyor = conveyor;
    }

    public void buyRawMaterial(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("You cant buy a negative amount :/");
        }

        float totalCost = amount * this.rawMaterial.getPrice();
        if (this.budget < totalCost) {
            ConsolePrinter.fail("Orçamento insuficiente para comprar matéria-prima!\n");
            return;
        }
        this.budget -= totalCost;
        this.rawMaterial.addStock(amount);
        ConsolePrinter.ok("Compra de matéria-prima realizada com sucesso!\n");
    }

    public void fabricateDemand(Demand demand) {
        this.chosenProduct = this.getProductByName(demand.getProductName());

        if (this.chosenProduct == null) {
            throw new IllegalArgumentException("Produto não encontrado.");
        }

        // Liga as máquinas
        this.conveyor.turnOn();
        for (Machine m : this.machines) {
            m.turnOn();
        }

        System.out.printf("Iniciando produção de: %s\n", this.chosenProduct.getName());

        int productsRemaining = demand.getAmount();
        int fabricatedAmount = 0;
        int approvedAmount = 0;

        while (fabricatedAmount < productsRemaining) {
            System.out.printf("─[ %d / %d ] %s " + "─".repeat(24) + "\n",
                    (fabricatedAmount + 1), productsRemaining, this.chosenProduct.getName());

            ProductionStages currentStage;
            Machine currentMachine;
            Product currentProduct;

            // 1. Processamento
            currentStage = ProductionStages.PROCESSING;
            ConsolePrinter.stage(currentStage.getName());

            currentMachine = this.machines.get(currentStage.getCode());
            if (!this.calcProductionCost(currentMachine)) {
                ConsolePrinter.fail("Orçamento insuficiente para operar a " + currentMachine.getName() + "!\n");
                break;
            }

            this.conveyor.addRawMaterial(this.chosenProduct.getRawMaterialPerUnit());
            ConsolePrinter.step("%s carregado para a esteira.\n", this.rawMaterial.getName());

            this.rawMaterial.consume(this.conveyor.removeRawMaterial());
            ConsolePrinter.step("%s transportado até a máquina de processamento.\n", this.rawMaterial.getName());
            ConsolePrinter.step("Máquina processando %d %s de %s...\n", this.chosenProduct.getRawMaterialPerUnit(), this.rawMaterial.getUnit(), this.rawMaterial.getName());
            currentProduct = currentMachine.process(this.chosenProduct);
            ConsolePrinter.step("Produto %s #%d criado.\n", currentProduct.getName(), currentProduct.getId());

            // 2. Empacotamento
            currentStage = ProductionStages.PACKAGING;
            ConsolePrinter.stage(currentStage.getName());

            currentMachine = this.machines.get(currentStage.getCode());
            if (!this.calcProductionCost(currentMachine)) {
                ConsolePrinter.fail("Orçamento insuficiente para operar a " + currentMachine.getName() + "!\n");
                break;
            }

            this.conveyor.addProduct(currentProduct);
            ConsolePrinter.step("%s #%d carregado para a esteira.\n", currentProduct.getName(), currentProduct.getId());
            ConsolePrinter.step("%s #%d transportado(a) até a máquina de empacotamento.\n", currentProduct.getName(), currentProduct.getId());
            ConsolePrinter.step("Máquina empacotando %s #%d...\n", currentProduct.getName(), currentProduct.getId());
            currentProduct = currentMachine.process(this.conveyor.removeProduct());
            ConsolePrinter.step("Produto %s #%d empacotado.\n", currentProduct.getName(), currentProduct.getId());

            // 3. Inspeção
            currentStage = ProductionStages.INSPECTION;
            ConsolePrinter.stage(currentStage.getName());

            currentMachine = this.machines.get(currentStage.getCode());
            if (!this.calcProductionCost(currentMachine)) {
                ConsolePrinter.fail("Orçamento insuficiente para operar a " + currentMachine.getName() + "!\n");
                break;
            }

            this.conveyor.addProduct(currentProduct);
            ConsolePrinter.step("%s #%d carregado para a esteira.\n", currentProduct.getName(), currentProduct.getId());
            ConsolePrinter.step("%s #%d transportado(a) até a máquina de inspeção.\n", currentProduct.getName(), currentProduct.getId());
            ConsolePrinter.step("Máquina inspecionando %s #%d...\n", currentProduct.getName(), currentProduct.getId());
            currentProduct = currentMachine.process(this.conveyor.removeProduct());
            this.conveyor.addProduct(currentProduct);
            ConsolePrinter.step("Produto %s #%d inspecionado.\n", currentProduct.getName(), currentProduct.getId());

            if (currentProduct.getStatus() == ProductStatus.APPROVED) {
                this.fabricatedProducts.add(this.conveyor.removeProduct());
                ConsolePrinter.step("O produto %s #%d " + ConsolePrinter.GREEN + "foi aprovado" + ConsolePrinter.RESET + " e enviado ao armazém!\n", currentProduct.getName(), currentProduct.getId());
                approvedAmount++;
            } else {
                ConsolePrinter.step("O produto %s #%d " + ConsolePrinter.RED + "foi rejeitado" + ConsolePrinter.RESET + " inspeção e descartado.\n", currentProduct.getName(), currentProduct.getId());
                this.conveyor.removeProduct();
            }

            fabricatedAmount++;
        }

        ConsolePrinter.card("%d produtos fabricados e %d aprovados.", fabricatedAmount, approvedAmount);

        this.conveyor.turnOff();
        for (Machine m : this.machines) {
            m.turnOff();
        }
    }

    private boolean calcProductionCost(Machine machine) {
        if (this.budget >= machine.getOperationCost()) {
            this.budget -= machine.getOperationCost();
            return true;
        }
        return false;
    }

    public Product getProductByName(String productName) {
        for (Product p : this.availableProducts) {
            if (p.getName().equalsIgnoreCase(productName)) {
                return p;
            }
        }
        return null;
    }

    public List<Demand> getDemands() {
        return this.demands;
    }

    public List<Product> getFabricatedProducts() {
        return this.fabricatedProducts;
    }

    public List<Machine> getMachines() {
        return this.machines;
    }

    public Product getChosenProduct() {
        return this.chosenProduct;
    }

    public void setChosenProduct(Product chosenProduct) {
        this.chosenProduct = chosenProduct;
    }

    public RawMaterial getRawMaterial() {
        return this.rawMaterial;
    }

    public Conveyor getConveyor() {
        return this.conveyor;
    }

    public float getBudget() {
        return this.budget;
    }
}