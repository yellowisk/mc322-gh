package domain.entities.productionmanager;

public enum ProductionStages {
    PROCESSING(0, "processamento", "processando"),
    PACKAGING(1, "empacotamento", "empacotando"),
    INSPECTION(2, "inspeção", "inspecionando");

    private final int code;
    private final String nome;
    private final String verboGerundio;

    ProductionStages(int code, String nome, String verboGerundio) {
        this.code = code;
        this.nome = nome;
        this.verboGerundio = verboGerundio;
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
}