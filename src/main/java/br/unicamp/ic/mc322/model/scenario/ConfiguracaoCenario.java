package br.unicamp.ic.mc322.model.scenario;

import java.util.List;
import java.util.Optional;

/**
 * Classe para armazenar as informações de um cenário. O cenário escolhido no começo define a verba
 * inicial, o multiplicador de falha das máquinas e o desgaste máximo por uso
 *
 * @author Glayson Oliveira
 */
public class ConfiguracaoCenario {
    private final String nome;
    private final String tipo;
    private final float verba;
    private final double multiplicadorCenario;
    private final int desgasteMaximo;

    /** Cenários disponíveis, na ordem do seletor de cenário */
    public static final List<ConfiguracaoCenario> CENARIOS = List.of(
            new ConfiguracaoCenario("Mercadinho Ideal", "Ideal", 1000, 0.5, 1),
            new ConfiguracaoCenario("Mercadinho Razoável", "Intermediário", 500, 1, 2),
            new ConfiguracaoCenario("Mercadinho Precário", "Apocalíptico", 250, 1.5, 4)
    );

    /**
     * Cria o cenário
     *
     * @param nome nome temático, ex.: "Mercadinho Precário"
     * @param tipo rótulo do enunciado, ex.: "Apocalíptico"
     * @param verba verba inicial, em R$
     * @param multiplicadorCenario multiplicador de falha das máquinas, ex.: 1.5
     * @param desgasteMaximo desgaste máximo por uso das máquinas
     */
    public ConfiguracaoCenario(String nome, String tipo, float verba, double multiplicadorCenario, int desgasteMaximo) {
        this.nome = nome;
        this.tipo = tipo;
        this.verba = verba;
        this.multiplicadorCenario = multiplicadorCenario;
        this.desgasteMaximo = desgasteMaximo;
    }

    /**
     * Verba inicial do cenário
     *
     * @return verba, em R$
     */
    public float getVerba() {
        return verba;
    }

    /**
     * Multiplicador de falha das máquinas
     *
     * @return multiplicador, ex.: 0.5 no Ideal
     */
    public double getMultiplicadorCenario() {
        return multiplicadorCenario;
    }

    /**
     * Desgaste máximo por uso das máquinas
     *
     * @return desgaste máximo
     */
    public int getDesgasteMaximo() {
        return desgasteMaximo;
    }

    /**
     * Nome temático do cenário
     *
     * @return nome, ex.: "Mercadinho Ideal"
     */
    public String getNome() {
        return this.nome;
    }

    /**
     * Rótulo do enunciado
     *
     * @return tipo, ex.: "Intermediário"
     */
    public String getTipo() {
        return this.tipo;
    }

    /**
     * Nome temático + rótulo do enunciado
     *
     * @return nome de exibição, ex.: "Mercadinho Precário (Apocalíptico)"
     */
    public String getNomeExibicao() {
        return String.format("%s (%s)", this.nome, this.tipo);
    }

    /**
     * Procura um dos {@link #CENARIOS} pelo tipo, ex.: p/ reconstruir o cenário salvo num arquivo
     *
     * @param tipo Tipo do cenário, ex.: "Intermediário"
     * @return o cenário, ou vazio se nenhum tiver esse tipo
     */
    public static Optional<ConfiguracaoCenario> buscarPorTipo(String tipo) {
        return CENARIOS.stream()
                .filter(c -> c.tipo.equalsIgnoreCase(tipo))
                .findFirst();
    }
}
