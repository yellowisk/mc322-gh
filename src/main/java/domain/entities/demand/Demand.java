package domain.entities.demand;

import domain.entities.product.Product;
import domain.exceptions.InvalidDemandAmountException;
import domain.exceptions.InvalidDemandTransitionException;

/**
 * Pedido de um produto do Mercadinho com infos como: quantas unidades fabricar, quanto já saiu
 * e em que status está.
 *
 * @author Heitor Almeida
 */
public class Demand {
    /**
     * Próxima ordem de chegada a ser entregue. Compartilhado por todas as demandas, cada vez que uma
     * demanda é criada ou atualizada ela pega o valor atual em {@link #arrivalOrder} e o contador sobe 1 pelo nextArrival.
     */
    private static long nextArrival = 0;

    private final String productName;
    private int amount;
    private DemandStatus status;
    private int producedAmount;
    private double totalRawMaterial;
    private double totalProductionTime;
    private double estimatedCost;
    private long arrivalOrder;

    /**
     * Cria a demanda já como {@link DemandStatus#PENDING} e c/ a ordem de chegada mais recente
     *
     * @param product modelo do catálogo, dá o nome e a matéria-prima por unidade
     * @param amount quantas unidades fabricar
     */
    public Demand(Product product, int amount) {
        this.productName = product.getName();
        this.amount = amount;
        this.status = DemandStatus.PENDING;
        this.producedAmount = 0;
        this.totalRawMaterial = calcRawMaterialNeeded(product);
        this.totalProductionTime = 0.0;
        this.arrivalOrder = nextArrival++;
    }

    /**
     * Matéria-prima necessária para fabricar a quantidade pedida
     *
     * @param product modelo do catálogo, dá a matéria-prima por unidade
     * @return matéria-prima total
     */
    private double calcRawMaterialNeeded(Product product) {
        // for those who just joined the stream: "calc" is short for "calculate"
        return (product.getRawMaterialPerUnit() * this.amount);
    }

    /**
     * Matéria-prima total que a demanda inteira consome
     *
     * @return matéria-prima necessária
     */
    public double getTotalRawMaterial() {
        return totalRawMaterial;
    }

    /**
     * Nome do produto pedido
     *
     * @return nome do produto, ex.: "Kit Copo"
     */
    public String getProductName() {
        return this.productName;
    }

    /**
     * Total de unidades pedidas
     *
     * @return quantidade pedida
     */
    public int getAmount() {
        return this.amount;
    }

    /**
     * Status atual da demanda
     *
     * @return status atual
     */
    public DemandStatus getStatus() {
        return this.status;
    }

    /**
     * Unidades que já passaram pela linha de produção
     *
     * @return quantidade já fabricada
     */
    public int getProducedAmount() {
        return this.producedAmount;
    }

    /**
     * Unidades que ainda faltam. Como uma demanda cancelada mantém o progresso, então é isso que uma retomada fabrica
     *
     * @return quantidade pedida menos a já fabricada
     */
    public int getRemainingAmount() {
        return this.amount - this.producedAmount;
    }

    /**
     * Troca a quantidade pedida e recalcula a matéria-prima. A demanda é resetada e ganha uma nova ordem de chegada
     *
     * @param product modelo do catálogo, p/ recalcular a matéria-prima
     * @param newAmount nova quantidade, 0 ou mais
     * @throws InvalidDemandAmountException se {@code newAmount} for negativo
     */
    public void updateAmount(Product product, int newAmount) {
        if (newAmount < 0) {
            throw new InvalidDemandAmountException("A demanda não pode ser negativa!");
        }
        this.amount = newAmount;
        this.totalRawMaterial = calcRawMaterialNeeded(product);
        this.arrivalOrder = nextArrival++;
        reset();
    }

    /**
     * Ordem de chegada da demanda, usada pela {@link domain.strategies.FilaDoCaixa}
     *
     * @return ordem de chegada. Menor = chegou antes
     */
    public long getArrivalOrder() {
        return this.arrivalOrder;
    }

    /**
     * Tempo somado de todas as unidades já fabricadas
     *
     * @return tempo total de produção, em segundos
     */
    public double getTotalProductionTime() {
        return totalProductionTime;
    }

    /**
     * Último custo calculado por {@link #updateEstimatedCost(double)}
     *
     * @return custo estimado do que falta fabricar, em R$
     */
    public double getEstimatedCost() {
        return estimatedCost;
    }

    /**
     * Recalcula o custo do que falta fabricar. Tem que ser chamado c/ um unitOperationCost atualizado
     *
     * @param unitOperationCost custo de operação das máquinas p/ fabricar uma unidade, em R$
     */
    public void updateEstimatedCost(double unitOperationCost) {
        this.estimatedCost = getRemainingAmount() * unitOperationCost;
    }

    /**
     * Diz se o orçamento cobre o custo estimado
     *
     * @param availableBudget orçamento disponível, em R$
     * @return true se o custo estimado cabe no orçamento
     */
    public boolean isViable(double availableBudget) {
        return this.estimatedCost <= availableBudget;
    }

    /**
     * Passa a demanda pra {@link DemandStatus#IN_PRODUCTION}
     *
     * @throws InvalidDemandTransitionException se o status atual não permitir
     */
    public void startProduction() {
        transitionTo(DemandStatus.IN_PRODUCTION);
    }

    /**
     * Finaliza a demanda como {@link DemandStatus#COMPLETED} e soma o progresso desta rodada
     *
     * @param producedNow unidades fabricadas nesta rodada
     * @param productionTimeNow tempo gasto nesta rodada, em segundos
     * @throws InvalidDemandTransitionException se o status atual não permitir
     */
    public void fulfill(int producedNow, double productionTimeNow) {
        transitionTo(DemandStatus.COMPLETED);
        addProgress(producedNow, productionTimeNow);
    }

    /**
     * Cancela a demanda como {@link DemandStatus#CANCELLED}, mas guarda o que já foi feito nesta rodada
     *
     * @param producedNow unidades fabricadas antes de parar
     * @param productionTimeNow tempo gasto antes de parar, em segundos
     * @throws InvalidDemandTransitionException se o status atual não permitir
     */
    public void cancel(int producedNow, double productionTimeNow) {
        transitionTo(DemandStatus.CANCELLED);
        addProgress(producedNow, productionTimeNow);
    }

    /**
     * Soma o que saiu numa rodada de produção ao progresso da demanda
     *
     * @param producedNow unidades fabricadas na rodada
     * @param productionTimeNow tempo gasto na rodada, em segundos
     */
    private void addProgress(int producedNow, double productionTimeNow) {
        this.producedAmount += producedNow;
        this.totalProductionTime += productionTimeNow;
    }

    /** Volta pra {@link DemandStatus#PENDING} e zera o progresso */
    public void reset() {
        transitionTo(DemandStatus.PENDING);
        this.producedAmount = 0;
        this.totalProductionTime = 0;
    }

    /**
     * Troca o status, mas só se {@link DemandStatus#canTransitionTo(DemandStatus)} deixar
     *
     * @param target novo status
     * @throws InvalidDemandTransitionException se a transição não for permitida
     */
    private void transitionTo(DemandStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidDemandTransitionException("Transição de status inválida para "
                    + this.productName + ": " + this.status.getDescription() + " -> " + target.getDescription());
        }
        this.status = target;
    }
}
