package domain.entities.product;

import domain.interfaces.Auditable;
import domain.utils.RandomProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Produto do Mercadinho Ideal. Os objetos criados no Menu são os modelos do catálogo, e cada
 * unidade fabricada é um objeto novo criado a partir de um modelo por {@link #processar(Produto, ProductStatus)}.
 * O risco de rejeição vem da qualidade + o risco acumulado nas etapas, e decide o veredito na inspeção:
 * abaixo de 0.25 é aprovado, até 0.45 vai pro armazém precisando de manutenção, e acima disso é descartado
 *
 * @author Heitor Almeida
 */
public abstract class Produto implements Auditable {
    private static int proximoId = 1;

    private final int id;
    private final String nome;
    private ProductStatus status;
    private final double quantidadeMateriaPrimaPorUnidade;
    private final double qualidade;
    private double probabilidadeFalhaAcumulada;
    private int lote; /* 0 = ainda não fabricado (modelos do catálogo) */
    private static int totalProdutosFabricados;

    /* Abaixo de LIMITE_RISCO_MANUTENCAO é aprovado, entre os dois precisa
    de manutenção e acima de LIMITE_RISCO_DESCARTE é descartado */
    private final static double LIMITE_RISCO_MANUTENCAO = 0.25;
    private final static double LIMITE_RISCO_DESCARTE = 0.45;

    /**
     * Cria o produto c/ o próximo id e status {@link ProductStatus#UNDERGOING}
     *
     * @param nome nome do produto, ex.: "Copo"
     * @param quantidadeMateriaPrimaPorUnidade matéria-prima gasta por unidade, em kg
     * @param qualidade qualidade, entre 0 e 1. Quanto maior, maior o risco de rejeição
     * @param probabilidadeFalhaAcumulada risco acumulado inicial, normalmente 0
     */
    public Produto(String nome, double quantidadeMateriaPrimaPorUnidade, double qualidade,
                   double probabilidadeFalhaAcumulada) {
        this.id = proximoId++;
        this.nome = nome;
        this.quantidadeMateriaPrimaPorUnidade = quantidadeMateriaPrimaPorUnidade;
        this.qualidade = qualidade;
        this.probabilidadeFalhaAcumulada = probabilidadeFalhaAcumulada;
        this.status = ProductStatus.UNDERGOING;

        /* Toda subclasse de Produto chama esse construtor via super, então isso roda p/ toda instância criada */
        totalProdutosFabricados++;
    }

    /**
     * Volta o contador de id pra 1. É chamado depois de criar os modelos do catálogo,
     * pra primeira unidade fabricada ser a #1
     */
    public static void resetarContadorId() {
        proximoId = 1;
    }

    /**
     * Cria uma unidade nova do mesmo tipo a partir de um modelo do catálogo, c/ id próprio
     *
     * @param modelo modelo do catálogo, dá o nome e a matéria-prima por unidade
     * @param novoStatus status da unidade nova
     * @return unidade nova
     */
    public abstract Produto processar(Produto modelo, ProductStatus novoStatus);

    /**
     * Sorteia quanto tempo uma unidade levou pra ser fabricada: o tempo base do tipo vezes {@link #fatorAleatorio()}
     *
     * @return tempo de produção, em segundos
     */
    public abstract double calcularTempoProducao();

    /**
     * Faixa de qualidade do produto
     *
     * @return tipo, ex.: "Alta Qualidade"
     */
    public abstract String getTipo();

    /**
     * Id da unidade
     *
     * @return id, a partir de 1
     */
    public int getId() {
        return id;
    }

    /**
     * Nome do produto
     *
     * @return nome, ex.: "Kit Copo"
     */
    public String getNome() {
        return nome;
    }

    /**
     * Em que ponto da linha de produção o produto está
     *
     * @return status atual
     */
    public ProductStatus getStatus() {
        return status;
    }

    /**
     * Troca o status, usado pelas máquinas a cada etapa
     *
     * @param status novo status
     */
    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    /**
     * Matéria-prima gasta por unidade
     *
     * @return matéria-prima por unidade, em kg
     */
    public double getQuantidadeMateriaPrimaPorUnidade() {
        return quantidadeMateriaPrimaPorUnidade;
    }

    /**
     * Qualidade do produto. Quanto maior, maior o risco de rejeição na inspeção, já que os critérios
     * p/ produto de alta qualidade são mais rigorosos
     *
     * @return qualidade, entre 0 e 1
     */
    public double getQualidade() {
        return qualidade;
    }

    /**
     * Risco que o produto foi acumulando quando as máquinas falharam nas etapas anteriores
     *
     * @return risco acumulado
     */
    public double getProbabilidadeFalhaAcumulada() {
        return probabilidadeFalhaAcumulada;
    }

    /**
     * Soma risco ao produto, usado pelas máquinas quando falham no sorteio
     *
     * @param incremento quanto de risco somar
     */
    public void aumentarProbabilidadeFalha(double incremento) {
        this.probabilidadeFalhaAcumulada += incremento;
    }

    /**
     * Quantos objetos de produto já foram criados, contando os modelos do catálogo
     *
     * @return total de produtos criados
     */
    public static int getTotalProdutosFabricados() {
        return totalProdutosFabricados;
    }

    /**
     * Lote em que a unidade foi fabricada
     *
     * @return número do lote, 0 se ainda não foi fabricado (modelos do catálogo)
     */
    public int getLote() {
        return lote;
    }

    /**
     * Marca de qual lote a unidade saiu, logo depois do processamento
     *
     * @param lote número do lote
     */
    public void setLote(int lote) {
        this.lote = lote;
    }

    /**
     * Risco de o produto ser rejeitado na inspeção
     * <pre>{@code riscoRejeicao = qualidade * 0.15 + probabilidadeFalhaAcumulada}</pre>
     * Produto melhor tem inspeção mais rigorosa
     *
     * @return risco de rejeição
     */
    public double getRiscoRejeicao() {
        return this.qualidade * 0.15 + this.probabilidadeFalhaAcumulada;
    }

    /**
     * Diz se o produto passou na inspeção, mas c/ ressalva: o risco de rejeição tá entre 0.25 e 0.45
     *
     * @return true se o risco estiver na faixa de manutenção
     */
    @Override
    public boolean precisaManutencao() {
        return LIMITE_RISCO_MANUTENCAO <= getRiscoRejeicao() && getRiscoRejeicao() <= LIMITE_RISCO_DESCARTE;
    }

    /**
     * Risco alto demais: o produto é descartado na inspeção e nem vai pro armazém
     *
     * @return true se o risco de rejeição passar de 0.45
     */
    public boolean passouLimiteDescarte() {
        return getRiscoRejeicao() > LIMITE_RISCO_DESCARTE;
    }

    @Override
    public String gerarResumoProblema() {
        return String.format("[Produto] %s #%d (Risco de Rejeição: %.0f%%)", getNome(), getId(), getRiscoRejeicao() * 100);
    }

    /**
     * Linha da tabela de produtos da auditoria: id, nome, lote, qualidade, risco acumulado,
     * risco de rejeição e se precisa de inspeção
     *
     * @return linha formatada, já c/ as cores
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        String reset = view.ConsolePrinter.RESET;
        String azul = view.ConsolePrinter.AZUL;
        String verde = view.ConsolePrinter.VERDE;
        String vermelho = view.ConsolePrinter.VERMELHO;
        String amarelo = view.ConsolePrinter.AMARELO;
        String cinza = view.ConsolePrinter.CINZA;

        String idStr = cinza + String.format("%-5d", this.id) + reset;
        String nomeStr = cinza + String.format("%-16s", this.nome) + reset;
        String loteStr = azul + String.format("%-6d", this.lote) + reset;
        String qualStr = amarelo + String.format("%-10.2f", this.qualidade) + reset;
        String riscoAcumStr = amarelo + String.format("%-12.2f", this.probabilidadeFalhaAcumulada) + reset;

        String corRisco = precisaManutencao() ? vermelho : verde;
        String riscoRejStr = corRisco + String.format("%-10s", String.format("%.0f%%", getRiscoRejeicao() * 100)) + reset;

        String statusTexto = precisaManutencao() ? "✗ Inspeção" : "✓ OK";
        String corStatus = precisaManutencao() ? vermelho : verde;
        String statusStr = corStatus + String.format("%-20s", statusTexto) + reset;

        // Retorna a linha completa formatada
        return String.format("    %s %s %s %s %s %s %s", idStr, nomeStr, loteStr, qualStr, riscoAcumStr, riscoRejStr, statusStr);
    }

    /**
     * Fator aleatório pro tempo de produção, entre 0.12 e 1.2
     *
     * @return fator que multiplica o tempo base
     */
    protected double fatorAleatorio() {
        return 1.2 * BigDecimal.valueOf(RandomProvider.sortearDouble())
                .setScale(1, RoundingMode.UP).doubleValue();
    }

}
