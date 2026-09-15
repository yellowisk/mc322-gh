package domain.entities.productionmanager;

public enum ProductionStages {
    PROCESSING(0, "processamento"),
    PACKAGING(1, "empacotamento"),
    INSPECTION(2, "inspeção");

    private final int code;
    private final String text;

    ProductionStages(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int getCode() {
        return code;
    }
    public String getName() {
        return text;
    }
}