package domain.exceptions;

/**
 * Lançada quando não tem matéria-prima suficiente p/ um consumo, ou seja, quando a retirada deixaria o estoque
 * abaixo do mínimo
 *
 * @author Glayson Oliveira
 */
public class InsufficientRawMaterialException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     */
    public InsufficientRawMaterialException(String message) {
        super(message);
    }
}
