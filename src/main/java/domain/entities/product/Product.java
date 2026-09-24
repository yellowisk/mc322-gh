package domain.entities.product;

import domain.interfaces.Auditable;
import domain.utils.RandomProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class Product implements Auditable {
    private static int globalUniqueId = 1;
    private static final double RISK_THRESHOLD = 0.5;

    private final int id;
    private final String name;
    private ProductStatus status;
    private final double rawMaterialPerUnit;
    private final double quality;
    private double cumulativeFailureOdd;
    private static int productsCounter;

    public Product(String name, double rawMaterialPerUnit, double quality,
                   double cumulativeFailureOdd) {
        this.id = globalUniqueId++;
        this.name = name;
        this.rawMaterialPerUnit = rawMaterialPerUnit;
        this.quality = quality;
        this.cumulativeFailureOdd = cumulativeFailureOdd;
        this.status = ProductStatus.UNDERGOING;

        /* As every subclass of Product uses super, this will run for every subclass created */
        productsCounter++;
    }

    public static void resetIdCounter() {
        globalUniqueId = 1;
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

    public double getRawMaterialPerUnit() {
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

    public double getRejectionRisk() {
        return this.quality * 0.3 + this.cumulativeFailureOdd;
    }

    @Override
    public boolean needsMaintenance() {
        return getRejectionRisk() >= RISK_THRESHOLD;
    }

    @Override
    public String generateDiagnosticReport() {
        return String.format("%s #%d [%s] | Qualidade: %.2f | Risco acumulado: %.2f | Risco de rejeição: %.0f%% | %s",
                this.name, this.id, getType(), this.quality, this.cumulativeFailureOdd,
                getRejectionRisk() * 100,
                needsMaintenance() ? "Precisa de nova inspeção" : "OK!!! Eitcha!!!");
    }

    protected double randomFactor() {
        return 1.2 * BigDecimal.valueOf(RandomProvider.nextDouble())
                .setScale(1, RoundingMode.UP).doubleValue();
    }

}
