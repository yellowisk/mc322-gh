package domain.entities.productionmanager;

import domain.entities.demand.Demand;
import domain.entities.conveyor.Conveyor;
import domain.entities.machine.Machine;
import domain.entities.product.Product;
import domain.entities.product.ProductStatus;
import domain.entities.rawmaterial.RawMaterial;
import view.ConsolePrinter;

import java.util.ArrayList;
import java.util.List;

public class ProductionManager {
    private List<Demand> demands = new ArrayList<Demand>();
    private List<Product> fabricatedProducts = new ArrayList<Product>();
    private List<Product> availableProducts = new ArrayList<Product>();
    private List<Machine> machines = new ArrayList<Machine>();
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
        registerDemand(newProduct, 0); // init the product demand
    }

    public void registerDemand(Product product, int amount) {
        this.demands.add(new Demand(product, amount));
    }

    public void updateDemand(Demand demand, int newValue) {
        demand.setAmount(newValue);
        Product product = getProductByName(demand.getProductName());
        demand.setTotalRawMaterial(product.getRawMaterialPerUnit() * demand.getAmount());
        demand.reset();
    }

    public Product getProductByName(String productName) {
        for (Product p: availableProducts) {
            if (p.getName().equalsIgnoreCase(productName)) {
                return p;
            }
        }
        return null;
    }

    public void addNewMachine(Machine machine) {
        this.machines.add(machine);
    }

    public void fabricateDemand(Demand demand) {
        this.chosenProduct = this.getProductByName(demand.getProductName());

        if (chosenProduct == null) {
            ConsolePrinter.fail("Produto sem demanda cadastrada.\n");
            return;
        }

        if (demand.getAmount() <= 0) {
            ConsolePrinter.fail("Nenhuma unidade de %s foi demandada. Atualize a demanda antes de fabricar.\n", chosenProduct.getName());
            return;
        }

        this.getConveyor().turnOn();
        for (Machine m: this.getMachines()) {
            m.turnOn();
        }

        System.out.printf("Iniciando produção de: %s\n", chosenProduct.getName());

        int productsRemaining = demand.getAmount();
        int fabricatedAmount = 0;
        int approvedAmount = 0;
        double totalProductionTime = 0;

        Machine currentMachine;
        Product currentProduct;

        while (fabricatedAmount < productsRemaining) {
            System.out.printf("(%d/%d) %s\n", (fabricatedAmount + 1), productsRemaining, chosenProduct.getName());

            try {
                // 1. Processamento
                ConsolePrinter.stageHeader("PROCESSAMENTO");
                currentMachine = this.machines.get(ProductionStages.PROCESSING.getCode());
                if (!calcProductionCost(currentMachine)) {
                    ConsolePrinter.treeFail(true, "Orçamento insuficiente para operar a máquina de processamento!");
                    break;
                }

                conveyor.addRawMaterial(chosenProduct.getRawMaterialPerUnit());
                ConsolePrinter.treeInfo(false, "%s carregado para a esteira.", this.rawMaterial.getName());

                this.rawMaterial.consume(conveyor.removeRawMaterial());
                ConsolePrinter.treeInfo(false, "%s transportado até a máquina de processamento.", this.rawMaterial.getName());
                ConsolePrinter.treeInfo(false, "Máquina processando %.2f %s de %s...", this.chosenProduct.getRawMaterialPerUnit(), this.rawMaterial.getUnit(), this.rawMaterial.getName());
                currentProduct = currentMachine.process(this.chosenProduct);
                ConsolePrinter.treeOk(true, "Produto %s #%d criado.", currentProduct.getName(), currentProduct.getId());

                // 2. Empacotamento
                ConsolePrinter.stageHeader("EMPACOTAMENTO");

                currentMachine = this.machines.get(ProductionStages.PACKAGING.getCode());
                if (!calcProductionCost(currentMachine)) {
                    ConsolePrinter.treeFail(true, "Orçamento insuficiente para operar a máquina de empacotamento!");
                    break;
                }

                this.conveyor.addProduct(currentProduct);
                ConsolePrinter.treeInfo(false, "%s #%d carregado para a esteira.", currentProduct.getName(), currentProduct.getId());
                ConsolePrinter.treeInfo(false, "%s #%d transportado(a) até a máquina de empacotamento.", currentProduct.getName(), currentProduct.getId());
                ConsolePrinter.treeInfo(false, "Máquina empacotando %s #%d...", currentProduct.getName(), currentProduct.getId());
                currentProduct = currentMachine.process(this.conveyor.removeProduct());
                ConsolePrinter.treeOk(true, "Produto %s #%d empacotado.", currentProduct.getName(), currentProduct.getId());

                // 3. Inspeção
                ConsolePrinter.stageHeader("INSPEÇÃO");
                currentMachine = this.machines.get(ProductionStages.INSPECTION.getCode());
                if (!calcProductionCost(currentMachine)) {
                    ConsolePrinter.treeFail(true, "Orçamento insuficiente para operar a máquina de inspeção!");
                    break;
                }
                this.conveyor.addProduct(currentProduct);
                ConsolePrinter.treeInfo(false, "%s #%d carregado para a esteira.", currentProduct.getName(), currentProduct.getId());
                ConsolePrinter.treeInfo(false, "%s #%d transportado(a) até a máquina de inspeção.", currentProduct.getName(), currentProduct.getId());
                ConsolePrinter.treeInfo(false, "Máquina inspecionando %s #%d...", currentProduct.getName(), currentProduct.getId());
                currentProduct = currentMachine.process(this.conveyor.removeProduct());
                conveyor.addProduct(currentProduct);
                ConsolePrinter.treeInfo(false, "Produto %s #%d inspecionado.", currentProduct.getName(), currentProduct.getId());

                if (currentProduct.getStatus() == ProductStatus.APPROVED) {
                    this.fabricatedProducts.add(this.conveyor.removeProduct());
                    ConsolePrinter.treeOk(true, "O produto %s #%d foi aprovado e enviado ao armazém!", currentProduct.getName(), currentProduct.getId());
                    approvedAmount++;
                } else {
                    ConsolePrinter.treeFail(true, "O produto %s #%d foi rejeitado na inspeção e descartado.", currentProduct.getName(), currentProduct.getId());
                    this.conveyor.removeProduct();
                }

                totalProductionTime += chosenProduct.countProductionTime();
                fabricatedAmount++;
            } catch (IllegalStateException | IllegalArgumentException e) {
                ConsolePrinter.treeFail(true, e.getMessage());
                break;
            }
        }

        ConsolePrinter.card("%d produtos fabricados e %d aprovados.\n", fabricatedAmount, approvedAmount);

        if (fabricatedAmount == productsRemaining) {
            demand.fulfill(fabricatedAmount, totalProductionTime);
            ConsolePrinter.info("Demanda de %s concluída em %.2f segundos de produção.\n", demand.getProductName(), totalProductionTime);
        } else if (fabricatedAmount > 0) {
            demand.partiallyFulfill(fabricatedAmount, totalProductionTime);
            ConsolePrinter.info("Demanda de %s atendida parcialmente (%d/%d) em %.2f segundos de produção.\n",
                    demand.getProductName(), fabricatedAmount, productsRemaining, totalProductionTime);
        } else {
            demand.reset();
        }

        this.getConveyor().turnOff();
        for (Machine m: this.getMachines()) {
            m.turnOff();
        }
    }

    private boolean calcProductionCost(Machine machine) {
        // for those who just entered the stream: calc is short for _calculate_
        if (this.budget >= machine.getOperationCost()) {
            this.budget -= machine.getOperationCost();
            return true;
        }
        return false;
    }

    public void buyRawMaterial(int amount) {
        float totalCost = amount * this.rawMaterial.getPrice();
        if (this.budget < totalCost) {
            ConsolePrinter.fail("Orçamento insuficiente para comprar matéria-prima!\n");
            return;
        }
        budget -= totalCost;
        this.rawMaterial.addStock(amount);
        ConsolePrinter.card(" [" + ConsolePrinter.GREEN + "OK" + ConsolePrinter.RESET + "] Compra de matéria-prima realizada com sucesso!");
    }

    public void setChosenProduct(Product chosenProduct) {
        this.chosenProduct = chosenProduct;
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

    public RawMaterial getRawMaterial() {
        return this.rawMaterial;
    }

    public void addNewConveyor(Conveyor conveyor) {
        this.conveyor = conveyor;
    }

    public Conveyor getConveyor() {
        return this.conveyor;
    }

    public float getBudget() {
        return budget;
    }

    public void displayBudget() {
        ConsolePrinter.printOneLineStats(this.budget, this.rawMaterial.getQuantity(), this.fabricatedProducts.size());
    }

    public void displayStorage() {
        System.out.printf("   Estoque de Matéria-Prima: " + ConsolePrinter.GREEN + "%.2f %s\n\n" + ConsolePrinter.RESET,
                this.rawMaterial.getQuantity(), this.rawMaterial.getUnit());
        ConsolePrinter.listStorage(this.demands, this.fabricatedProducts);
    }

}