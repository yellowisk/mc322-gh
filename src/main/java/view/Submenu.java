package view;

import domain.entities.productionmanager.ProductionManager;

public abstract class Submenu {
    protected final Menu menu;
    protected final ProductionManager productionManager;
    /** Submenu's main color: icon, actions, highlights and neutral messages */
    protected final String color;

    protected Submenu(Menu menu, ProductionManager productionManager, String color) {
        this.menu = menu;
        this.productionManager = productionManager;
        this.color = color;
    }

    /** Icon already painted w/ the submenu color */
    public abstract String icon();

    /** Name on the main menu, e.g. "Ver armazém" */
    public abstract String label();

    public abstract void show();

    /** Clears the screen and prints the header w/ the icon + label in caps */
    protected void printHeader() {
        ConsolePrinter.clearScreen();
        ConsolePrinter.card("%s", icon() + " " + label().toUpperCase());
        System.out.println();
    }
}
