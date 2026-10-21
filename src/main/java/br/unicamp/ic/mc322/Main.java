package br.unicamp.ic.mc322;

import br.unicamp.ic.mc322.util.GeradorAleatorio;
import br.unicamp.ic.mc322.view.Menu;

/**
 * Ponto de entrada do simulador do Mercadinho
 *
 * @author Heitor Almeida
 */
public class Main {
    /** Só tem o main, não é p/ instanciar */
    private Main() {
    }

    /**
     * Inicia o menu principal. Se vier uma seed, o aleatório fica reproduzível
     *
     * @param args opcional, {@code args[0]} é a seed do {@link GeradorAleatorio}.
     *             <br>Ex.: {@code ./gradlew run --args=42}
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                GeradorAleatorio.setSeed(Long.parseLong(args[0]));
            } catch (NumberFormatException e) {
                System.out.println("Seed inválida (\"" + args[0] + "\"): tem que ser um número inteiro. Seguindo sem seed.");
            }
        }
        new Menu().iniciar();
    }
}
