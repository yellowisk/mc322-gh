package domain.entities.product;

public class Product {
    private static int globalUniqueId = 1;

    private final int id;
    private final String name;
    private ProductStatus status =  ProductStatus.UNPROCESSED;
    private final int rawMaterialAmountNeeded;

    public Product(String name, int rawMaterialNeeded) {
        this.id = globalUniqueId++;
        this.name = name;
        this.rawMaterialAmountNeeded = rawMaterialNeeded;
    }

    public void process() {
        if (status == ProductStatus.PROCESSED) {
            throw new IllegalStateException("[DESSA VEZ NÃO É] The product is already processed.");
        }
        setStatus(ProductStatus.PROCESSED);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public int getRawMaterialAmountNeeded() {
        return rawMaterialAmountNeeded;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", rawMaterialAmountNeeded=" + rawMaterialAmountNeeded +
                '}';
    }
}
