package domain.entities.demand;

/** Possui os valores Enum do ciclo de vida de uma {@link Demand},
 * sendo estes {@link PENDING}, {@link IN_PRODUCTION}, {@link COMPLETED} e {@link CANCELLED}.
 *
 * @author Heitor Almeida
 */
public enum DemandStatus {
    /** Esperando ser escolhida pra produção */
    PENDING("Pendente"),
    /** Passando pela linha de produção agora */
    IN_PRODUCTION("Em Produção"),
    /** Todas as unidades foram fabricadas */
    COMPLETED("Concluída"),
    /** Parou no meio por algum motivo. Ex.: máquina quebrou ou o orçamento acabou. Dá pra retomar */
    CANCELLED("Cancelada");

    private final String description;

    /**
     * Associa o status à descrição que aparece nas tabelas
     *
     * @param description nome do status em pt-br
     */
    DemandStatus(String description) {
        this.description = description;
    }

    /**
     * Nome do status em pt-br
     *
     * @return descrição, ex.: "Em Produção"
     */
    public String getDescription() {
        return description;
    }

    /**
     * Diz se as estratégias podem escolher a demanda nesse status
     *
     * @return booleano true só para demandas {@link #PENDING}
     */
    public boolean isSelectable() {
        return this == PENDING;
    }

    /**
     * Diz se dá pra mandar fabricar a demanda nesse status
     *
     * @return booleano true p/ {@link #PENDING} e {@link #CANCELLED} (caso de retomada)
     */
    public boolean canBeFabricated() {
        return this == PENDING || this == CANCELLED;
    }

    /**
     * Allow-list explícita dos próximos status válidos p/ cada status atual.
     * Uma demanda pode voltar pra {@link #PENDING} de qualquer status, menos {@link #IN_PRODUCTION}, pois atualizar a
     * quantidade de uma demanda ({@link Demand#updateAmount}) reseta ela, tornando-a uma nova demanda.<br>
     * Uma demanda em produção sempre termina como {@link #COMPLETED} ou {@link #CANCELLED} antes de dar para atualizá-la
     *
     * @param target status pro qual a demanda quer ir
     * @return true se a transição é válida
     */
    public boolean canTransitionTo(DemandStatus target) {
        if (target == PENDING && this != IN_PRODUCTION) {
            return true;
        }

        return switch (this) {
            case PENDING -> target == IN_PRODUCTION;
            case IN_PRODUCTION -> target == COMPLETED || target == CANCELLED;
            case CANCELLED -> target == IN_PRODUCTION;
            case COMPLETED -> false;
        };
    }
}
