package domain.entities.rawmaterial;

import domain.exceptions.InsufficientRawMaterialException;
import domain.exceptions.InvalidDemandException;

import java.util.UUID;

/**
 * Matéria-prima da fábrica. É comprada no menu e gasta uma unidade de cada vez na etapa de processamento
 *
 * @author Glayson Oliveira
 * @author Heitor Almeida
 */
public class RawMaterial {
    // Mandatory attributes
    private final UUID id;
    private final String name;
    private final String unit;
    private final int minQuantity;
    private double quantity;
    private final float price;

    // My methods
    /**
     * Cria a matéria-prima c/ o estoque inicial
     *
     * @param name nome, ex.: "Vidro"
     * @param initialQuantity estoque inicial
     * @param unit unidade de medida, ex.: "kg"
     * @param minQuantity estoque mínimo, que não pode ser consumido
     * @param price preço por unidade, em R$
     * @throws IllegalArgumentException se o estoque inicial ou o mínimo forem negativos
     */
    public RawMaterial(String name, int initialQuantity, String unit, int minQuantity, float price) {
        if (initialQuantity < 0 || minQuantity < 0) {
            throw new IllegalArgumentException("Cadê a força? Quantidades iniciais e mínimas não podem ser negativas.");
        }

        this.id = UUID.randomUUID();
        this.name = name;
        this.unit = unit;
        this.minQuantity = minQuantity;
        this.quantity = initialQuantity;
        this.price = price;
    }

    // Mandatory methods
    /**
     * Tira a quantidade do estoque
     *
     * @param demand quantidade a consumir, maior que 0
     * @throws InvalidDemandException se a quantidade não for positiva
     * @throws InsufficientRawMaterialException se o consumo deixar o estoque abaixo do mínimo
     */
    public void consume(double demand) {
        if (demand <= 0) {
            throw new InvalidDemandException("Cadê a força? A demanda para consumo deve ser maior que zero.");
        }
        if (!isAvailable(demand)) {
            throw new InsufficientRawMaterialException("Cadê a força? Estoque insuficiente para atender à demanda de " + demand + " " + unit + ".");
        }
        this.quantity -= demand;
    }

    /**
     * Adiciona a quantidade especificada no estoque
     * @param quantity quantidade a adicionar, maior que 0
     * @throws IllegalArgumentException se a quantidade não for positiva
     */
    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Cadê a força? A quantidade para adicionar ao estoque deve ser maior que zero.");
        }
        this.quantity += quantity;
    }

    /**
     * Verifica se dá p/ consumir a quantidade sem deixar o estoque abaixo do mínimo
     * @param demand quantidade que se quer consumir
     * @return true se a quantidade for positiva e o estoque continuar acima do mínimo depois do consumo
     */
    public boolean isAvailable(double demand) {
        return demand > 0 && this.quantity >= demand && (this.quantity - demand) >= minQuantity;
    }

    /**
     * Identificador único da matéria-prima
     *
     * @return id gerado na criação
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Nome da matéria-prima
     *
     * @return nome, ex.: "Vidro"
     */
    public String getName() {
        return this.name;
    }

    /**
     * Unidade de medida
     *
     * @return unidade, ex.: "kg"
     */
    public String getUnit() {
        return this.unit;
    }

    /**
     * Estoque mínimo, que nunca pode ser consumido
     *
     * @return quantidade mínima
     */
    public int getMinQuantity() {
        return this.minQuantity;
    }

    /**
     * Preço de uma unidade
     *
     * @return preço, em R$
     */
    public float getPrice() {
        return this.price;
    }

    /**
     * Estoque atual, contando o mínimo
     *
     * @return quantidade em estoque
     */
    public double getQuantity() {
        return this.quantity;
    }

    /**
     * Sobrescreve o estoque direto, sem validação
     *
     * @param quantity novo estoque
     */
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

}
