package domain.exceptions;

/**
 * Lançada quando a estratégia atual não encontra nenhuma demanda elegível p/ fabricar
 *
 * @author Glayson Oliveira
 */
public class NenhumaDemandaElegivelException extends PlantaException {
    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção c/ a mensagem que explica o problema
     *
     * @param message a mensagem que vai aparecer pro usuário, c/ o nome da estratégia
     */
    public NenhumaDemandaElegivelException(String message) {
        super(message);
    }
}
