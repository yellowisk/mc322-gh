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

    /** Explicit allow-list of legal next statuses for each current status. */
    public boolean canTransitionTo(DemandStatus target) {
        if (target == this || target == PENDING) {
            // Updating a demand's amount always requeues it, regardless of current status.
            return true;
        }

        return switch (this) {
            case PENDING, PARTIAL -> target == IN_PRODUCTION || target == PARTIAL
                    || target == COMPLETED || target == CANCELLED;
            case IN_PRODUCTION -> target == PARTIAL || target == COMPLETED || target == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
