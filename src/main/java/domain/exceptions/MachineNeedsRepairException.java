package domain.exceptions;

/**
 * Lançada quando uma máquina está quebrada (saúde abaixo do limiar crítico): ao tentar processar um produto
 * nela ou ao iniciar uma fabricação enquanto alguma máquina precisa de reparo
 *
 * @author Glayson Oliveira
 */
public class MachineNeedsRepairException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message a mensagem que vai aparecer pro usuário
     */
    public MachineNeedsRepairException(String message) {
        super(message);
    }
}
