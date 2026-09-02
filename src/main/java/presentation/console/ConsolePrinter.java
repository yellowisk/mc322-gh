package presentation.console;

public final class ConsolePrinter {
    public static final String RESET = "\033[0m";
    public static final String RED = "\033[31m";
    public static final String GREEN = "\033[32m";
    public static final String BLUE = "\033[34m";

    /** Prints a card (menus, headers, reports, etc...) in blue */
    public static void card(String format, Object... args) {
        System.out.printf(BLUE + format + RESET, args);
    }

    /** Prints a success message in green */
    public static void ok(String format, Object... args) {
        System.out.printf(GREEN + format + RESET, args);
    }

    /** Prints an error message in red */
    public static void fail(String format, Object... args) {
        System.out.printf(RED + format + RESET, args);
    }
}
