package domain.utils;

import java.util.Random;

/**
 * Fonte única de aleatoriedade do sistema. Todo sorteio (falha das máquinas, desgaste, tempo de produção)
 * passa por aqui, então c/ uma seed fixa um teste inteiro fica reproduzível
 *
 * @author Heitor Almeida
 */
public final class GeradorAleatorio {
    private static Random aleatorio = new Random();
    /* começa aleatório de verdade por padrão */
    private static Long seed = null;

    private GeradorAleatorio() {}

    /**
     * Fixa a seed, recriando o gerador. A partir daí os sorteios se repetem igual em toda execução
     *
     * @param novaSeed seed, passada em {@code ./gradlew run --args=42}
     */
    public static void setSeed(long novaSeed) {
        seed = novaSeed;
        aleatorio = new Random(novaSeed);
    }

    /**
     * Sorteia um evento c/ a probabilidade dada
     *
     * @param probabilidade chance do evento, entre 0 e 1
     * @return true se o evento aconteceu
     */
    public static boolean chance(double probabilidade) {
        return sortearDouble() < probabilidade;
    }

    /**
     * Sorteia um inteiro
     *
     * @param limite limite exclusivo, tem que ser maior que 0
     * @return inteiro entre 0 e o limite
     */
    public static int sortearInt(int limite) {
        return aleatorio.nextInt(limite);
    }

    /**
     * Sorteia um double
     *
     * @return double entre 0 e 1
     */
    public static double sortearDouble() {
        return aleatorio.nextDouble();
    }

    /**
     * Seed em uso
     *
     * @return seed fixada, ou null se a execução está aleatória de verdade
     */
    public static Long getSeed() {
        return seed;
    }
}
