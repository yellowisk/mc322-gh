package domain.entities.demand;

import domain.entities.product.Product;

public class Demand {
    private final String productName;
    private int amount;
    private DemandStatus status;
    private int producedAmount;
    private double totalRawMaterial;
    private double totalProductionTime;

    public Demand(Product product, int amount) {
        this.productName = product.getName();
        this.amount = amount;
        this.status = DemandStatus.PENDING;
        this.producedAmount = 0;
        this.totalRawMaterial = calcRawMaterialNeeded(product);
        this.totalProductionTime = 0.0;
    }

    public double calcRawMaterialNeeded(Product product) {
        // for those who just joined the stream: "calc" is short for "calculate"
        return (product.getRawMaterialPerUnit() * this.amount);
    }

    public void setTotalRawMaterial(double totalRawMaterial) {
        this.totalRawMaterial = totalRawMaterial;
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

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getTotalProductionTime() {
        return totalProductionTime;
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
