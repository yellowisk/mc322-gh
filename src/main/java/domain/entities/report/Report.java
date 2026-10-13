package domain.entities.report;

/**
 * Classe para armazenar os dados de log da fábrica. Acumula os números de todas as produções da
 * sessão (produtos aprovados, que precisam de manutenção e rejeitados, tempo, matéria-prima e dinheiro gastos)
 * p/ a auditoria geral mostrar
 *
 * @author Glayson Oliveira
 */
public class Report {
    private double productionSuccessRate;
    private double totalProductionTime;
    private double totalRawMaterialSpent;
    private double moneySpent;
    private int totalApprovedProducts;
    private int totalNeedsMaintenanceProducts;
    private int totalRejectedProducts;

    /**
     * Recalcula a taxa de sucesso: aprovados dividido pelo total. Os que precisam de manutenção
     * contam no total, mas não como sucesso. É chamado no fim de cada produção
     */
    public void calcProdSuccessRate() {
        this.productionSuccessRate = getTotalProducts() == 0 ? 0 : (double) getTotalApprovedProducts() / getTotalProducts();
    }

    /**
     * Taxa de sucesso calculada no último {@link #calcProdSuccessRate()}
     *
     * @return taxa entre 0 e 1, 0 se nada foi produzido ainda
     */
    public double getProductionSuccessRate() {
        return productionSuccessRate;
    }


    /**
     * Tempo somado de todas as unidades fabricadas
     *
     * @return tempo total de produção, em segundos
     */
    public double getTotalProductionTime() {
        return totalProductionTime;
    }

    /**
     * Soma o tempo de uma produção
     *
     * @param time tempo a somar, em segundos
     */
    public void increaseTotalProductionTime(double time) {
        this.totalProductionTime += time;
    }

    /**
     * Matéria-prima consumida em todas as produções
     *
     * @return matéria-prima gasta, na unidade dela (ex.: kg)
     */
    public double getTotalRawMaterialSpent() {
        return totalRawMaterialSpent;
    }

    /**
     * Soma a matéria-prima consumida por uma unidade
     *
     * @param lostRawMaterial quantidade consumida
     */
    public void increaseRawMaterialSpent(double lostRawMaterial) {
        this.totalRawMaterialSpent += lostRawMaterial;
    }

    /**
     * Dinheiro gasto operando as máquinas em todas as produções
     *
     * @return valor gasto, em R$
     */
    public double getMoneySpent() {
        return moneySpent;
    }

    /**
     * Soma o custo de operação de uma produção
     *
     * @param moneySpent valor a somar, em R$
     */
    public void increaseMoneySpent(double moneySpent) {
        this.moneySpent += moneySpent;
    }

    /**
     * Total de unidades que chegaram ao fim da inspeção
     *
     * @return aprovados + precisam de manutenção + rejeitados
     */
    public int getTotalProducts() {
        return totalApprovedProducts + totalNeedsMaintenanceProducts + totalRejectedProducts;
    }

    /**
     * Unidades aprovadas na inspeção
     *
     * @return total de aprovados
     */
    public int getTotalApprovedProducts() {
        return totalApprovedProducts;
    }

    /**
     * Conta mais uma unidade aprovada
     */
    public void increaseTotalApprovedProducts() {
        this.totalApprovedProducts++;
    }

    /**
     * Unidades aprovadas c/ ressalva, que foram pro armazém precisando de manutenção
     *
     * @return total que precisa de manutenção
     */
    public int getTotalNeedsMaintenanceProducts() {
        return totalNeedsMaintenanceProducts;
    }

    /**
     * Conta mais uma unidade que precisa de manutenção
     */
    public void increaseTotalNeedsMaintenanceProducts() {
        this.totalNeedsMaintenanceProducts++;
    }

    /**
     * Unidades rejeitadas e descartadas na inspeção
     *
     * @return total de rejeitados
     */
    public int getTotalRejectedProducts() {
        return totalRejectedProducts;
    }

    /**
     * Conta mais uma unidade rejeitada
     */
    public void increaseTotalRejectedProducts() {
        this.totalRejectedProducts++;
    }
}
