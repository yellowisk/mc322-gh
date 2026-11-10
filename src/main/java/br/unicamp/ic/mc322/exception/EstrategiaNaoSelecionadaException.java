package br.unicamp.ic.mc322.exception;

/**
 * Lançada quando mandam fabricar pela estratégia mas nenhuma estratégia de produção foi escolhida
 *
 * @author Glayson Oliveira
 */
public class EstrategiaNaoSelecionadaException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     */
    public EstrategiaNaoSelecionadaException(String message) {
        super(message);
    }
}
