package domain.entities.product;

/**
 * Pote de vidro, o produto de média qualidade (0.7), c/ tempo base de 20s
 *
 * @author Heitor Almeida
 */
public class PoteDeVidro extends Product {
    /**
     * Cria o pote c/ a qualidade fixa do tipo e risco acumulado 0
     *
     * @param nome nome que aparece nos menus
     * @param quantidadeMateriaPrimaPorUnidade matéria-prima gasta por unidade, em kg
     */
    public PoteDeVidro(String nome, double quantidadeMateriaPrimaPorUnidade) {
        super(nome, quantidadeMateriaPrimaPorUnidade, 0.7, 0.0);
    }

    @Override
    public Product processar(Product modelo, ProductStatus novoStatus) {
        Product p = new PoteDeVidro(modelo.getNome(), modelo.getQuantidadeMateriaPrimaPorUnidade());
        p.setStatus(novoStatus);
        return p;
    }

    @Override
    public double calcularTempoProducao() {
        double tempoBase = 20.0;
        return tempoBase * fatorAleatorio();
    }

    @Override
    public String getTipo() {
        return "Média Qualidade";
    }
}
