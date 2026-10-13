package domain.entities.product;

/**
 * Copo de vidro, o produto de baixa qualidade (0.4) e mais rápido de fazer (tempo base de 10s)
 *
 * @author Heitor Almeida
 */
public class CopoDeVidro extends Product {

    /**
     * Cria o copo c/ a qualidade fixa do tipo e risco acumulado 0
     *
     * @param nome nome que aparece nos menus
     * @param quantidadeMateriaPrimaPorUnidade matéria-prima gasta por unidade, em kg
     */
    public CopoDeVidro(String nome, double quantidadeMateriaPrimaPorUnidade) {
        super(nome, quantidadeMateriaPrimaPorUnidade, 0.4, 0.0);
    }

    @Override
    public Product processar(Product modelo, ProductStatus novoStatus) {
        Product p = new CopoDeVidro(modelo.getNome(), modelo.getQuantidadeMateriaPrimaPorUnidade());
        p.setStatus(novoStatus);
        return p;
    }

    @Override
    public double calcularTempoProducao() {
        double tempoBase = 10.0;
        return tempoBase * fatorAleatorio();
    }

    @Override
    public String getTipo() {
        return "Baixa Qualidade";
    }
}