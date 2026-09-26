package domain.entities.machine;

import domain.entities.product.Product;
import domain.entities.product.ProductStatus;
import domain.utils.RandomProvider;

public class InspectionMachine extends Machine {

    public InspectionMachine(String name, int maxCapacity, double failureOdd,
                             double operationCost, double scenarioMultiplier,
                             int wearDamage) {
        super(name, maxCapacity, failureOdd, operationCost, scenarioMultiplier, wearDamage);
    }

    /**
     * Decide se o produto é reprovado na etapa. Usado pela máquina de inspeção.
     * O produto é reprovado em dois casos (sorteios independentes):
     * <ul>
     *   <li>a própria máquina falha, com chance {@link #getFailureOdd()}
     *   (que já considera a saúde e o cenário);</li>
     *   <li>o risco do produto se concretiza, com chance
     *   {@link Product#getRejectionRisk()}. Quanto maior a qualidade e o
     *   risco acumulado nas etapas anteriores, maior a chance de reprovação.</li>
     * </ul>
     *
     * @param product O produto sendo inspecionado.
     * @return true se o produto deve ser reprovado.
     */
    private boolean willProductGetRejected(Product product) {
        boolean failureFloor = willRollFailureOdd();

        double rejectionOdds = product.getRejectionRisk();

        return failureFloor || (RandomProvider.chance(rejectionOdds));
    }

    @Override
    public Product processAux(Product product) {
        if (willProductGetRejected(product)) {
            product.setStatus(ProductStatus.FAILED);
            return product;
        }

        product.setStatus(ProductStatus.APPROVED);
        return product;
    }

    @Override
    public String getType() {
        return "Inspection";
    }
}
