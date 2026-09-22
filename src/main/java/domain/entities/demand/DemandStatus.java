package domain.entities.demand;

public enum DemandStatus {
    PENDING("Pendente"),
    IN_PRODUCTION("Em Produção"),
    PARTIAL("Parcialmente Atendida"),
    COMPLETED("Concluída"),
    CANCELLED("Cancelada");

    private final String description;

    DemandStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSelectable() {
        return this == PENDING || this == PARTIAL;
    }
}
