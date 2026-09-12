package domain.entities.productionmanager;

import domain.entities.demand.Demand;
import domain.entities.machines.conveyor.Conveyor;
import domain.entities.machines.machine.Machine;
import domain.entities.product.Product;
import domain.entities.product.ProductStatus;
import domain.entities.rawmaterial.RawMaterial;

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
        this.demands.add(new Demand(product.getName(), amount));
    }

    public void updateDemand(Demand demand, int newValue) {
        demand.setAmount(newValue);
    }

    private Product getProductByName(String productName) {
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
        System.out.printf("[DEBUG] Produto selecionado: %s\n", chosenProduct.getName());
        int remainingProducts = demand.getAmount();
        int fabricatedProducts = 0;

        if (chosenProduct == null) {
            System.out.println("Produto sem demanda.");
            return;
        }

        Machine currentMachine = null;
        Product currentProduct = null;

        while (fabricatedProducts <= remainingProducts) {

            this.conveyor.addRawMaterial(chosenProduct.getRawMaterialPerUnit());
            // 1. Processamento
            System.out.println("[OK] Etapa iniciada: PROCESSAMENTO.\n");
            currentMachine = this.machines.get(ProductionStages.PROCESSING.getCode());
            currentProduct = currentMachine.process(this.chosenProduct, ProductStatus.PROCESSED);
            if (currentProduct == null) {
                rawMaterial.consume(chosenProduct.getRawMaterialPerUnit());
                break; // TODO: tratar erro
            }
            System.out.println("[OK] Etapa concluída: PROCESSAMENTO.\n");
            this.conveyor.addProduct(currentProduct);

            // 2. Empacotamento
            System.out.println("[OK] Etapa iniciada: EMPACOTAMENTO.");
            currentMachine = this.machines.get(ProductionStages.PACKAGING.getCode());
            currentProduct = currentMachine.process(this.conveyor.removeProduct(), ProductStatus.PACKED);
            this.conveyor.addProduct(currentProduct);
            System.out.println("[OK] Etapa concluída: EMPACOTAMENTO.\n");

            // 3. Inspeção
            System.out.println("[OK] Etapa iniciada: INSPEÇÃO.");
            currentMachine = this.machines.get(ProductionStages.INSPECTION.getCode());
            currentProduct = currentMachine.process(this.conveyor.removeProduct(), ProductStatus.INSPECTED);
            this.conveyor.addProduct(currentProduct);
            System.out.println("[OK] Etapa concluída: INSPEÇÃO.\n");

            if (currentProduct.getStatus() == ProductStatus.APPROVED) {
                fabricatedProducts++;
                this.fabricatedProducts.add(this.conveyor.removeProduct());
            } else {
                // TODO: descartar produto
            }
        }

    }

    public void buyRawMaterial(int amount) {
        float totalCost = amount * this.rawMaterial.getPrice();
        if (this.budget < totalCost) { return;}
        budget -= totalCost;
        this.rawMaterial.addStock(amount);
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
}

