package br.unicamp;

import java.util.UUID;

public class RawMaterial {
    // Mandatory attributes
    private final UUID id;
    private final String name;
    private final String unit;
    private final int minQuantity;
    private int quantity;

    // My methods
    public RawMaterial(String name, int initialQuantity, String unit, int minQuantity) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.unit = unit;
        this.minQuantity = minQuantity;
        this.quantity = initialQuantity;
    }

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
