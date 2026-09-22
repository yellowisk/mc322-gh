package domain.entities.machine;

import domain.entities.product.Product;
import domain.utils.RandomProvider;

public abstract class Machine {
    // === Atributos ===
    private final String name;
    private boolean isOn;
    private final int maxCapacity;
    private final double failureOdd;
    private final double operationCost;
    // Saúde
    private static final int saudeMaxima = 100; /** Saúde máxima. */
    private int saude = saudeMaxima; /** Saúde atual. */
    private int desgasteMaximo = 5; /** Valor máximo de desgaste por ciclo. */

    public Machine(String name, int maxCapacity, double failureOdd, double operationCost) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.failureOdd = failureOdd;
        this.operationCost = operationCost;
    }

    /**
     * Método auxiliar do método 'process()'. Esse é o método que
     * as subclasses irão sobrescrever.
     *
     * @param product O produto deverá ser fabricado.
     * @return Produto fabricado.
     */
    public abstract Product processAux(Product product);

    /**
     * Método 'process()' principal. Funciona como um Decorator.
     *
     * @param product O produto deverá ser fabricado.
     * @return Produto fabricado.
     */
    public Product process(Product product) {
        Product produto = processAux(product);
        desgastar(); // Trecho que será rodado sempre que o 'process()' for utilizado
        return product;
    }

    public abstract String getType();

    /* ====== Concrete ======*/

    protected boolean isProcessFailure(Product product) {
        boolean failureFloor = isMachineFailure();

        /* The greate the quality, thej gratear the rejection odds.
        The greater tcheckFailurehe cumulativeFailureOdd, the greater the rejection odds */
        double rejectionOdds = product.getQuality() * 0.3 + product.getCumulativeFailureOdd();

        return failureFloor || (RandomProvider.chance(rejectionOdds));
    }

    protected void tryIncreaseFailureOdd(Product product, double increment) {
        if (isMachineFailure()) {
            product.increaseCumulativeFailureOdd(increment);
        }
    }

    private void desgastar() {
        this.saude -= RandomProvider.nextInt(this.desgasteMaximo);
        System.out.printf("♡ Saúde atual da máquina de '%s': %d\n", getType(), saude); // TODO: remover debug
    }

    private void reparar() {
        /* TODO: Método `reparar()` aumentar a saúde da máquina aos poucos para
            a barra de progresso da reparação.
         */
        setSaude(100);
    }

    // ---- Getters e Setters ----


    public int getSaude() {
        return saude;
    }

    public void setSaude(int saude) {
        this.saude = saude;
    }

    public void turnOff() {
        this.isOn = false;
    }

    public void turnOn() {
        this.isOn = true;
    }

    public boolean isOn() {
        // Replaces the method `estaLigada` suggested on tarefa1 :b
        // We thought it did not make sense, as it we'd be violating DRY
        return isOn;
    }

    public String getName() {
        return name;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public double getFailureOdd() {
        return failureOdd;
    }

    protected boolean isMachineFailure() {
        return RandomProvider.chance(failureOdd);
    }

    public double getOperationCost() {
        return operationCost;
    }
}
