package br.unicamp.ideal.domain.entities;

import lombok.Getter;

import java.util.UUID;

@Getter
public class RawMaterial {
    // Mandatory attributes
    private final UUID id;
    private final String name;
    private final String unit;
    private final int minQuantity;
    private int quantity;

    // My methods
    public RawMaterial(String name, int initialQuantity, String unit, int minQuantity) {
        if (initialQuantity < 0 || minQuantity < 0) {
            throw new IllegalArgumentException("Quantidades iniciais e mínimas não podem ser negativas.");
        }

        this.id = UUID.randomUUID();
        this.name = name;
        this.unit = unit;
        this.minQuantity = minQuantity;
        this.quantity = initialQuantity;
    }

    // Mandatory methods
    public void consume(int demand) {
        if (demand <= 0) {
            throw new IllegalArgumentException("A demanda para consumo deve ser maior que zero.");
        }
        if (!isAvailable(demand)) {
            throw new IllegalStateException("Estoque insuficiente para atender à demanda de " + demand + " " + unit + ".");
        }
        this.quantity -= demand;
    }

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("A quantidade para adicionar ao estoque deve ser maior que zero.");
        }
        this.quantity += quantity;
    }

    public boolean isAvailable(int demand) {
        return demand > 0 && this.quantity >= demand;
    }
}
