package br.unicamp.ic.mc322.exception;

/**
 * Lançada quando não dá p/ gravar o arquivo de save (sem permissão, disco cheio,
 * caminho é uma pasta, etc...).<br>
 * O save anterior continua intacto
 *
 * @author Heitor Almeida
 */
public class FalhaEscritaPersistenciaException extends PersistenciaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     */
    public FalhaEscritaPersistenciaException(String message) {
        super(message);
    }

    /**
     * Cria a exceção c/ a mensagem e a exceção que causou o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     * @param cause exceção original
     */
    public FalhaEscritaPersistenciaException(String message, Throwable cause) {
        super(message, cause);
    }
}
