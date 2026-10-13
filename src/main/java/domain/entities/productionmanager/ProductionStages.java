package domain.entities.productionmanager;

/**
 * Etapas de produção. Esse enum foi criado para generalizar o processo de fabricação
 * em ProductionManager
 */
public enum ProductionStages {
    PROCESSING(0, "processamento", "processando", "processado"),
    PACKAGING(1, "empacotamento", "empacotando", "empacotado"),
    INSPECTION(2, "inspeção", "inspecionando", "inspecionado");

    private final int code;
    private final String nome;
    private final String verboGerundio;
    private final String verboParticipio;

    ProductionStages(int code, String nome, String verboGerundio, String verboParticipio) {
        this.code = code;
        this.nome = nome;
        this.verboGerundio = verboGerundio;
        this.verboParticipio = verboParticipio;
    }

    public int getCode() {
        return code;
    }

    public String getNome() {
        return nome;
    }

    public Object getGerundio() {
        return verboGerundio;
    }

    public String getVerboParticipio() {
        return verboParticipio;
    }
}