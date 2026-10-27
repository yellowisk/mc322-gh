package br.unicamp.ic.mc322.model.machine;

import br.unicamp.ic.mc322.util.GeradorAleatorio;

/**
 * Motor da máquina. Guarda a saúde e se desgasta a cada uso. Abaixo do limiar crítico ele quebra
 * e a máquina para até ser reparada. Parte da composição de {@link Maquina} (1:1)
 *
 * @author Heitor Almeida
 */
class Motor {
    /** Saúde de um motor novo ou recém-reparado */
    private static final int SAUDE_MAXIMA = 100;
    /** Abaixo disso o motor está quebrado */
    private static final int SAUDE_CRITICA = 15;
    /** Abaixo disso o motor está perto de quebrar */
    private static final int SAUDE_ALERTA = 40;
    private final int desgasteMaximo;
    private int saude = SAUDE_MAXIMA;

    /**
     * Cria o motor c/ saúde máxima
     *
     * @param desgasteMaximo desgaste máximo por uso, cada uso tira de 1 a esse valor da saúde
     * @throws IllegalArgumentException se o desgaste máximo não for positivo
     */
    Motor(int desgasteMaximo) {
        if (desgasteMaximo < 1) {
            throw new IllegalArgumentException("Dessa vez não é! O desgaste máximo do motor tem que ser positivo.");
        }
        this.desgasteMaximo = desgasteMaximo;
    }

    /** Tira da saúde um valor sorteado em [1, desgasteMaximo] */
    void desgastar() {
        this.saude = Math.max(0, this.saude - (GeradorAleatorio.sortearInt(this.desgasteMaximo) + 1));
    }

    /** Volta a saúde pro máximo */
    void reparar() {
        this.saude = SAUDE_MAXIMA;
    }

    /**
     * Volta o motor p/ uma saúde salva antes
     *
     * @param saude saúde salva
     * @throws IllegalArgumentException se a saúde estiver fora de 0 até {@value #SAUDE_MAXIMA}
     */
    void restaurar(int saude) {
        if (saude < 0 || saude > SAUDE_MAXIMA) {
            throw new IllegalArgumentException("Dessa vez não é! A saúde do motor tem que estar entre 0 e " + SAUDE_MAXIMA
                    + ", mas veio " + saude + ".");
        }
        this.saude = saude;
    }

    /**
     * Diz se a saúde está abaixo do limiar crítico
     *
     * @return true se o motor quebrou
     */
    boolean estaQuebrado() {
        return this.saude < SAUDE_CRITICA;
    }

    /**
     * Diz se o motor está c/ a saúde cheia, sem nenhum desgaste
     *
     * @return true se a saúde for a máxima
     */
    boolean estaNovo() {
        return this.saude == SAUDE_MAXIMA;
    }

    /**
     * Quanto o desgaste piora a chance de falha da máquina
     *
     * @return 1.0 com saúde cheia, perto de 2.0 quando está quase quebrando
     */
    double fatorDesgaste() {
        return 1 + (double) (SAUDE_MAXIMA - this.saude) / SAUDE_MAXIMA;
    }

    /**
     * Saúde atual
     *
     * @return saúde, de 0 até {@link #getSaudeMaxima()}
     */
    int getSaude() {
        return this.saude;
    }

    /**
     * Saúde de um motor novo ou recém-reparado
     *
     * @return saúde máxima
     */
    int getSaudeMaxima() {
        return SAUDE_MAXIMA;
    }

    /**
     * Estado do motor pela faixa de saúde: novo (cheio), desgastado, crítico (abaixo de
     * {@value #SAUDE_ALERTA}) ou quebrado (abaixo de {@value #SAUDE_CRITICA})
     *
     * @return estado p/ o diagnóstico
     */
    private String estado() {
        if (estaQuebrado()) {
            return "QUEBRADO";
        }
        if (this.saude < SAUDE_ALERTA) {
            return "Crítico";
        }
        if (this.saude < SAUDE_MAXIMA) {
            return "Desgastado";
        }
        return "Novo";
    }

    /**
     * Uma linha c/ o estado do motor para a auditoria, primeiro o estado e depois a saúde
     *
     * @return ex.: "Motor: Desgastado (72/100)"
     */
    String diagnostico() {
        return String.format("Motor: %s (%d/%d)", estado(), this.saude, SAUDE_MAXIMA);
    }
}
