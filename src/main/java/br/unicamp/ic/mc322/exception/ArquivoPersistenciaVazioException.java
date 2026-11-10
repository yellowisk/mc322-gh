package br.unicamp.ic.mc322.exception;

/**
 * Lançada ao carregar quando o arquivo de save está vazio ou só tem espaços
 *
 * @author Heitor Almeida
 */
public class ArquivoPersistenciaVazioException extends PersistenciaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     */
    public ArquivoPersistenciaVazioException(String message) {
        super(message);
    }

    /**
     * Cria a exceção c/ a mensagem e a exceção que causou o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     * @param cause exceção original
     */
    public ArquivoPersistenciaVazioException(String message, Throwable cause) {
        super(message, cause);
    }
}
