package br.unicamp.ideal.domain.entities.machines.conveyor;

import br.unicamp.ideal.domain.entities.machines.MachinesStatus;


public class Conveyor {
    // Mandatory attributes
    private Object item;
    private boolean inMovement;
    private int maxCapacity;

    // My attributes
    private MachinesStatus status;

    // Mandatory methods
    private void turnOn() { setStatus(MachinesStatus.ON); }

    private void turnOff() { setStatus(MachinesStatus.OFF); }

    private void addItem(Object item) { setItem(item); }

    private void removeItem() { setItem(null); }

    // TODO: Implementar método canCarry
    private boolean canCarry(Object item) {
        return false;
    }

    public void setStatus(MachinesStatus status) {
        this.status = status;
    }

    public void setInMovement(boolean inMovement) {
        this.inMovement = inMovement;
    }

    public void setItem(Object item) {
        this.item = item;
    }
}
