package domain.entities.demand;

import domain.entities.product.Product;

public class Demand {
    private final String productName;
    private int amount;
    private boolean isDone;

    public Demand(String productName, int amount) {
        this.productName = productName;
        this.amount = amount;
        this.isDone = false;
    }

    public int calcRawMaterialNeeded(Product product) {
        // for those who just joined the stream: "calc" is short for "calculate"
        return (product.getRawMaterialPerUnit() * this.amount);
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

