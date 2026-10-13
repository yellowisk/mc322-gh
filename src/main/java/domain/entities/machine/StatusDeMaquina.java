package domain.entities.machine;

/**
 * Estados possíveis de uma máquina
 *
 * @author Glayson Oliveira
 */
public enum StatusDeMaquina {
    /** Saúde acima do limiar crítico, pode processar */
    FUNCIONAL,
    /** Saúde abaixo do limiar crítico, precisa de reparo */
    QUEBRADA;
}
