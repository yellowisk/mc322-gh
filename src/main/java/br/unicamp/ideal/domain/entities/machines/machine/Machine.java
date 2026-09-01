package br.unicamp.ideal.domain.entities.machines.machine;

import br.unicamp.ideal.domain.entities.RawMaterial;
import br.unicamp.ideal.domain.entities.product.Product;

public class Machine {
    private String name;
    private Boolean isOn;
    private int maxCapacity;

    public Machine(String name, Boolean isOn, int maxCapacity) {
        this.name = name;
        this.isOn = isOn;
        this.maxCapacity = maxCapacity;
    }

    void turnOff() {
        setIsOn(false);
    }

    void turnOn() {
        setIsOn(true);
    }

    boolean isOn() {
        return getIsOn();
    }

    private void process(RawMaterial material, int demand, Product product) {
        if (!getIsOn())
            throw new IllegalStateException("Eitcha, João! The machine can't proccess anything, since it ain't on!");

        if (!material.isAvailable(demand))
            throw new IllegalStateException("E não foi 150 reais! There is not enough raw material to proccess the product! Que pena!");

        product.process();
        material.consume(demand);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsOn() {
        // Replaces the method `estaLigada` sugested on tarefa1 :b
        // We thought it did not make sense, as it we'd be violating DRY
        return isOn;
    }

    public void setIsOn(Boolean isOn) {
        this.isOn = isOn;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
}
