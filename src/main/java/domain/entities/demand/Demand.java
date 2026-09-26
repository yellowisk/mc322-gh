package domain.entities.demand;

import domain.entities.product.Product;
import domain.exceptions.InvalidDemandTransitionException;

public class Demand {
    /** Global arrival counter, stamped whenever a demand is (re)placed */
    private static long nextArrival = 0;

    private final String productName;
    private int amount;
    private DemandStatus status;
    private int producedAmount;
    private double totalRawMaterial;
    private double totalProductionTime;
    private double estimatedCost;
    private long arrivalOrder;

    public Demand(Product product, int amount) {
        this.productName = product.getName();
        this.amount = amount;
        this.status = DemandStatus.PENDING;
        this.producedAmount = 0;
        this.totalRawMaterial = calcRawMaterialNeeded(product);
        this.totalProductionTime = 0.0;
        this.arrivalOrder = nextArrival++;
    }

    private double calcRawMaterialNeeded(Product product) {
        // for those who just joined the stream: "calc" is short for "calculate"
        return (product.getRawMaterialPerUnit() * this.amount);
    }

    public double getTotalRawMaterial() {
        return totalRawMaterial;
    }

    public String getProductName() {
        return this.productName;
    }

    public int getAmount() {
        return this.amount;
    }

    public DemandStatus getStatus() {
        return this.status;
    }

    public int getProducedAmount() {
        return this.producedAmount;
    }

    /** Units still missing. A cancelled demand keeps its progress, so this is what a resume makes */
    public int getRemainingAmount() {
        return this.amount - this.producedAmount;
    }

    public void updateAmount(Product product, int newAmount) {
        if (newAmount < 0) {
            throw new IllegalArgumentException("A demanda não pode ser negativa!");
        }
        this.amount = newAmount;
        this.totalRawMaterial = calcRawMaterialNeeded(product);
        this.arrivalOrder = nextArrival++;
        reset();
    }

    public long getArrivalOrder() {
        return this.arrivalOrder;
    }

    public double getTotalProductionTime() {
        return totalProductionTime;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    /** Must be called with a fresh unitOperationCost */
    public void updateEstimatedCost(double unitOperationCost) {
        this.estimatedCost = getRemainingAmount() * unitOperationCost;
    }

    public boolean isViable(double availableBudget) {
        return this.estimatedCost <= availableBudget;
    }

    public void startProduction() {
        transitionTo(DemandStatus.IN_PRODUCTION);
    }

    public void fulfill(int producedNow, double productionTimeNow) {
        transitionTo(DemandStatus.COMPLETED);
        addProgress(producedNow, productionTimeNow);
    }

    public void cancel(int producedNow, double productionTimeNow) {
        transitionTo(DemandStatus.CANCELLED);
        addProgress(producedNow, productionTimeNow);
    }

    private void addProgress(int producedNow, double productionTimeNow) {
        this.producedAmount += producedNow;
        this.totalProductionTime += productionTimeNow;
    }

    public void reset() {
        transitionTo(DemandStatus.PENDING);
        this.producedAmount = 0;
        this.totalProductionTime = 0;
    }

    private void transitionTo(DemandStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidDemandTransitionException("Transição de status inválida para "
                    + this.productName + ": " + this.status.getDescription() + " -> " + target.getDescription());
        }
        this.status = target;
    }
}
