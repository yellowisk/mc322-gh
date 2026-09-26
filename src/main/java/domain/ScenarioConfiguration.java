package domain;

public class ScenarioConfiguration {
    private final String name;
    private final float budget;
    private final double scenarioMultiplier;
    private final int waarDamage;

    public ScenarioConfiguration(String name, float budget, double failOdd, int waarDamage) {
        this.name = name;
        this.budget = budget;
        this.scenarioMultiplier = failOdd;
        this.waarDamage = waarDamage;
    }

    public float getBudget() {
        return budget;
    }

    public double getScenarioMultiplier() {
        return scenarioMultiplier;
    }

    public int getWaarDamage() {
        return waarDamage;
    }

    public String getName() {
        return this.name;
    }
}
