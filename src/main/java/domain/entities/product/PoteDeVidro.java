package domain.entities.product;

public class PoteDeVidro extends Product {
    public PoteDeVidro(String name, int rawMaterialPerUnit) {
        super(name, rawMaterialPerUnit, 7.0, 0.0);
    }

    @Override
    public Product process(Product model, ProductStatus newStatus) {
        Product p = new PoteDeVidro(model.getName(), model.getRawMaterialPerUnit());
        p.setStatus(newStatus);
        return p;
    }

    @Override
    public double countProductionTime() {
        double baseTime = 20.0;
        return baseTime * randomFactor();
    }

    @Override
    public String getType() {
        return "Average quality";
    }
}
