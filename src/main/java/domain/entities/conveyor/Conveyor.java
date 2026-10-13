package domain.entities.conveyor;

import domain.entities.product.Produto;
import domain.exceptions.MachineUnavailableException;

/**
 * Esteira que leva a matéria-prima e os produtos entre as máquinas. Carrega uma coisa por vez e só funciona ligada
 *
 * @author Glayson Oliveira
 * @author Heitor Almeida
 */
public class Conveyor {
    // Mandatory attributes
    private final String name;
    private Produto product;
    private double rawMaterial;
    private boolean isOn; // emMovimento
    private final int maxCapacity;

    /**
     * Cria a esteira desligada e vazia
     *
     * @param name nome, ex.: "Esteira Ligeira"
     * @param maxCapacity peso máximo que ela carrega, em kg
     * @throws IllegalArgumentException se a capacidade não for positiva
     */
    public Conveyor(String name, int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Dessa vez não é! A conveyor can't have a negative capacity.");
        }
        this.name = name;
        this.maxCapacity = maxCapacity;
    }

    // Mandatory methods
    /**
     * Desliga a esteira
     */
    public void turnOff() {
        this.isOn = false;
    }

    /**
     * Liga a esteira
     */
    public void turnOn() {
        this.isOn = true;
    }

    /**
     * Verifica se a esteira consegue carregar a quantidade especificada de
     * matéria prima
     * @param quantity quantidade
     */
    private void checkCanCarry(double quantity) {
        if (!this.isOn) {
            throw new IllegalStateException("Dessa vez não é! The conveyor is off.");
        }
        if (this.rawMaterial > 0 || this.product != null) {
            throw new IllegalStateException("Dessa vez não é! The conveyor is already occupied.");
        }
        if (!verifyCapacity(quantity)) {
            throw new IllegalArgumentException("Dessa vez não é! The conveyor can't take this weight.");
        }
    }

    // >>>>> RECURSO
    /**
     * Coloca matéria-prima na esteira
     *
     * @param quantity quantidade a carregar
     * @throws IllegalStateException se a esteira estiver desligada ou já ocupada
     * @throws IllegalArgumentException se a quantidade passar da capacidade
     */
    public void addRawMaterial(double quantity) {
        checkCanCarry(quantity);
        this.rawMaterial = quantity;
    }

    /**
     * Remove a matéria prima da esteira retornando a quantidade
     * do material
     * @return quantidade de matéria prima da esteira
     * @throws domain.exceptions.MachineUnavailableException se a esteira estiver desligada
     * @throws IllegalStateException se não tiver matéria-prima na esteira
     */
    public double removeRawMaterial() {
        if (!isOn()) {
            throw new MachineUnavailableException("Dessa vez não é! The conveyor is off.");
        }

        if (this.rawMaterial == 0) {
            throw new IllegalStateException("Dessa vez não é! There's no product on the conveyor.");
        }

        double n = this.rawMaterial;
        this.rawMaterial = 0;
        return n;
    }

    // >>>>> PRODUTO

    /**
     * Adiciona o produto na esteira
     * @param product o produto a ser adicionado
     * @throws IllegalStateException se a esteira estiver desligada ou já ocupada
     * @throws IllegalArgumentException se o produto passar da capacidade
     */
    public void addProduct(Produto product) {
        checkCanCarry(product.getQuantidadeMateriaPrimaPorUnidade());
        this.product = product;
    }

    /**
     * Remove o produto da esteira, retornando-o
     * @return o produto
     * @throws domain.exceptions.MachineUnavailableException se a esteira estiver desligada
     * @throws IllegalArgumentException se não tiver produto na esteira
     */
    public Produto removeProduct() {
        if (!isOn()) {
            throw new MachineUnavailableException("Não é mais ligeira... A esteira está desligada.");
        }

        if (this.product == null) {
            throw new IllegalArgumentException("Dessa vez não é! There's no product on the conveyor.");
        }


        Produto p = this.product;
        this.product = null;
        return p;
    }

    /**
     * Verifica se a esteira suporta o peso fornecido
     * @param weight o peso a ser verificado
     * @return true ou false
     */
    private boolean verifyCapacity(double weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Dessa vez não é! The weight can't be negative");
        }

        return (weight <= this.maxCapacity);
    }

    /**
     * Matéria-prima que está na esteira agora
     *
     * @return quantidade carregada, 0 se não tiver nada
     */
    public double getRawMaterial() {
        return rawMaterial;
    }

    /**
     * Produto que está na esteira agora
     *
     * @return produto carregado, ou null se não tiver nenhum
     */
    public Produto getProduct() {
        return product;
    }

    /**
     * Nome da esteira
     *
     * @return nome
     */
    public String getName() {
        return name;
    }

    /**
     * Diz se a esteira está ligada
     *
     * @return true se estiver ligada
     */
    public boolean isOn() {
        return isOn;
    }
}
