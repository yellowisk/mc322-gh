package br.unicamp;

import java.util.UUID;

public class RawMaterial {
    // Mandatory attributes
    private UUID id;
    private String name;
    private int quantity;
    private String unity;
    private int minQuantity;

    // Mandatory methods
    public void consume(int quantity) {
        this.quantity -= quantity;
    }

    public void addStock(int quantity) {
        this.quantity += quantity;
    }

    public boolean isAvailable(int demand) {
        return (this.getQuantity() >= demand);
    }

    public UUID getId() {
        return (this.id);
    }
    public String getName() {
        return (this.name);
    }
    public int getQuantity() {
        return (this.quantity);
    }
}
