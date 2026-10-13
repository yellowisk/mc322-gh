package domain.entities.conveyor;

import domain.entities.product.Produto;
import domain.exceptions.MaquinaIndisponivelException;

/**
 * Esteira que leva a matéria-prima e os produtos entre as máquinas. Carrega uma coisa por vez e só funciona ligada
 *
 * @author Glayson Oliveira
 * @author Heitor Almeida
 */
public class Esteira {
    // Mandatory attributes
    private final String nome;
    private Produto produto;
    private double materiaPrima;
    private boolean ligada; // emMovimento
    private final int capacidadeMaxima;

    /**
     * Cria a esteira desligada e vazia
     *
     * @param nome nome, ex.: "Esteira Ligeira"
     * @param capacidadeMaxima peso máximo que ela carrega, em kg
     * @throws IllegalArgumentException se a capacidade não for positiva
     */
    public Esteira(String nome, int capacidadeMaxima) {
        if (capacidadeMaxima <= 0) {
            throw new IllegalArgumentException("Dessa vez não é! A capacidade da esteira tem que ser positiva.");
        }
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
    }

    // Mandatory methods
    /**
     * Desliga a esteira
     */
    public void desligar() {
        this.ligada = false;
    }

    /**
     * Liga a esteira
     */
    public void ligar() {
        this.ligada = true;
    }

    /**
     * Verifica se a esteira consegue carregar a quantidade especificada de
     * matéria prima
     * @param quantidade quantidade
     */
    private void verificarPodeCarregar(double quantidade) {
        if (!this.ligada) {
            throw new MaquinaIndisponivelException("Dessa vez não é! A esteira está desligada.");
        }
        if (this.materiaPrima > 0 || this.produto != null) {
            throw new MaquinaIndisponivelException("Dessa vez não é! A esteira já está ocupada.");
        }
        if (!verificarCapacidade(quantidade)) {
            throw new IllegalArgumentException("Dessa vez não é! A esteira não aguenta esse peso.");
        }
    }

    // >>>>> RECURSO
    /**
     * Coloca matéria-prima na esteira
     *
     * @param quantidade quantidade a carregar
     * @throws IllegalStateException se a esteira estiver desligada ou já ocupada
     * @throws IllegalArgumentException se a quantidade passar da capacidade
     */
    public void adicionarMateriaPrima(double quantidade) {
        verificarPodeCarregar(quantidade);
        this.materiaPrima = quantidade;
    }

    /**
     * Remove a matéria prima da esteira retornando a quantidade
     * do material
     * @return quantidade de matéria prima da esteira
     * @throws domain.exceptions.MaquinaIndisponivelException se a esteira estiver desligada
     * @throws IllegalStateException se não tiver matéria-prima na esteira
     */
    public double removerMateriaPrima() {
        if (!estaLigada()) {
            throw new MaquinaIndisponivelException("Dessa vez não é! A esteira está desligada.");
        }

        if (this.materiaPrima == 0) {
            throw new IllegalStateException("Dessa vez não é! Não tem matéria-prima na esteira.");
        }

        double n = this.materiaPrima;
        this.materiaPrima = 0;
        return n;
    }

    // >>>>> PRODUTO

    /**
     * Adiciona o produto na esteira
     * @param produto o produto a ser adicionado
     * @throws IllegalStateException se a esteira estiver desligada ou já ocupada
     * @throws IllegalArgumentException se o produto passar da capacidade
     */
    public void adicionarProduto(Produto produto) {
        verificarPodeCarregar(produto.getQuantidadeMateriaPrimaPorUnidade());
        this.produto = produto;
    }

    /**
     * Remove o produto da esteira, retornando-o
     * @return o produto
     * @throws domain.exceptions.MaquinaIndisponivelException se a esteira estiver desligada
     * @throws IllegalArgumentException se não tiver produto na esteira
     */
    public Produto removerProduto() {
        if (!estaLigada()) {
            throw new MaquinaIndisponivelException("Não é mais ligeira... A esteira está desligada.");
        }

        if (this.produto == null) {
            throw new IllegalArgumentException("Dessa vez não é! Não tem produto na esteira.");
        }


        Produto p = this.produto;
        this.produto = null;
        return p;
    }

    /**
     * Verifica se a esteira suporta o peso fornecido
     * @param peso o peso a ser verificado
     * @return true ou false
     */
    private boolean verificarCapacidade(double peso) {
        if (peso < 0) {
            throw new IllegalArgumentException("Dessa vez não é! O peso não pode ser negativo.");
        }

        return (peso <= this.capacidadeMaxima);
    }

    /**
     * Matéria-prima que está na esteira agora
     *
     * @return quantidade carregada, 0 se não tiver nada
     */
    public double getMateriaPrima() {
        return materiaPrima;
    }

    /**
     * Produto que está na esteira agora
     *
     * @return produto carregado, ou null se não tiver nenhum
     */
    public Produto getProduto() {
        return produto;
    }

    /**
     * Nome da esteira
     *
     * @return nome
     */
    public String getNome() {
        return nome;
    }

    /**
     * Diz se a esteira está ligada
     *
     * @return true se estiver ligada
     */
    public boolean estaLigada() {
        return ligada;
    }
}
