package domain.entities.product;

/**
 * Em que ponto da linha de produção um {@link Produto} está. A ordem na linha é
 * UNDERGOING, PROCESSED, PACKED e depois da inspeção é APPROVED, TO_REPAIR ou FAILED
 *
 * @author Heitor Almeida
 */
public enum ProductStatus {
    /** Status inicial, de todo produto recém-criado (inclusive os modelos do catálogo) */
    UNDERGOING,
    /** Saiu da máquina de processamento */
    PROCESSED,
    /** Saiu da máquina de empacotamento */
    PACKED,
    /** Passou na inspeção e foi pro armazém */
    APPROVED,
    /** Passou na inspeção c/ ressalva: tá no armazém, mas precisa de manutenção */
    TO_REPAIR,
    /** Rejeitado na inspeção e descartado */
    FAILED
}
