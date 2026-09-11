package domain.entities.machines.machine;

import domain.entities.rawmaterial.RawMaterial;
import domain.entities.product.Product;

import java.util.Random;

public abstract class Machine {
    private final String name;
    private boolean isOn;
    private final int maxCapacity;
    private final float failureOdd;
    private final float operationCost;
    protected static final Random random = new Random();

    public Machine(String name, int maxCapacity, float failureOdd, float operationCost) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.failureOdd = failureOdd;
        this.operationCost = operationCost;
    }

    /* ====== Abstract ====== */
    public abstract void process(Product product);

    /* ====== Concrete ======*/
    public void turnOff() {
        this.isOn = false;
    }

    private void turnOn() {
        this.isOn = true;
    }

    protected boolean checkFailure() {
        return random.nextDouble() < failureOdd;
    }

    public Boolean isOn() {
        // Replaces the method `estaLigada` sugested on tarefa1 :b
        // We thought it did not make sense, as it we'd be violating DRY
        return isOn;
    }

    public String getName() {
        return name;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public float getFailureOdd() {
        return failureOdd;
    }

    public float getOperationCost() {
        return operationCost;
    }
}
