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

        this.getConveyor().turnOn();
        for (Machine m: this.getMachines()) {
            m.turnOn();
        }

        System.out.printf("Iniciando produção de: %s\n", chosenProduct.getName());

        int productsRemaining = demand.getAmount();
        int fabricatedAmount = 0;
        int approvedAmount = 0;
        int nextId = chosenProduct.getId();

        Machine currentMachine;
        Product currentProduct;

        while (fabricatedAmount < productsRemaining) {
            System.out.printf("(%d/%d) %s #%d \n", (fabricatedAmount + 1), productsRemaining, chosenProduct.getName(), ++nextId);

            // 1. Processamento
            System.out.print("Etapa iniciada: " + ConsolePrinter.YELLOW + "PROCESSAMENTO" + ConsolePrinter.RESET + ".\n");
            currentMachine = this.machines.get(ProductionStages.PROCESSING.getCode());
            if (!calcProductionCost(currentMachine)) {
                ConsolePrinter.fail("Orçamento insuficiente para operar a máquina de processamento!\n");
                break;
            }

            conveyor.addRawMaterial(chosenProduct.getRawMaterialPerUnit());
            ConsolePrinter.info("%s carregado para a esteira.\n", this.rawMaterial.getName());

            this.rawMaterial.consume(conveyor.removeRawMaterial());
            ConsolePrinter.info("%s transportado até a máquina de processamento.\n", this.rawMaterial.getName());
            ConsolePrinter.info("Máquina processando %d %s de %s...\n", this.chosenProduct.getRawMaterialPerUnit(), this.rawMaterial.getUnit(), this.rawMaterial.getName());
            currentProduct = currentMachine.process(this.chosenProduct);
            ConsolePrinter.info("Produto %s #%d criado.\n", currentProduct.getName(), currentProduct.getId());
            ConsolePrinter.ok("Etapa concluída: PROCESSAMENTO.\n");

            // 2. Empacotamento
            System.out.print("Etapa iniciada: " + ConsolePrinter.YELLOW + "EMPACOTAMENTO" + ConsolePrinter.RESET + ".\n");

            currentMachine = this.machines.get(ProductionStages.PACKAGING.getCode());
            if (!calcProductionCost(currentMachine)) {
                ConsolePrinter.fail("Orçamento insuficiente para operar a máquina de empacotamento!\n");
                break;
            }

            this.conveyor.addProduct(currentProduct);
            ConsolePrinter.info("%s #%d carregado para a esteira.\n", currentProduct.getName(), currentProduct.getId());
            ConsolePrinter.info("%s #%d transportado(a) até a máquina de empacotamento.\n", currentMachine.getName(), currentProduct.getId());
            ConsolePrinter.info("Máquina empacotando %s #%d...\n", currentProduct.getName(), currentProduct.getId());
            currentProduct = currentMachine.process(this.conveyor.removeProduct());
            ConsolePrinter.info("Produto %s #%d empacotado.\n", currentProduct.getName(), currentProduct.getId());
            ConsolePrinter.ok("Etapa concluída: EMPACOTAMENTO.\n");

            // 3. Inspeção
            System.out.print("Etapa iniciada: " + ConsolePrinter.YELLOW + "INSPEÇÃO" + ConsolePrinter.RESET + ".\n");
            currentMachine = this.machines.get(ProductionStages.INSPECTION.getCode());
            if (!calcProductionCost(currentMachine)) {
                ConsolePrinter.fail("Orçamento insuficiente para operar a máquina de inspeção!\n");
                break;
            }
            this.conveyor.addProduct(currentProduct);
            ConsolePrinter.info("%s #%d carregado para a esteira.\n", currentProduct.getName(), currentProduct.getId());
            ConsolePrinter.info("%s #%d transportado(a) até a máquina de inspeção.\n", currentProduct.getName(), currentProduct.getId());
            ConsolePrinter.info("Máquina inspecionando %s #%d...\n", currentProduct.getName(), currentProduct.getId());
            currentProduct = currentMachine.process(this.conveyor.removeProduct());
            conveyor.addProduct(currentProduct);
            ConsolePrinter.info("Produto %s #%d inspecionado.\n", currentProduct.getName(), currentProduct.getId());

            if (currentProduct.getStatus() == ProductStatus.APPROVED) {
                this.fabricatedProducts.add(this.conveyor.removeProduct());
                ConsolePrinter.ok("O produto %s #%d foi aprovado e enviado ao armazém!\n", currentProduct.getName(), currentProduct.getId());
                approvedAmount++;
            } else {
                ConsolePrinter.fail("O produto %s #%d foi rejeitado na inspeção e descartado.\n", currentProduct.getName(), currentProduct.getId());
                this.conveyor.removeProduct();
            }
            ConsolePrinter.ok("[OK] Etapa concluída: INSPEÇÃO.\n");

            fabricatedAmount++;
        }

        ConsolePrinter.card("%d produtos fabricados e %d aprovados.\n", fabricatedAmount, approvedAmount);

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
        ConsolePrinter.ok("Compra de matéria-prima realizada com sucesso!\n");
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

}