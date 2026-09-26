package domain.entities.machine;

import domain.entities.product.Product;
import domain.exceptions.MachineNeedsRepairException;
import domain.interfaces.Auditable;
import domain.utils.RandomProvider;
import view.ConsolePrinter;

public abstract class Machine implements Auditable {
    // === Atributos ===
    private final String name;
    private boolean isOn;
    private final int maxCapacity;
    private final double failureOdd; /** Chance base de falha. */
    private final double operationCost;
    private final double scenarioMultiplier; // Multiplicador de falha do cenário atual
    // Saúde
    private StatusDeMaquina status = StatusDeMaquina.FUNCIONAL;
    private static final int saudeMaxima = 100; /** Saúde máxima. */
    private int saude = saudeMaxima;
    private final int saudeCritica = 15; /** Limiar crítico da saúde para manutenção. */
    private final int desgasteMaximo;

    public Machine(String name, int maxCapacity, double failureOdd,
                   double operationCost, double scenarioMultiplier,
                   int wearDamage) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.failureOdd = failureOdd;
        this.operationCost = operationCost;
        this.scenarioMultiplier = scenarioMultiplier;
        this.desgasteMaximo = wearDamage;
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

        if (needsMaintenance()) {
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
        double rejectionOdds = product.getRejectionRisk();

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
        setSaude(saudeMaxima);
        this.status = StatusDeMaquina.FUNCIONAL;
    }

    // === AUDITÁVEL ===

    /**
     * Retorna um relatório de diagnóstico formatado com o estado completo
     * da máquina, incluindo saúde, status, custos, etc.
     * @return String formatada com o diagnóstico.
     */
    @Override
    public String generateDiagnosticReport() {
        String reset = ConsolePrinter.RESET;
        String blue = ConsolePrinter.BLUE;
        String green = ConsolePrinter.GREEN;
        String red = ConsolePrinter.RED;
        String yellow = ConsolePrinter.YELLOW;
        String gray = ConsolePrinter.GRAY;

        // Saúde
        String corSaude = needsMaintenance() ? red : green;
        String saudeFormatada = String.format("%s%d/%d%s", corSaude, getSaude(), getSaudeMaxima(), reset);

        // Chance de falha
        String chanceFalhaFormatada;
        if (getRawChanceFalha() == getFailureOdd()) {
            chanceFalhaFormatada = String.format("%s %.2f%s", blue, getFailureOdd(), reset);
        } else {
            double diffChanceFalha = getFailureOdd() - getRawChanceFalha();
            chanceFalhaFormatada = String.format("%s⚂ %.2f %s(▴ %.2f)%s", blue, getFailureOdd(), gray, diffChanceFalha, reset);
        }

        // Status
        String statusEnergia = isOn() ? green + "Ligada" + reset : gray + "Desligada" + reset;
        String statusFisico = (this.status == StatusDeMaquina.FUNCIONAL) ? green + "✓ Funcional" + reset : red + "✗ Quebrada" + reset;

        // Custo operacional
        String custo = String.format("%sR$ %.2f%s", yellow, getOperationCost(), reset);
        String capacidade = String.format("%s%d un/ciclo%s", blue, getMaxCapacity(), reset);

        // Texto
        return String.format(
                """
                        ├─ ⏻ Status: %s | %s
                        ├─ ❤ Saúde: %s
                        ├─ ⚂ Chance de Falha: %s
                        ├─ $ Custo Operacional: %s
                        └─ Capacidade Máx: %s
                """,
                statusEnergia, statusFisico,
                saudeFormatada,
                chanceFalhaFormatada,
                custo,
                capacidade
        );
    }

    /**
     * Indica se a saúde da máquina está abaixo do limiar crítico.
     * @return true se a saúde estiver abaixo do limiar crítico.
     */
    @Override
    public boolean needsMaintenance() {
        return this.saude < this.saudeCritica;
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
        return this.failureOdd * (1 + (double) (this.getSaudeMaxima() - this.saude) / this.getSaudeMaxima()) * this.scenarioMultiplier;
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

    public int getSaudeMaxima() {
        return saudeMaxima;
    }
}
