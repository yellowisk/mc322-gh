package domain.entities.product;

/**
 * Em que ponto da linha de produção um {@link Produto} está. A ordem na linha é
 * EM_PRODUCAO, PROCESSADO, EMPACOTADO e depois da inspeção é APROVADO, PRECISA_MANUTENCAO ou REJEITADO
 *
 * @author Heitor Almeida
 */
public enum StatusProduto {
    /** Status inicial, de todo produto recém-criado (inclusive os modelos do catálogo) */
    EM_PRODUCAO,
    /** Saiu da máquina de processamento */
    PROCESSADO,
    /** Saiu da máquina de empacotamento */
    EMPACOTADO,
    /** Passou na inspeção e foi pro armazém */
    APROVADO,
    /** Passou na inspeção c/ ressalva: tá no armazém, mas precisa de manutenção */
    PRECISA_MANUTENCAO,
    /** Rejeitado na inspeção e descartado */
    REJEITADO
}
