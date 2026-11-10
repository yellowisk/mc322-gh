package br.unicamp.ic.mc322.exception;

/**
 * Lançada ao carregar quando o XML está bem formado, mas os dados quebram uma regra da fábrica,<br>
 * ex.: saúde do motor acima do máximo, demanda concluída c/ unidades faltando ou cenário que não existe
 *
 * @author Heitor Almeida
 */
public class DadosPersistidosInvalidosException extends PersistenciaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     */
    public DadosPersistidosInvalidosException(String message) {
        super(message);
    }

    /**
     * Cria a exceção c/ a mensagem e a exceção que causou o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     * @param cause exceção original
     */
    public DadosPersistidosInvalidosException(String message, Throwable cause) {
        super(message, cause);
    }
}
