package br.unicamp.ic.mc322.exception;

/**
 * Lançada quando o sistema operacional não deixa ler o arquivo de save (sem permissão, erro de disco, etc...)
 *
 * @author Heitor Almeida
 */
public class FalhaLeituraPersistenciaException extends PersistenciaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem e a exceção que causou o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     * @param cause exceção original
     */
    public FalhaLeituraPersistenciaException(String message, Throwable cause) {
        super(message, cause);
    }
}
