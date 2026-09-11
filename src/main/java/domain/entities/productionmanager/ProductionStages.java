package domain.entities.productionmanager;

public enum ProductionStages {
    PROCESSING(0),
    PACKAGING(1),
    INSPECTION(2);

    private final int code;

    ProductionStages(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}