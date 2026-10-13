package domain.entities.machine;

import domain.entities.product.Product;
import domain.entities.product.ProductStatus;

/**
 * Segunda etapa da linha: empacota o produto que saiu do processamento
 *
 * @author Heitor Almeida
 */
public class PackingMachine extends Machine {

    /**
     * Cria a máquina de empacotamento
     *
     * @param nome nome que aparece nos menus
     * @param capacidadeMaxima unidades por ciclo
     * @param chanceFalhaBase chance base de falha, entre 0 e 1
     * @param custoOperacao custo p/ processar uma unidade, em R$
     * @param multiplicadorCenario multiplicador de falha do cenário
     * @param desgasteMaximo desgaste máximo por uso
     */
    public PackingMachine(String nome, int capacidadeMaxima, double chanceFalhaBase,
                             double custoOperacao, double multiplicadorCenario,
                             int desgasteMaximo) {
        super(nome, capacidadeMaxima, chanceFalhaBase, custoOperacao, multiplicadorCenario, desgasteMaximo);
    }

    /**
     * Marca o produto como {@link ProductStatus#PACKED}. Se a máquina falhar no sorteio,
     * o produto sai c/ mais risco acumulado
     *
     * @param produto produto vindo do processamento
     * @return o mesmo produto, empacotado
     */
    @Override
    public Product processarEtapa(Product produto) {
        tentaAumentarRisco(produto);

        produto.setStatus(ProductStatus.PACKED);
        return produto;
    }

    @Override
    public String getTipo() {
        return "Empacotamento";
    }
}
