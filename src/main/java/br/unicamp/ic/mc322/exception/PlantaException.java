package br.unicamp.ic.mc322.exception;

/**
 * Base de todas as exceções de domínio da fábrica, ou seja, das situações que quebram uma regra de negócio
 * (falta de orçamento, de matéria-prima, demanda inválida, máquina indisponível...).
 *
 * @author Heitor Almeida
 */
public abstract class PlantaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     */
    protected PlantaException(String message) {
        super(message);
    }
}
