package domain.entities.machine;

import domain.entities.product.Product;
import domain.exceptions.MachineNeedsRepairException;
import domain.utils.RandomProvider;

public abstract class Machine {
    // === Atributos ===
    private final String name;
    private boolean isOn;
    private final int maxCapacity;
    private final double failureOdd; /** Chance base de falha. */
    private final double operationCost;
    // Saúde
    private StatusDeMaquina status = StatusDeMaquina.FUNCIONAL;
    private static final int saudeMaxima = 40; /** Saúde máxima. */
    private int saude = saudeMaxima; /** Saúde atual. */
    private int desgasteMaximo = 5; /** Valor máximo de desgaste por ciclo. */
    private final int saudeCritica = 15; /** Limiar crítico da saúde para manutenção. */

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
     * Aqui ficam trechos de código compartilhados por
     * todas as máquinas.
     *
     * @param product O produto deverá ser fabricado.
     * @return Produto fabricado.
     */
    public Product process(Product product) {
        if (!isOn()) {
            throw new IllegalStateException("Eitcha, João! The machine can't process anything, since it ain't on!");
        }

        if (precisaManutencao()) {
            String mensagem = String.format("A %s quebrou!", getName().toLowerCase());
            throw new MachineNeedsRepairException(mensagem);
        }

        try {
            Product produto = processAux(product);
            desgastar(); // Trecho que será rodado sempre que o 'process()' for utilizado
            return produto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    // === SAÚDE DA MÁQUINA ===

    /**
     * Diminui a saúde da máquina em [1, this.desgasteMaximo]
     */
    private void desgastar() {
        this.saude -= RandomProvider.nextInt(this.desgasteMaximo) + 1;
        if (this.saude < this.saudeCritica) {
            this.status = StatusDeMaquina.QUEBRADA;
        }
    }

    /**
     * Restaura a saúde da máquina para this.saudeMaxima
     */
    public void reparar() {
        /* TODO: Método `reparar()` aumentar a saúde da máquina aos poucos para
            a barra de progresso da reparação.
         */
        setSaude(saudeMaxima);
        this.status = StatusDeMaquina.FUNCIONAL;
    }

    /**
     * Indica se a saúde da máquina está abaixo do limiar crítico.
     * @return true se a saúde estiver abaixo do limiar crítico.
     */
    public boolean precisaManutencao() {
        return this.saude <= this.saudeCritica;
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

    /**
     * Calcula e retorna a chance de falha atual baseando-se na saúde atual da
     * máquina.
     * falhaEfetiva = falhaBase * (1 + (100 - saúde)/100)
     *
     * @return A chance de falha da máquina.
     */
    public double getFailureOdd() {
        return this.failureOdd * (1 + (double) (this.getSaudeMaxima() - this.saude) / this.getSaudeMaxima());
    }

    public double getRawChanceFalha() {
        return this.failureOdd;
    }

    protected boolean isMachineFailure() {
        return RandomProvider.chance(failureOdd);
    }

    public double getOperationCost() {
        return operationCost;
    }

    public int getSaudeCritica() {
        return saudeCritica;
    }

    public int getSaudeMaxima() {
        return saudeMaxima;
    }
}
