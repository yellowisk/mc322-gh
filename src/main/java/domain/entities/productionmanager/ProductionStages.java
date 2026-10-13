package domain.entities.productionmanager;

/**
 * Etapas de produção. Esse enum foi criado para generalizar o processo de fabricação
 * em ProductionManager.
 *
 * @author Glayson Oliveira
 */
public enum ProductionStages {
    /** Transforma a matéria-prima em produto */
    PROCESSING(0, "processamento", "processando", "processado"),
    /** Empacota o produto */
    PACKAGING(1, "empacotamento", "empacotando", "empacotado"),
    /** Decide se o produto é aprovado, precisa de manutenção ou é rejeitado */
    INSPECTION(2, "inspeção", "inspecionando", "inspecionado");

    private final int code;
    private final String nome;
    private final String verboGerundio;
    private final String verboParticipio;

    /**
     * Associa a etapa à máquina e aos textos dos logs
     *
     * @param code posição da máquina da etapa na lista de máquinas do ProductionManager
     * @param nome nome da etapa, ex.: "empacotamento"
     * @param verboGerundio verbo no gerúndio, ex.: "empacotando"
     * @param verboParticipio verbo no particípio, ex.: "empacotado"
     */
    ProductionStages(int code, String nome, String verboGerundio, String verboParticipio) {
        this.code = code;
        this.nome = nome;
        this.verboGerundio = verboGerundio;
        this.verboParticipio = verboParticipio;
    }

    /**
     * Posição da máquina dessa etapa na lista de máquinas do ProductionManager
     *
     * @return índice da máquina
     */
    public int getCode() {
        return code;
    }

    /**
     * Nome da etapa
     *
     * @return nome, ex.: "inspeção"
     */
    public String getNome() {
        return nome;
    }

    /**
     * Verbo da etapa no gerúndio, usado nos logs da produção
     *
     * @return verbo, ex.: "processando"
     */
    public Object getGerundio() {
        return verboGerundio;
    }

    /**
     * Verbo da etapa no particípio, usado nos logs da produção
     *
     * @return verbo, ex.: "processado"
     */
    public String getVerboParticipio() {
        return verboParticipio;
    }
}