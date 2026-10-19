package domain.entities.machine;

import domain.utils.GeradorAleatorio;

/**
 * Sensor que monitora o processo da máquina. Perde calibração c/ o uso, e um sensor descalibrado
 * deixa defeitos passarem, o que aumenta a chance de falha da máquina. Parte da composição de
 * {@link Maquina} (1:N)
 *
 * @author Heitor Almeida
 */
class Sensor {
    /** Chance de perder calibração a cada ciclo */
    private static final double CHANCE_DESCALIBRAR = 0.08;
    /** Perda máxima de calibração num ciclo */
    private static final double PERDA_MAXIMA = 0.15;
    /** Abaixo disso o sensor está descalibrado */
    private static final double CALIBRACAO_MINIMA = 0.7;
    private final int id;
    private double calibracao = 1.0;

    /**
     * Cria o sensor calibrado
     *
     * @param id número do sensor na máquina
     */
    Sensor(int id) {
        this.id = id;
    }

    /** Registra um ciclo de uso, c/ chance de perder um pouco da calibração */
    void registrarCiclo() {
        if (GeradorAleatorio.chance(CHANCE_DESCALIBRAR)) {
            this.calibracao = Math.max(0, this.calibracao - GeradorAleatorio.sortearDouble() * PERDA_MAXIMA);
        }
    }

    /** Volta a calibração pro máximo */
    void recalibrar() {
        this.calibracao = 1.0;
    }

    /**
     * Diz se o sensor ainda está calibrado o suficiente
     *
     * @return true se a calibração está acima do mínimo
     */
    boolean estaCalibrado() {
        return this.calibracao > CALIBRACAO_MINIMA;
    }

    /**
     * Diz se o sensor está c/ a calibração perfeita
     *
     * @return true se nunca perdeu calibração desde o último reparo
     */
    boolean estaNovo() {
        return this.calibracao >= 1.0;
    }

    /**
     * Uma linha c/ o estado do sensor p/ a auditoria
     *
     * @return ex.: "Sensor 1: OK (95%)""
     */
    String diagnostico() {
        String estado = estaCalibrado() ? "OK" : "Descalibrado";
        return String.format("Sensor %d: %s (%.0f%%)", this.id, estado, this.calibracao * 100);
    }
}
