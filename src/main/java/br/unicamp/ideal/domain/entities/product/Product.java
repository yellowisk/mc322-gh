package br.unicamp.ideal.domain.entities.product;

public class Product {
    private int id;
    private String name;
    private ProductStatus status;
    private int rawMaterialAmountNeeded;

    public Product(int id, String name, ProductStatus status, int rawMaterialNeeded) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.rawMaterialAmountNeeded = rawMaterialNeeded;
    }

    public void process() {
        if (status == ProductStatus.PROCESSED)
            throw new IllegalStateException("The product is already processed.");
        setStatus(ProductStatus.PROCESSED);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setRawMaterialAmountNeeded(int rawMaterialAmountNeeded) {
        this.rawMaterialAmountNeeded = rawMaterialAmountNeeded;
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
