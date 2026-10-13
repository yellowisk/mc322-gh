package domain.utils;

/**
 * Classe para armazenar as informações de um cenário. O cenário escolhido no começo define a verba
 * inicial, o multiplicador de falha das máquinas e o desgaste máximo por uso
 *
 * @author Glayson Oliveira
 */
public class ScenarioConfig {
    private final String name;
    private final String type;
    private final float budget;
    private final double scenarioMultiplier;
    private final int wearDamage;

    /**
     * Cria o cenário
     *
     * @param name nome temático, ex.: "Mercadinho Precário"
     * @param type rótulo do enunciado, ex.: "Apocalíptico"
     * @param budget verba inicial, em R$
     * @param failOdd multiplicador de falha das máquinas, ex.: 1.5
     * @param wearDamage desgaste máximo por uso das máquinas
     */
    public ScenarioConfig(String name, String type, float budget, double failOdd, int wearDamage) {
        this.name = name;
        this.type = type;
        this.budget = budget;
        this.scenarioMultiplier = failOdd;
        this.wearDamage = wearDamage;
    }

    /**
     * Verba inicial do cenário
     *
     * @return verba, em R$
     */
    public float getBudget() {
        return budget;
    }

    /**
     * Multiplicador de falha das máquinas
     *
     * @return multiplicador, ex.: 0.5 no Ideal
     */
    public double getScenarioMultiplier() {
        return scenarioMultiplier;
    }

    /**
     * Desgaste máximo por uso das máquinas
     *
     * @return desgaste máximo
     */
    public int getWearDamage() {
        return wearDamage;
    }

    /**
     * Nome temático do cenário
     *
     * @return nome, ex.: "Mercadinho Ideal"
     */
    public String getName() {
        return this.name;
    }

    /**
     * Rótulo do enunciado
     *
     * @return tipo, ex.: "Intermediário"
     */
    public String getType() {
        return this.type;
    }

    /**
     * Nome temático + rótulo do enunciado
     *
     * @return nome de exibição, ex.: "Mercadinho Precário (Apocalíptico)"
     */
    public String getDisplayName() {
        return String.format("%s (%s)", this.name, this.type);
    }
}
