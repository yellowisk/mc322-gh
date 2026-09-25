package view;

import domain.entities.productionmanager.ProductionManager;
import domain.entities.rawmaterial.RawMaterial;

public class StorageMenu extends Submenu {

    public StorageMenu(Menu menu, ProductionManager productionManager) {
        super(menu, productionManager, ConsolePrinter.BLUE);
    }

    @Override
    public String icon() {
        return ConsolePrinter.color(color, "≡");
    }

    @Override
    public String label() {
        return "Ver armazém";
    }

    @Override
    public void show() {
        while (true) {
            printHeader();
            RawMaterial rm = productionManager.getRawMaterial();
            System.out.printf("   Estoque de Matéria-Prima: " + ConsolePrinter.color(color, "%.2f %s") + "\n\n",
                    rm.getQuantity(), rm.getUnit());
            ConsolePrinter.listStorage(productionManager.getDemands(), productionManager.getFabricatedProducts());
            ConsolePrinter.printBackOption();

            menu.printFooter();
            int option = menu.readInt("Escolha: ");

            if (option == 0) break;
            // No list to index here, so anything that ain't 0 is invalid
            menu.setLastBuffer(ConsolePrinter.failText("Dessa vez não é! Opção inválida!"));
        }
    }
}
