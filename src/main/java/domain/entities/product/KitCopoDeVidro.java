package domain.entities.product;

/**
 * Kit de copos de vidro, o produto de alta qualidade (1.0) e mais demorado (tempo base de 30s)
 *
 * @author Heitor Almeida
 */
public class KitCopoDeVidro extends Product {

    /**
     * Cria o kit c/ a qualidade fixa do tipo e risco acumulado 0
     *
     * @param nome nome que aparece nos menus
     * @param quantidadeMateriaPrimaPorUnidade matéria-prima gasta por unidade, em kg
     */
    public KitCopoDeVidro(String nome, double quantidadeMateriaPrimaPorUnidade) {
        super(nome, quantidadeMateriaPrimaPorUnidade, 1.0, 0.0);
    }

    @Override
    public Product processar(Product modelo, ProductStatus novoStatus) {
        Product p = new KitCopoDeVidro(modelo.getNome(), modelo.getQuantidadeMateriaPrimaPorUnidade());
        p.setStatus(novoStatus);
        return p;
    }

    @Override
    public double calcularTempoProducao() {
        double tempoBase = 30.0;
        return tempoBase * fatorAleatorio();
    }

    @Override
    public String getTipo() {
        return "Alta Qualidade";
    }
}
