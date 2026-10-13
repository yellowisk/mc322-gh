package domain.entities.machine;

import domain.entities.product.Product;
import domain.entities.product.ProductStatus;

/**
 * Primeira etapa da linha: transforma a matéria-prima em um produto novo
 *
 * @author Heitor Almeida
 */
public class ProcessingMachine extends Machine {

    /**
     * Cria a máquina de processamento
     *
     * @param nome nome que aparece nos menus
     * @param capacidadeMaxima unidades por ciclo
     * @param chanceFalhaBase chance base de falha, entre 0 e 1
     * @param custoOperacao custo p/ processar uma unidade, em R$
     * @param multiplicadorCenario multiplicador de falha do cenário
     * @param desgasteMaximo desgaste máximo por uso
     */
    public ProcessingMachine(String nome, int capacidadeMaxima, double chanceFalhaBase,
                             double custoOperacao, double multiplicadorCenario,
                             int desgasteMaximo) {
        super(nome, capacidadeMaxima, chanceFalhaBase, custoOperacao, multiplicadorCenario, desgasteMaximo);
    }

    /**
     * Cria uma unidade nova a partir do modelo do catálogo, já como {@link ProductStatus#PROCESSED}.
     * Se a máquina falhar no sorteio, a unidade sai c/ mais risco acumulado
     *
     * @param produto modelo do catálogo
     * @return unidade nova, c/ id próprio
     */
    @Override
    public Product processarEtapa(Product produto) {
        Product novoProduto = produto.processar(produto, ProductStatus.PROCESSED);
        tentaAumentarRisco(novoProduto);
        return novoProduto;
    }

    @Override
    public String getTipo() {
        return "Processamento";
    }
}
