package domain.interfaces;

public interface Auditable {
    String gerarRelatorioDiagnostico();

    /** Uma linha identificando o componente e o motivo do alerta, para a lista de problemas da auditoria */
    String gerarResumoProblema();

    boolean precisaManutencao();
}