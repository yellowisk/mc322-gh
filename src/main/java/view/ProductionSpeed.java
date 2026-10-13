package view;

/**
 * Velocidade da animação da produção, escolhida no menu e mantida pela sessão toda
 *
 * @author Heitor Almeida
 */
public enum ProductionSpeed {
    /** Pausas normais */
    NORMAL("Normal", 1.0),
    /** Pausas c/ 30% da duração normal */
    RAPIDA("Rápida", 0.3),
    /** Sem pausa nenhuma */
    INSTANTANEA("Instantânea", 0.0);

    private final String descricao;
    /* multiplica as pausas da produção, 0 = sem pausa */
    private final double fatorPausa;

    /**
     * Associa a velocidade ao nome no menu e ao fator das pausas
     *
     * @param descricao nome que aparece no menu
     * @param fatorPausa quanto multiplica as pausas da produção
     */
    ProductionSpeed(String descricao, double fatorPausa) {
        this.descricao = descricao;
        this.fatorPausa = fatorPausa;
    }

    /**
     * Nome da velocidade
     *
     * @return nome que aparece no menu, ex.: "Rápida"
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Quanto as pausas da produção são multiplicadas, ver {@link ConsolePrinter#pausarProducao(int)}
     *
     * @return fator, 0 = sem pausa
     */
    public double getFatorPausa() {
        return fatorPausa;
    }
}
