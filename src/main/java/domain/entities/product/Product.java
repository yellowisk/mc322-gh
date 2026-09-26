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
    private int batch; /* 0 = ainda não fabricado (modelos do catálogo) */
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

    public int getBatch() {
        return batch;
    }

    public void setBatch(int batch) {
        this.batch = batch;
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
        String reset = view.ConsolePrinter.RESET;
        String blue = view.ConsolePrinter.BLUE;
        String green = view.ConsolePrinter.GREEN;
        String red = view.ConsolePrinter.RED;
        String yellow = view.ConsolePrinter.YELLOW;
        String gray = view.ConsolePrinter.GRAY;

        String idStr = gray + String.format("%-5d", this.id) + reset;
        String nomeStr = gray + String.format("%-16s", this.name) + reset;
        String loteStr = blue + String.format("%-6d", this.batch) + reset;
        String qualStr = yellow + String.format("%-10.2f", this.quality) + reset;
        String riscoAcumStr = yellow + String.format("%-12.2f", this.cumulativeFailureOdd) + reset;

        String corRisco = needsMaintenance() ? red : green;
        String riscoRejStr = corRisco + String.format("%-10s", String.format("%.0f%%", getRejectionRisk() * 100)) + reset;

        String statusTexto = needsMaintenance() ? "✗ Inspeção" : "✓ OK";
        String corStatus = needsMaintenance() ? red : green;
        String statusStr = corStatus + String.format("%-20s", statusTexto) + reset;

        // Retorna a linha completa formatada
        return String.format("    %s %s %s %s %s %s %s", idStr, nomeStr, loteStr, qualStr, riscoAcumStr, riscoRejStr, statusStr);
    }

    protected double randomFactor() {
        return 1.2 * BigDecimal.valueOf(RandomProvider.nextDouble())
                .setScale(1, RoundingMode.UP).doubleValue();
    }

}
