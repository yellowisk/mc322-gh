package br.unicamp.ic.mc322.exception;

/**
 * Lançada quando tentam atualizar uma demanda c/ uma quantidade inválida, ex.: negativa
 *
 * @author Heitor Almeida
 */
public class QuantidadeDemandaInvalidaException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message a mensagem que vai aparecer no menu
     */
    public QuantidadeDemandaInvalidaException(String message) {
        super(message);
    }
}
