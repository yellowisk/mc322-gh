package domain.entities.demand;

/** Possui os valores Enum do ciclo de vida de uma {@link Demanda},
 * sendo estes {@link PENDENTE}, {@link EM_PRODUCAO}, {@link CONCLUIDA} e {@link CANCELADA}.
 *
 * @author Heitor Almeida
 */
public enum StatusDemanda {
    /** Esperando ser escolhida pra produção */
    PENDENTE("Pendente"),
    /** Passando pela linha de produção agora */
    EM_PRODUCAO("Em Produção"),
    /** Todas as unidades foram fabricadas */
    CONCLUIDA("Concluída"),
    /** Parou no meio por algum motivo. Ex.: máquina quebrou ou o orçamento acabou. Dá pra retomar */
    CANCELADA("Cancelada");

    private final String descricao;

    /**
     * Associa o status à descrição que aparece nas tabelas
     *
     * @param descricao nome do status em pt-br
     */
    StatusDemanda(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Nome do status em pt-br
     *
     * @return descrição, ex.: "Em Produção"
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Diz se as estratégias podem escolher a demanda nesse status
     *
     * @return booleano true só para demandas {@link #PENDENTE}
     */
    public boolean ehSelecionavel() {
        return this == PENDENTE;
    }

    /**
     * Diz se dá pra mandar fabricar a demanda nesse status
     *
     * @return booleano true p/ {@link #PENDENTE} e {@link #CANCELADA} (caso de retomada)
     */
    public boolean podeSerFabricada() {
        return this == PENDENTE || this == CANCELADA;
    }

    /**
     * Allow-list explícita dos próximos status válidos p/ cada status atual.
     * Uma demanda pode voltar pra {@link #PENDENTE} de qualquer status, menos {@link #EM_PRODUCAO}, pois atualizar a
     * quantidade de uma demanda ({@link Demanda#atualizarQuantidade}) reseta ela, tornando-a uma nova demanda.<br>
     * Uma demanda em produção sempre termina como {@link #CONCLUIDA} ou {@link #CANCELADA} antes de dar para atualizá-la
     *
     * @param alvo status pro qual a demanda quer ir
     * @return true se a transição é válida
     */
    public boolean podeTransicionarPara(StatusDemanda alvo) {
        if (alvo == PENDENTE && this != EM_PRODUCAO) {
            return true;
        }

        return switch (this) {
            case PENDENTE -> alvo == EM_PRODUCAO;
            case EM_PRODUCAO -> alvo == CONCLUIDA || alvo == CANCELADA;
            case CANCELADA -> alvo == EM_PRODUCAO;
            case CONCLUIDA -> false;
        };
    }
}
