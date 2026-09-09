package domain.entities.demand;

public class Demand {
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

