package domain.utils;

public class ScenarioConfig {
    private final String name;
    private final String type;
    private final float budget;
    private final double scenarioMultiplier;
    private final int wearDamage;

    public ScenarioConfig(String name, String type, float budget, double failOdd, int wearDamage) {
        this.name = name;
        this.type = type;
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

    public String getType() {
        return this.type;
    }

    /** Nome temático + rótulo do enunciado, e.g. "Mercadinho Precário (Apocalíptico)" */
    public String getDisplayName() {
        return String.format("%s (%s)", this.name, this.type);
    }
}
