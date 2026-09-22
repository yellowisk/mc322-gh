package domain.entities.demand;

import domain.entities.product.Product;

public class Demand {
    private final String productName;
    private int amount;
    private DemandStatus status;
    private int producedAmount;
    private double totalRawMaterial;
    private double totalProductionTime;
    private double estimatedCost;

    public Demand(Product product, int amount) {
        this.productName = product.getName();
        this.amount = amount;
        this.status = DemandStatus.PENDING;
        this.producedAmount = 0;
        this.totalRawMaterial = calcRawMaterialNeeded(product);
        this.totalProductionTime = 0.0;
    }

    private double calcRawMaterialNeeded(Product product) {
        // for those who just joined the stream: "calc" is short for "calculate"
        return (product.getRawMaterialPerUnit() * this.amount);
    }

    public double getTotalRawMaterial() {
        return totalRawMaterial;
    }

    public String getProductName() {
        return this.productName;
    }

    public int getAmount() {
        return this.amount;
    }

    public DemandStatus getStatus() {
        return this.status;
    }

    public int getProducedAmount() {
        return this.producedAmount;
    }

    public void updateAmount(Product product, int newAmount) {
        this.amount = newAmount;
        this.totalRawMaterial = calcRawMaterialNeeded(product);
        reset();
    }

    public double getTotalProductionTime() {
        return totalProductionTime;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    /* Must be called with a fresh unitOperationCost */
    public void updateEstimatedCost(double unitOperationCost) {
        this.estimatedCost = this.amount * unitOperationCost;
    }

    public boolean isViable(double availableBudget) {
        return this.estimatedCost <= availableBudget;
    }

    public void fulfill(int producedAmount, double totalProductionTime) {
        this.status = DemandStatus.COMPLETED;
        this.producedAmount = producedAmount;
        this.totalProductionTime = totalProductionTime;
    }

    public void partiallyFulfill(int producedAmount, double totalProductionTime) {
        this.status = DemandStatus.PARTIAL;
        this.producedAmount = producedAmount;
        this.totalProductionTime = totalProductionTime;
    }

    public void reset() {
        this.status = DemandStatus.PENDING;
        this.producedAmount = 0;
        this.totalProductionTime = 0;
    }
}
