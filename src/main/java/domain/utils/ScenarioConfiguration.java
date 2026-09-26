package domain.utils;

public class ScenarioConfiguration {
    private final String name;
    private final float budget;
    private final double scenarioMultiplier;
    private final int wearDamage;

    public ScenarioConfiguration(String name, float budget, double failOdd, int wearDamage) {
        this.name = name;
        this.budget = budget;
        this.scenarioMultiplier = failOdd;
        this.wearDamage = wearDamage;
    }

    public float getBudget() {
        return budget;
    }

    public double getScenarioMultiplier() {
        return scenarioMultiplier;
    }

    public int getWearDamage() {
        return wearDamage;
    }

    public String getName() {
        return this.name;
    }
}
