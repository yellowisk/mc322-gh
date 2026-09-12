package domain.entities.machines.machine;

import domain.entities.product.ProductStatus;
import domain.entities.rawmaterial.RawMaterial;
import domain.entities.product.Product;

import java.util.Random;

public abstract class Machine {
    private final String name;
    private boolean isOn;
    private final int maxCapacity;
    private final double failureOdd;
    private final double operationCost;
    protected static final Random random = new Random();

    public Machine(String name, int maxCapacity, double failureOdd, double operationCost) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.failureOdd = failureOdd;
        this.operationCost = operationCost;
    }

    /* ====== Abstract ====== */
    public abstract Product process(Product product, ProductStatus status);

    public abstract String getType();

    /* ====== Concrete ======*/

    protected boolean isProcessFailure(Product product) {
        boolean failureFloor = isMachineFailure();

        /* The greate the quality, thej gratear the rejection odds.
        The greater tcheckFailurehe cumulativeFailureOdd, the greater the rejection odds */
        double rejectionOdds = product.getQuality() * 0.3 + product.getCumulativeFailureOdd();

        return failureFloor || (random.nextDouble() < rejectionOdds);
    }

    protected void tryIncreaseFailureOdd(Product product, double increment) {
        if (isProcessFailure(product)) {
            product.increaseCumulativeFailureOdd(increment);
        }
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
        return random.nextDouble() < failureOdd;
    }

    public double getOperationCost() {
        return operationCost;
    }
}
