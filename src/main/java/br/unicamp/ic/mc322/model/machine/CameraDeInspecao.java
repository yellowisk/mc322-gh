package br.unicamp.ic.mc322.model.machine;

import br.unicamp.ic.mc322.util.GeradorAleatorio;

/**
 * Câmera que examina o produto na inspeção. A lente pode sujar a cada captura, dificultando
 * o achado de defeitos. Parte da composição de {@link MaquinaInspecao} (1:N)
 *
 * @author Heitor Almeida
 */
class CameraDeInspecao {
    /** Chance de a lente sujar a cada captura */
    private static final double CHANCE_SUJAR = 0.012;
    private final int id;
    private boolean lenteSuja;

    /**
     * Cria a câmera c/ a lente limpa
     *
     * @param id número da câmera na máquina, começa em 1
     */
    CameraDeInspecao(int id) {
        this.id = id;
    }

    /** Captura a imagem do produto com chance de sujar a lente */
    void capturar() {
        if (!this.lenteSuja && GeradorAleatorio.chance(CHANCE_SUJAR)) {
            this.lenteSuja = true;
        }
    }

    /** Limpa a lente */
    void limpar() {
        this.lenteSuja = false;
    }

    /**
     * Volta a câmera p/ um estado salvo antes
     *
     * @param lenteSuja se a lente estava suja
     */
    void restaurar(boolean lenteSuja) {
        this.lenteSuja = lenteSuja;
    }

    /**
     * Diz se a câmera está enxergando
     *
     * @return true se a lente estiver limpa
     */
    boolean estaFuncionando() {
        return !this.lenteSuja;
    }

    /**
     * Uma linha c/ o estado da câmera p/ a auditoria
     *
     * @return ex.: "Câmera 1: OK" "
     */
    String diagnostico() {
        String estado = this.lenteSuja ? "Lente suja" : "OK";
        return String.format("Câmera %d: %s", this.id, estado);
    }
}
