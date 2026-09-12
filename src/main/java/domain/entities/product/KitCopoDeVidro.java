package domain.entities.product;

public class KitCopoDeVidro extends Product {

    public KitCopoDeVidro(String name, int rawMaterialPerUnit) {
        super(name, rawMaterialPerUnit, 10.0, 0.0);
    }

    @Override
    public Product process(Product model, ProductStatus newStatus) {
        Product p = new KitCopoDeVidro(model.getName(), model.getRawMaterialPerUnit());
        p.setStatus(newStatus);
        return p;
    }

    @Override
    public double countProductionTime() {
        double baseTime = 30.0;
        return baseTime * randomFactor();
    }

    @Override
    public String getType() {
        return "High Quality";
    }
}
