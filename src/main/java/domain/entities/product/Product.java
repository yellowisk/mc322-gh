package domain.entities.product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public abstract class Product {
    private static int globalUniqueId = 1;

    private final int id;
    private final String name;
    private ProductStatus status;
    private final int rawMaterialPerUnit;
    private final double quality;
    private double cumulativeFailureOdd;
    private static int productsCounter;

    private static final Random random = new Random();

    public Product(String name, int rawMaterialPerUnit, double quality,
                   double cumulativeFailureOdd) {
        this.id = globalUniqueId++;
        this.name = name;
        this.rawMaterialPerUnit = rawMaterialPerUnit;
        this.quality = quality;
        this.cumulativeFailureOdd = cumulativeFailureOdd;
        this.status = ProductStatus.UNDERGOING;

        /* As every subclass uses super, this will run for every subclass created */
        productsCounter++;
    }

    public abstract Product process(Product model, ProductStatus newStatus);

    /* Will be used to showcase the time it has taken
    for the product to be processed by the processing machine */
    public abstract double countProductionTime();

    /* Will be used to showcase the type of the product easily */
    public abstract String getType();

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

    public int getRawMaterialPerUnit() {
        return rawMaterialPerUnit;
    }

    public double getQuality() {
        return quality;
    }

    public double getCumulativeFailureOdd() {
        return cumulativeFailureOdd;
    }

    public void increaseCumulativeFailureOdd(double increment) {
        this.cumulativeFailureOdd += increment;
    }

    public static int getProductsCounter() {
        return productsCounter;
    }


    protected double randomFactor() {
        return 1.2 * BigDecimal.valueOf(random.nextDouble())
                .setScale(1, RoundingMode.UP).doubleValue();
    }

}
