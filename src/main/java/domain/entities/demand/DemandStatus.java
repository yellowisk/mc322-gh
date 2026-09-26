package domain.entities.demand;

public enum DemandStatus {
    PENDING("Pendente"),
    IN_PRODUCTION("Em Produção"),
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
        return this == PENDING;
    }
    public boolean canBeFabricated() {
        return this == PENDING || this == CANCELLED;
    }

    /** Explicit allow-list of legal next statuses for each current status. */
    public boolean canTransitionTo(DemandStatus target) {
        if (target == PENDING) {
            // Updating a demand's amount always requeues it, regardless of current status.
            return true;
        }

        return switch (this) {
            case PENDING -> target == IN_PRODUCTION || target == CANCELLED;
            case IN_PRODUCTION -> target == COMPLETED || target == CANCELLED;
            case CANCELLED -> target == IN_PRODUCTION;
            case COMPLETED -> false;
        };
    }
}
