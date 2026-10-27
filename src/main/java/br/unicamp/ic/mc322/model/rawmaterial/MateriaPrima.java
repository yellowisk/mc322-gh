package br.unicamp.ic.mc322.model.rawmaterial;

import br.unicamp.ic.mc322.exception.MateriaPrimaInsuficienteException;
import br.unicamp.ic.mc322.exception.DemandaInvalidaException;

import java.util.UUID;

/**
 * Matéria-prima da fábrica. É comprada no menu e gasta uma unidade de cada vez na etapa de processamento
 *
 * @author Glayson Oliveira
 * @author Heitor Almeida
 */
public class MateriaPrima {
    // Mandatory attributes
    private final UUID id;
    private final String nome;
    private final String unidade;
    private final int quantidadeMinima;
    private double quantidade;
    private final float preco;

    // My methods
    /**
     * Cria a matéria-prima c/ o estoque inicial
     *
     * @param nome nome, ex.: "Vidro"
     * @param quantidadeInicial estoque inicial
     * @param unidade unidade de medida, ex.: "kg"
     * @param quantidadeMinima estoque mínimo, que não pode ser consumido
     * @param preco preço por unidade, em R$
     * @throws IllegalArgumentException se o estoque inicial ou o mínimo forem negativos, se o preço não for
     *         positivo ou se o nome ou a unidade estiverem em branco
     */
    public MateriaPrima(String nome, double quantidadeInicial, String unidade, int quantidadeMinima, float preco) {
        if (!(quantidadeInicial >= 0) || quantidadeMinima < 0) {
            throw new IllegalArgumentException("Cadê a força? Quantidades iniciais e mínimas não podem ser negativas.");
        }
        if (!(preco > 0)) {
            throw new IllegalArgumentException("Cadê a força? O preço da matéria-prima tem que ser positivo.");
        }
        if (nome == null || nome.isBlank() || unidade == null || unidade.isBlank()) {
            throw new IllegalArgumentException("Cadê a força? A matéria-prima precisa de nome e unidade.");
        }

        this.id = UUID.randomUUID();
        this.nome = nome;
        this.unidade = unidade;
        this.quantidadeMinima = quantidadeMinima;
        this.quantidade = quantidadeInicial;
        this.preco = preco;
    }

    // Mandatory methods
    /**
     * Tira a quantidade do estoque
     *
     * @param quantidadePedida quantidade a consumir, maior que 0
     * @throws DemandaInvalidaException se a quantidade não for positiva
     * @throws MateriaPrimaInsuficienteException se o consumo deixar o estoque abaixo do mínimo
     */
    public void consumir(double quantidadePedida) {
        if (quantidadePedida <= 0) {
            throw new DemandaInvalidaException("Cadê a força? A demanda para consumo deve ser maior que zero.");
        }
        if (!estaDisponivel(quantidadePedida)) {
            throw new MateriaPrimaInsuficienteException("Cadê a força? Estoque insuficiente para atender à \ndemanda de " + quantidadePedida + " " + unidade + ".");
        }
        this.quantidade -= quantidadePedida;
    }

    /**
     * Adiciona a quantidade especificada no estoque
     * @param quantidade quantidade a adicionar, maior que 0
     * @throws IllegalArgumentException se a quantidade não for positiva
     */
    public void adicionarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Cadê a força? A quantidade para adicionar ao estoque deve ser maior que zero.");
        }
        this.quantidade += quantidade;
    }

    /**
     * Verifica se dá p/ consumir a quantidade sem deixar o estoque abaixo do mínimo
     * @param quantidadePedida quantidade que se quer consumir
     * @return true se a quantidade for positiva e o estoque continuar acima do mínimo depois do consumo
     */
    public boolean estaDisponivel(double quantidadePedida) {
        return quantidadePedida > 0 && this.quantidade >= quantidadePedida && (this.quantidade - quantidadePedida) >= quantidadeMinima;
    }

    /**
     * Identificador único da matéria-prima
     *
     * @return id gerado na criação
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Nome da matéria-prima
     *
     * @return nome, ex.: "Vidro"
     */
    public String getNome() {
        return this.nome;
    }

    /**
     * Unidade de medida
     *
     * @return unidade, ex.: "kg"
     */
    public String getUnidade() {
        return this.unidade;
    }

    /**
     * Estoque mínimo, que nunca pode ser consumido
     *
     * @return quantidade mínima
     */
    public int getQuantidadeMinima() {
        return this.quantidadeMinima;
    }

    /**
     * Preço de uma unidade
     *
     * @return preço, em R$
     */
    public float getPreco() {
        return this.preco;
    }

    /**
     * Estoque atual, contando o mínimo
     *
     * @return quantidade em estoque
     */
    public double getQuantidade() {
        return this.quantidade;
    }

    /**
     * Sobrescreve o estoque direto, sem validação
     *
     * @param quantidade novo estoque
     */
    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

}
