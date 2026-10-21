package br.unicamp.ic.mc322.model.machine;

import br.unicamp.ic.mc322.util.GeradorAleatorio;

/**
 * Resistência do forno que derrete o vidro. Pode queimar a cada aquecimento, e cada uma queimada
 * deixa o forno mais frio. Parte da composição de {@link MaquinaProcessamento} (1:N)
 *
 * @author Heitor Almeida
 */
class ResistenciaDoForno {
    /** Chance de queimar a cada aquecimento */
    private static final double CHANCE_QUEIMAR = 0.01;
    private final int id;
    private final double potencia;
    private boolean queimada;

    /**
     * Cria a resistência funcionando
     *
     * @param id número da resistência no forno
     * @param potencia potência da resistência
     */
    ResistenciaDoForno(int id, double potencia) {
        this.id = id;
        this.potencia = potencia;
    }

    /** Aquece o forno, c/ uma pequena chance de queimar. Uma resistência queimada não aquece mais */
    void aquecer() {
        if (!this.queimada && GeradorAleatorio.chance(CHANCE_QUEIMAR)) {
            this.queimada = true;
        }
    }

    /** Troca a resistência por uma nova */
    void trocar() {
        this.queimada = false;
    }

    /**
     * Diz se a resistência queimou
     *
     * @return true se estiver queimada
     */
    boolean estaQueimada() {
        return this.queimada;
    }

    /**
     * Potência que ela entrega ao forno
     *
     * @return potência, tem valor 0 se estiver queimada
     */
    double potenciaEntregue() {
        return this.queimada ? 0 : this.potencia;
    }

    /**
     * Potência nominal
     *
     * @return potência, em W
     */
    double getPotencia() {
        return this.potencia;
    }

    /**
     * Uma linha c/ o estado da resistência p/ a auditoria
     *
     * @return ex.: "Resistência 1 (1500 W): OK""
     */
    String diagnostico() {
        String estado = this.queimada ? "Queimada" : "OK";
        return String.format("Resistência %d (%.0f W): %s", this.id, this.potencia, estado);
    }
}
