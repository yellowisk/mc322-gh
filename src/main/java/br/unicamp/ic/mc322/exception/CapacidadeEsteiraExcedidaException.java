package br.unicamp.ic.mc322.exception;

/**
 * Lançada quando a matéria-prima ou o produto que vai pra esteira pesa mais do que ela aguenta carregar
 *
 * @author Heitor Almeida
 */
public class CapacidadeEsteiraExcedidaException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     */
    public CapacidadeEsteiraExcedidaException(String message) {
        super(message);
    }
}
