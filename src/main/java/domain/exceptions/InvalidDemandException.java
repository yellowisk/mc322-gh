package domain.exceptions;

/**
 * Lançada quando uma demanda não pode ser usada como foi pedida, ex.: o produto dela não existe no catálogo,
 * ela não tem nenhuma unidade pedida ou a quantidade de matéria-prima a consumir não é positiva
 *
 * @author Glayson Oliveira
 */
public class InvalidDemandException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message o motivo da demanda ser inválida
     */
    public InvalidDemandException(String message) {
        super(message);
    }
}
