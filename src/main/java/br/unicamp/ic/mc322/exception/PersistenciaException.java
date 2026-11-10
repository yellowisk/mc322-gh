package br.unicamp.ic.mc322.exception;

/**
 * Base das falhas ao salvar ou carregar o estado da fábrica
 *
 * @author Heitor Almeida
 */
public abstract class PersistenciaException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     */
    protected PersistenciaException(String message) {
        super(message);
    }

    /**
     * Cria a exceção c/ a mensagem e a exceção que causou o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     * @param cause exceção original
     */
    protected PersistenciaException(String message, Throwable cause) {
        super(message, cause);
    }
}
