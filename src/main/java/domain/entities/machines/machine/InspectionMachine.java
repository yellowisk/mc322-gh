package domain.entities.machines.machine;

import domain.entities.product.Product;

public class InspectionMachine extends Machine {

    public InspectionMachine(String name, int maxCapacity, float failureOdd,
                             float operationCost) {
        super(name, maxCapacity, failureOdd, operationCost);
    }

    @Override
    public void process(Product product) {
        if (!isOn()) {
            throw new IllegalStateException("[Eitcha, João...] The machine can't proccess anything, since it ain't on!");
        }

        boolean failureFloor = checkFailure();

        /* The greate the quality, thej gratear the rejection odds.
        The greater the cumulativeFailureOdd, the greater the rejection odds */
//        double chanceRejeicao = product.getQuality() * 0.3 + product.getCumulativeFailureOdd();

//        boolean isFailure = failureFloor || (random.nextDouble() < chanceRejeicao);


    }
}
