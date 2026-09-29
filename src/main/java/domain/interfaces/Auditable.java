package domain.interfaces;

public interface Auditable {
    String generateDiagnosticReport();

    /** Uma linha identificando o componente e o motivo do alerta, para a lista de problemas da auditoria */
    String generateIssueSummary();

    boolean needsMaintenance();
}