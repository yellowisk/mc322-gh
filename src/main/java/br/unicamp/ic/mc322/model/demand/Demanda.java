package br.unicamp.ic.mc322.model.demand;

import br.unicamp.ic.mc322.model.product.Produto;
import br.unicamp.ic.mc322.exception.QuantidadeDemandaInvalidaException;
import br.unicamp.ic.mc322.exception.TransicaoDemandaInvalidaException;

/**
 * Pedido de um produto do Mercadinho com infos como: quantas unidades fabricar, quanto já saiu
 * e em que status está.
 *
 * @author Heitor Almeida
 */
public class Demanda {
    /**
     * Próxima ordem de chegada a ser entregue. Compartilhado por todas as demandas, cada vez que uma
     * demanda é criada ou atualizada ela pega o valor atual em {@link #ordemChegada} e o contador sobe 1 pelo proximaChegada.
     */
    private static long proximaChegada = 0;

    private final String nomeProduto;
    private int quantidade;
    private StatusDemanda status;
    private int quantidadeProduzida;
    private double materiaPrimaTotal;
    private double tempoTotalProducao;
    private double custoEstimado;
    private long ordemChegada;

    /**
     * Cria a demanda já como {@link StatusDemanda#PENDENTE} e c/ a ordem de chegada mais recente
     *
     * @param produto modelo do catálogo, dá o nome e a matéria-prima por unidade
     * @param quantidade quantas unidades fabricar
     */
    public Demanda(Produto produto, int quantidade) {
        this(produto, quantidade, proximaChegada++);
    }

    /**
     * Cria a demanda como {@link StatusDemanda#PENDENTE} c/ uma ordem de chegada já conhecida
     *
     * @param produto modelo do catálogo, dá o nome e a matéria-prima por unidade
     * @param quantidade quantas unidades fabricar
     * @param ordemChegada ordem de chegada
     */
    private Demanda(Produto produto, int quantidade, long ordemChegada) {
        this.nomeProduto = produto.getNome();
        this.quantidade = quantidade;
        this.status = StatusDemanda.PENDENTE;
        this.quantidadeProduzida = 0;
        this.materiaPrimaTotal = calcularMateriaPrimaNecessaria(produto);
        this.tempoTotalProducao = 0.0;
        this.ordemChegada = ordemChegada;
    }

    /**
     * Recria uma demanda salva antes, c/ o progresso e o status dela.
     * Não mexe no contador de chegada, quem ajusta ele é quem passa a usar as demandas
     *
     * @param produto modelo do catálogo
     * @param quantidade quantas unidades fabricar
     * @param quantidadeProduzida quantas já foram fabricadas
     * @param status status salvo
     * @param tempoTotalProducao tempo já gasto, em segundos
     * @param ordemChegada ordem de chegada salva
     * @return a demanda recriada
     * @throws QuantidadeDemandaInvalidaException se a quantidade for negativa
     * @throws IllegalArgumentException se o progresso, o tempo ou a ordem forem negativos, ou se não baterem c/ o status
     */
    public static Demanda restaurar(Produto produto, int quantidade, int quantidadeProduzida, StatusDemanda status,
                                    double tempoTotalProducao, long ordemChegada) {
        if (quantidade < 0) {
            throw new QuantidadeDemandaInvalidaException("A demanda não pode ser negativa!");
        }
        if (quantidadeProduzida < 0 || !(tempoTotalProducao >= 0) || ordemChegada < 0) {
            throw new IllegalArgumentException("Dessa vez não é! A demanda de " + produto.getNome()
                    + " tem progresso, tempo ou ordem de chegada negativos.");
        }
        boolean coerente = switch (status) {
            case PENDENTE -> quantidadeProduzida == 0 && tempoTotalProducao == 0;
            case CANCELADA -> quantidadeProduzida < quantidade;
            case CONCLUIDA -> quantidadeProduzida == quantidade;
            case EM_PRODUCAO -> false;
        };
        if (!coerente) {
            throw new IllegalArgumentException(String.format(
                    "Dessa vez não é! A demanda de %s não pode estar %s c/ %d de %d unidades produzidas.",
                    produto.getNome(), status.getDescricao(), quantidadeProduzida, quantidade));
        }

        Demanda demanda = new Demanda(produto, quantidade, ordemChegada);
        demanda.status = status;
        demanda.quantidadeProduzida = quantidadeProduzida;
        demanda.tempoTotalProducao = tempoTotalProducao;
        return demanda;
    }

    /**
     * Matéria-prima necessária para fabricar a quantidade pedida
     *
     * @param produto modelo do catálogo, dá a matéria-prima por unidade
     * @return matéria-prima total
     */
    private double calcularMateriaPrimaNecessaria(Produto produto) {
        return (produto.getQuantidadeMateriaPrimaPorUnidade() * this.quantidade);
    }

    /**
     * Matéria-prima total que a demanda inteira consome
     *
     * @return matéria-prima necessária
     */
    public double getMateriaPrimaTotal() {
        return materiaPrimaTotal;
    }

    /**
     * Nome do produto pedido
     *
     * @return nome do produto, ex.: "Kit Copo"
     */
    public String getNomeProduto() {
        return this.nomeProduto;
    }

    /**
     * Total de unidades pedidas
     *
     * @return quantidade pedida
     */
    public int getQuantidade() {
        return this.quantidade;
    }

    /**
     * Status atual da demanda
     *
     * @return status atual
     */
    public StatusDemanda getStatus() {
        return this.status;
    }

    /**
     * Unidades que já passaram pela linha de produção
     *
     * @return quantidade já fabricada
     */
    public int getQuantidadeProduzida() {
        return this.quantidadeProduzida;
    }

