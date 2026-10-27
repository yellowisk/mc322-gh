package br.unicamp.ic.mc322.exception;

/**
 * Lançada ao carregar quando o arquivo de save não existe
 *
 * @author Heitor Almeida
 */
public class ArquivoPersistenciaInexistenteException extends PersistenciaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     */
    public ArquivoPersistenciaInexistenteException(String message) {
        super(message);
    }

    /**
     * Cria a exceção c/ a mensagem e a exceção que causou o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     * @param cause exceção original
     */
    public ArquivoPersistenciaInexistenteException(String message, Throwable cause) {
        super(message, cause);
    }
}
