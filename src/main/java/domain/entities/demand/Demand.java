package domain.entities.demand;

import domain.entities.product.Product;

public class Demand {
    private final String productName;
    private int amount;
    private boolean isDone;
    private int totalRawMaterial;

    public Demand(Product product, int amount) {
        this.productName = product.getName();
        this.amount = amount;
        this.isDone = false;
        this.totalRawMaterial = calcRawMaterialNeeded(product);
    }

    public int calcRawMaterialNeeded(Product product) {
        // for those who just joined the stream: "calc" is short for "calculate"
        return (product.getRawMaterialPerUnit() * this.amount);
    }

    public void setTotalRawMaterial(int totalRawMaterial) {
        this.totalRawMaterial = totalRawMaterial;
    }

    public int getTotalRawMaterial() {
        return totalRawMaterial;
    }

    public String getProductName() {
        return this.productName;
    }

    public int getAmount() {
        return this.amount;
    }

    public boolean isDone() {
        return this.isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}

