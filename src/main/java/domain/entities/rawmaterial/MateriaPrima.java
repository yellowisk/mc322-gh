package domain.entities.rawmaterial;

import domain.exceptions.InsufficientRawMaterialException;
import domain.exceptions.InvalidDemandException;

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
     * @throws IllegalArgumentException se o estoque inicial ou o mínimo forem negativos
     */
    public MateriaPrima(String nome, int quantidadeInicial, String unidade, int quantidadeMinima, float preco) {
        if (quantidadeInicial < 0 || quantidadeMinima < 0) {
            throw new IllegalArgumentException("Cadê a força? Quantidades iniciais e mínimas não podem ser negativas.");
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
     * @throws InvalidDemandException se a quantidade não for positiva
     * @throws InsufficientRawMaterialException se o consumo deixar o estoque abaixo do mínimo
     */
    public void consumir(double quantidadePedida) {
        if (quantidadePedida <= 0) {
            throw new InvalidDemandException("Cadê a força? A demanda para consumo deve ser maior que zero.");
        }
        if (!estaDisponivel(quantidadePedida)) {
            throw new InsufficientRawMaterialException("Cadê a força? Estoque insuficiente para atender à demanda de " + quantidadePedida + " " + unidade + ".");
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
