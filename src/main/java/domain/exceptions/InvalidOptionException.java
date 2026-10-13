package domain.exceptions;

/**
 * Lançada quando o usuário escolhe uma opção que não existe num menu, ex.: o número 7 numa lista de 3
 *
 * @author Glayson Oliveira
 */
public class InvalidOptionException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     */
    public InvalidOptionException(String message) {
        super(message);
    }
}
