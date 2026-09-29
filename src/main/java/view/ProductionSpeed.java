package view;

/** Velocidade da animação da produção, escolhida no menu e mantida pela sessão toda */
public enum ProductionSpeed {
    NORMAL("Normal", 1.0),
    RAPIDA("Rápida", 0.3),
    INSTANTANEA("Instantânea", 0.0);

    private final String description;
    /* multiplica as pausas da produção, se for 0 = sem pausa) */
    private final double delayFactor;

    ProductionSpeed(String description, double delayFactor) {
        this.description = description;
        this.delayFactor = delayFactor;
    }

    public String getDescription() {
        return description;
    }

    public double getDelayFactor() {
        return delayFactor;
    }
}
