package domain.entities.machine;

import domain.utils.GeradorAleatorio;

/**
 * Seladora da embalagem. A lâmina se gasta a cada selagem, e quanto mais gasta, maior a chance de a
 * embalagem sair mal selada. Se gastar totalmente, ela para. Parte da composição de {@link MaquinaEmpacotamento} (1:1)
 *
 * @author Heitor Almeida
 */
class Seladora {
    /** Desgaste máximo da lâmina numa selagem */
    private static final double DESGASTE_MAXIMO_POR_SELAGEM = 0.015;
    /** Chance de selagem ruim c/ a lâmina totalmente gasta */
    private static final double CHANCE_SELAGEM_RUIM_MAXIMA = 0.3;
    /** De 0 (nova) a 1 (gasta) */
    private double desgasteLamina;

    /** Cria a seladora c/ a lâmina nova */
    Seladora() {
    }

    /** Sela uma embalagem, gastando um pouco da lâmina */
    void selar() {
        this.desgasteLamina = Math.min(1.0, this.desgasteLamina + GeradorAleatorio.sortearDouble() * DESGASTE_MAXIMO_POR_SELAGEM);
    }

    /**
     * Diz se a lâmina ainda não foi usada
     *
     * @return true se a lâmina não tiver desgaste nenhum
     */
    boolean estaNova() {
        return this.desgasteLamina == 0;
    }

    /** Troca a lâmina por uma nova */
    void trocarLamina() {
        this.desgasteLamina = 0;
    }

    /**
     * Diz se a lâmina gastou de vez
     *
     * @return true se a seladora não consegue mais selar
     */
    boolean estaGasta() {
        return this.desgasteLamina >= 1.0;
    }

    /**
     * Chance de a embalagem sair mal selada, que cresce c/ o desgaste da lâmina
     *
     * @return chance, entre 0 e {@value #CHANCE_SELAGEM_RUIM_MAXIMA}
     */
    double chanceSelagemRuim() {
        return this.desgasteLamina * CHANCE_SELAGEM_RUIM_MAXIMA;
    }

    /**
     * Uma linha c/ o estado da seladora p/ a auditoria
     *
     * @return ex.: "Seladora: lâmina 30% gasta""
     */
    String diagnostico() {
        if (estaGasta()) {
            return "Seladora: lâmina totalmente gasta";
        }
        return String.format("Seladora: lâmina %.0f%% gasta", this.desgasteLamina * 100);
    }
}
