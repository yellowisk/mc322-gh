import domain.utils.RandomProvider;
import view.Menu;

public class Main {

    public static void main(String[] args) {
        if (args.length > 0)
            RandomProvider.setSeed(Long.parseLong(args[0]));
        new Menu().start();
    }
}