    /**
     * Unidades que ainda faltam. Como uma demanda cancelada mantém o progresso, então é isso que uma retomada fabrica
     *
     * @return quantidade pedida menos a já fabricada
     */
    public int getQuantidadeRestante() {
        return this.quantidade - this.quantidadeProduzida;
    }

    /**
     * Troca a quantidade pedida e recalcula a matéria-prima. A demanda é resetada e ganha uma nova ordem de chegada
     *
     * @param produto modelo do catálogo, p/ recalcular a matéria-prima
     * @param novaQuantidade nova quantidade, 0 ou mais
     * @throws QuantidadeDemandaInvalidaException se {@code novaQuantidade} for negativo
     */
    public void atualizarQuantidade(Produto produto, int novaQuantidade) {
        if (novaQuantidade < 0) {
            throw new QuantidadeDemandaInvalidaException("A demanda não pode ser negativa!");
        }
        this.quantidade = novaQuantidade;
        this.materiaPrimaTotal = calcularMateriaPrimaNecessaria(produto);
        this.ordemChegada = proximaChegada++;
        resetar();
    }

    /**
     * Ordem de chegada da demanda, usada pela {@link br.unicamp.ic.mc322.strategy.FilaDoCaixa}
     *
     * @return ordem de chegada. Menor = chegou antes
     */
    public long getOrdemChegada() {
        return this.ordemChegada;
    }

    /**
     * Tempo somado de todas as unidades já fabricadas
     *
     * @return tempo total de produção, em segundos
     */
    public double getTempoTotalProducao() {
        return tempoTotalProducao;
    }

    /**
     * Último custo calculado por {@link #atualizarCustoEstimado(double)}
     *
     * @return custo estimado do que falta fabricar, em R$
     */
    public double getCustoEstimado() {
        return custoEstimado;
    }

    /**
     * Recalcula o custo do que falta fabricar. Tem que ser chamado c/ um custoOperacaoUnitario atualizado
     *
     * @param custoOperacaoUnitario custo de operação das máquinas p/ fabricar uma unidade, em R$
     */
    public void atualizarCustoEstimado(double custoOperacaoUnitario) {
        this.custoEstimado = getQuantidadeRestante() * custoOperacaoUnitario;
    }

    /**
     * Diz se o orçamento cobre o custo estimado
     *
     * @param orcamentoDisponivel orçamento disponível, em R$
     * @return true se o custo estimado cabe no orçamento
     */
    public boolean ehViavel(double orcamentoDisponivel) {
        return this.custoEstimado <= orcamentoDisponivel;
    }

    /**
     * Define a ordem de chegada da próxima demanda criada. Usado quando uma fábrica passa a ser usada,
     * p/ as demandas novas chegarem depois das que ela já tem
     *
     * @param valor próxima ordem de chegada
     */
    public static void ajustarProximaChegada(long valor) {
        proximaChegada = valor;
    }

    /**
     * Passa a demanda pra {@link StatusDemanda#EM_PRODUCAO}
     *
     * @throws TransicaoDemandaInvalidaException se o status atual não permitir
     */
    public void iniciarProducao() {
        transicionarPara(StatusDemanda.EM_PRODUCAO);
    }

    /**
     * Finaliza a demanda como {@link StatusDemanda#CONCLUIDA} e soma o progresso desta rodada
     *
     * @param produzidasAgora unidades fabricadas nesta rodada
     * @param tempoProducaoAgora tempo gasto nesta rodada, em segundos
     * @throws TransicaoDemandaInvalidaException se o status atual não permitir
     */
    public void concluir(int produzidasAgora, double tempoProducaoAgora) {
        transicionarPara(StatusDemanda.CONCLUIDA);
        somarProgresso(produzidasAgora, tempoProducaoAgora);
    }

    /**
     * Cancela a demanda como {@link StatusDemanda#CANCELADA}, mas guarda o que já foi feito nesta rodada
     *
     * @param produzidasAgora unidades fabricadas antes de parar
     * @param tempoProducaoAgora tempo gasto antes de parar, em segundos
     * @throws TransicaoDemandaInvalidaException se o status atual não permitir
     */
    public void cancelar(int produzidasAgora, double tempoProducaoAgora) {
        transicionarPara(StatusDemanda.CANCELADA);
        somarProgresso(produzidasAgora, tempoProducaoAgora);
    }

    /**
     * Soma o que saiu numa rodada de produção ao progresso da demanda
     *
     * @param produzidasAgora unidades fabricadas na rodada
     * @param tempoProducaoAgora tempo gasto na rodada, em segundos
     */
    private void somarProgresso(int produzidasAgora, double tempoProducaoAgora) {
        this.quantidadeProduzida += produzidasAgora;
        this.tempoTotalProducao += tempoProducaoAgora;
    }

    /** Volta pra {@link StatusDemanda#PENDENTE} e zera o progresso */
    public void resetar() {
        transicionarPara(StatusDemanda.PENDENTE);
        this.quantidadeProduzida = 0;
        this.tempoTotalProducao = 0;
    }

    /**
     * Troca o status, mas só se {@link StatusDemanda#podeTransicionarPara(StatusDemanda)} deixar
     *
     * @param alvo novo status
     * @throws TransicaoDemandaInvalidaException se a transição não for permitida
     */
    private void transicionarPara(StatusDemanda alvo) {
        if (!this.status.podeTransicionarPara(alvo)) {
            throw new TransicaoDemandaInvalidaException("Transição de status inválida para "
                    + this.nomeProduto + ": " + this.status.getDescricao() + " -> " + alvo.getDescricao());
        }
        this.status = alvo;
    }
}
