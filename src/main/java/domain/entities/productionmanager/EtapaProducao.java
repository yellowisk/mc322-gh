package domain.entities.productionmanager;

/**
 * Etapas de produção. Esse enum foi criado para generalizar o processo de fabricação
 * em ProductionManager.
 *
 * @author Glayson Oliveira
 */
public enum EtapaProducao {
    /** Transforma a matéria-prima em produto */
    PROCESSAMENTO(0, "processamento", "processando", "processado"),
    /** Empacota o produto */
    EMPACOTAMENTO(1, "empacotamento", "empacotando", "empacotado"),
    /** Decide se o produto é aprovado, precisa de manutenção ou é rejeitado */
    INSPECAO(2, "inspeção", "inspecionando", "inspecionado");

    private final int codigo;
    private final String nome;
    private final String verboGerundio;
    private final String verboParticipio;

    /**
     * Associa a etapa à máquina e aos textos dos logs
     *
     * @param codigo posição da máquina da etapa na lista de máquinas do ProductionManager
     * @param nome nome da etapa, ex.: "empacotamento"
     * @param verboGerundio verbo no gerúndio, ex.: "empacotando"
     * @param verboParticipio verbo no particípio, ex.: "empacotado"
     */
    EtapaProducao(int codigo, String nome, String verboGerundio, String verboParticipio) {
        this.codigo = codigo;
        this.nome = nome;
        this.verboGerundio = verboGerundio;
        this.verboParticipio = verboParticipio;
    }

    /**
     * Posição da máquina dessa etapa na lista de máquinas do ProductionManager
     *
     * @return índice da máquina
     */
    public int getCodigo() {
        return codigo;
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
    public String getGerundio() {
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