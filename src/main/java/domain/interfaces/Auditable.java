package domain.interfaces;

public interface Auditable {
    String generateDiagnosticReport();
    boolean needsMaintenance();
}