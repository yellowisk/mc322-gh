package domain.entities.machines.machine;

import domain.entities.rawmaterial.RawMaterial;
import domain.entities.product.Product;

public class Machine {
    private final String name;
    private boolean isOn;
    private final int maxCapacity;

    public Machine(String name, int maxCapacity) {
        this.name = name;
        this.maxCapacity = maxCapacity;
    }

    public void turnOff() {
        this.isOn = false;
    }

    public void turnOn() {
        this.isOn = true;
    }

    public void process(RawMaterial material, int demand, Product product) {
        if (!isOn()) {
            throw new IllegalStateException("[Eitcha, João...] The machine can't proccess anything, since it ain't on!");
        }

        if (!material.isAvailable(demand)) {
            throw new IllegalStateException("[E não foi 150 reais...] There is not enough raw material to process the product! Que pena!");
        }

        if (demand > this.maxCapacity) {
            throw new IllegalArgumentException("[NÃO FOI DESSA VEZ...] The demand is higher than the machine's capacity.");
        }

        product.process();
        material.consume(demand);
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
}
