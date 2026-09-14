package domain.entities.product;

public class CopoDeVidro extends Product {

    public CopoDeVidro(String name, double rawMaterialPerUnit) {
        super(name, rawMaterialPerUnit, 0.4, 0.0);
    }

    @Override
    public Product process(Product model, ProductStatus newStatus) {
        Product p = new CopoDeVidro(model.getName(), model.getRawMaterialPerUnit());
        p.setStatus(newStatus);
        return p;
    }

    @Override
    public double countProductionTime() {
        double baseTime = 10.0;
        return baseTime * randomFactor();
    }

    @Override
    public String getType() {
        return "Low quality";
    }
}