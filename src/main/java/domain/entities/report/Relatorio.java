package domain.entities.report;

/**
 * Classe para armazenar os dados de log da fábrica. Acumula os números de todas as produções da
 * sessão (produtos aprovados, que precisam de manutenção e rejeitados, tempo, matéria-prima e dinheiro gastos)
 * p/ a auditoria geral mostrar
 *
 * @author Glayson Oliveira
 */
public class Relatorio {
    private double taxaSucessoProducao;
    private double tempoTotalProducao;
    private double materiaPrimaGastaTotal;
    private double dinheiroGasto;
    private int totalProdutosAprovados;
    private int totalProdutosPrecisamManutencao;
    private int totalProdutosRejeitados;

    /** Cria o relatório zerado, no começo da sessão */
    public Relatorio() {
    }

    /**
     * Recalcula a taxa de sucesso: aprovados dividido pelo total. Os que precisam de manutenção
     * contam no total, mas não como sucesso. É chamado no fim de cada produção
     */
    public void calcularTaxaSucesso() {
        this.taxaSucessoProducao = getTotalProdutos() == 0 ? 0 : (double) getTotalProdutosAprovados() / getTotalProdutos();
    }

    /**
     * Taxa de sucesso calculada no último {@link #calcularTaxaSucesso()}
     *
     * @return taxa entre 0 e 1, 0 se nada foi produzido ainda
     */
    public double getTaxaSucessoProducao() {
        return taxaSucessoProducao;
    }


    /**
     * Tempo somado de todas as unidades fabricadas
     *
     * @return tempo total de produção, em segundos
     */
    public double getTempoTotalProducao() {
        return tempoTotalProducao;
    }

    /**
     * Soma o tempo de uma produção
     *
     * @param tempo tempo a somar, em segundos
     */
    public void aumentarTempoTotalProducao(double tempo) {
        this.tempoTotalProducao += tempo;
    }

    /**
     * Matéria-prima consumida em todas as produções
     *
     * @return matéria-prima gasta, na unidade dela (ex.: kg)
     */
    public double getMateriaPrimaGastaTotal() {
        return materiaPrimaGastaTotal;
    }

    /**
     * Soma a matéria-prima consumida por uma unidade
     *
     * @param materiaPrimaConsumida quantidade consumida
     */
    public void aumentarMateriaPrimaGasta(double materiaPrimaConsumida) {
        this.materiaPrimaGastaTotal += materiaPrimaConsumida;
    }

    /**
     * Dinheiro gasto operando as máquinas em todas as produções
     *
     * @return valor gasto, em R$
     */
    public double getDinheiroGasto() {
        return dinheiroGasto;
    }

    /**
     * Soma o custo de operação de uma produção
     *
     * @param dinheiroGasto valor a somar, em R$
     */
    public void aumentarDinheiroGasto(double dinheiroGasto) {
        this.dinheiroGasto += dinheiroGasto;
    }

    /**
     * Total de unidades que chegaram ao fim da inspeção
     *
     * @return aprovados + precisam de manutenção + rejeitados
     */
    public int getTotalProdutos() {
        return totalProdutosAprovados + totalProdutosPrecisamManutencao + totalProdutosRejeitados;
    }

    /**
     * Unidades aprovadas na inspeção
     *
     * @return total de aprovados
     */
    public int getTotalProdutosAprovados() {
        return totalProdutosAprovados;
    }

    /**
     * Conta mais uma unidade aprovada
     */
    public void aumentarTotalProdutosAprovados() {
        this.totalProdutosAprovados++;
    }

    /**
     * Unidades aprovadas c/ ressalva, que foram pro armazém precisando de manutenção
     *
     * @return total que precisa de manutenção
     */
    public int getTotalProdutosPrecisamManutencao() {
        return totalProdutosPrecisamManutencao;
    }

    /**
     * Conta mais uma unidade que precisa de manutenção
     */
    public void aumentarTotalProdutosPrecisamManutencao() {
        this.totalProdutosPrecisamManutencao++;
    }

    /**
     * Unidades rejeitadas e descartadas na inspeção
     *
     * @return total de rejeitados
     */
    public int getTotalProdutosRejeitados() {
        return totalProdutosRejeitados;
    }

    /**
     * Conta mais uma unidade rejeitada
     */
    public void aumentarTotalProdutosRejeitados() {
        this.totalProdutosRejeitados++;
    }
}
