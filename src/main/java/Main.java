import domain.utils.GeradorAleatorio;
import view.Menu;

/**
 * Ponto de entrada do simulador do Mercadinho
 *
 * @author Heitor Almeida
 */
public class Main {

    /**
     * Inicia o menu principal. Se vier uma seed, o aleatório fica reproduzível
     *
     * @param args opcional, {@code args[0]} é a seed do {@link GeradorAleatorio}.
     *             <br>Ex.: {@code ./gradlew run --args=42}
     */
    public static void main(String[] args) {
        if (args.length > 0)
            GeradorAleatorio.setSeed(Long.parseLong(args[0]));
        new Menu().iniciar();
    }
}
