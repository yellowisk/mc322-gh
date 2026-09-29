package domain.entities.report;

import domain.entities.productionmanager.ProductionManager;

public class Report {
    private double productionSuccessRate;
    private double totalProductionTime;
    private double totalRawMaterialSpent;
    private double moneySpent;
    private int totalApprovedProducts;
    private int totalNeedsMaintenanceProducts;
    private int totalRejectedProducts;

    public void calcProdSuccessRate() {
        this.productionSuccessRate = getTotalProducts() == 0 ? 0 : (double) getTotalApprovedProducts() / getTotalProducts();
    }

    public double getProductionSuccessRate() {
        return productionSuccessRate;
    }


    public double getTotalProductionTime() {
        return totalProductionTime;
    }

    public void increaseTotalProductionTime(double time) {
        this.totalProductionTime += time;
    }

    public double getTotalRawMaterialSpent() {
        return totalRawMaterialSpent;
    }

    public void increaseRawMaterialSpent(double lostRawMaterial) {
        this.totalRawMaterialSpent += lostRawMaterial;
    }

    public double getMoneySpent() {
        return moneySpent;
    }

    public void increaseMoneySpent(double moneySpent) {
        this.moneySpent += moneySpent;
    }

    public int getTotalProducts() {
        return totalApprovedProducts + totalNeedsMaintenanceProducts + totalRejectedProducts;
    }

    public int getTotalApprovedProducts() {
        return totalApprovedProducts;
    }

    public void increaseTotalApprovedProducts() {
        this.totalApprovedProducts++;
    }

    public int getTotalNeedsMaintenanceProducts() {
        return totalNeedsMaintenanceProducts;
    }

    public void increaseTotalNeedsMaintenanceProducts() {
        this.totalNeedsMaintenanceProducts++;
    }

    public int getTotalRejectedProducts() {
        return totalRejectedProducts;
    }

    public void increaseTotalRejectedProducts() {
        this.totalRejectedProducts++;
    }
}
