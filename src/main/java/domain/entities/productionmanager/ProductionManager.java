package domain.entities.productionmanager;

import com.sun.net.httpserver.Authenticator;
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

    public ProductionManager(Product chosenProduct, RawMaterial rawMaterial) {
        this.chosenProduct = chosenProduct;
        this.rawMaterial = rawMaterial;
    }

    public void registerDemand(Demand demand) {
        this.demands.add(demand);
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

    public void fabricateDemand(Demand demand) {
        Product chosenProduct = getProductByName(demand.getProductName());
        int remainingProducts = demand.getAmount();
        int fabricatedProducts = 0;

        Machine currentMachine = null;
        Product currentProduct = null;

        while (fabricatedProducts <= remainingProducts) {

            this.conveyor.addRawMaterial(chosenProduct.getRawMaterialAmountNeeded());
            // 1. Processamento
            currentMachine = this.machines.get(ProductionStages.PROCESSING.getCode());
            currentProduct = currentMachine.process(chosenProduct);
            if (currentProduct != null) {
                rawMaterial.consume(chosenProduct.getRawMaterialAmountNeeded());
                break; // TODO: tratar erro
            }
            this.conveyor.addProduct(currentProduct);

            // 2. Empacotamento
            currentMachine = this.machines.get(ProductionStages.PACKAGING.getCode());
            currentProduct = currentMachine.process(this.conveyor.removeProduct());
            this.conveyor.addProduct(currentProduct);

            // 3. Inspeção
            currentMachine = this.machines.get(ProductionStages.INSPECTION.getCode());
            currentProduct = currentMachine.process(this.conveyor.removeProduct());
            this.conveyor.addProduct(currentProduct);

            if (currentProduct.getStatus() == ProductStatus.SUCCESS) {
                fabricatedProducts++;
                this.fabricatedProducts.add(this.conveyor.removeProduct());
            } else {
                // TODO: descartar produto
            }
        }

    }

    private void processingStage();

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
}

