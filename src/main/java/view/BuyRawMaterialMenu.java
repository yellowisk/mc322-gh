package view;

import domain.entities.productionmanager.ProductionManager;
import domain.entities.rawmaterial.RawMaterial;
import domain.exceptions.InsufficientBudgetException;

public class BuyRawMaterialMenu extends Submenu {

    public BuyRawMaterialMenu(Menu menu, ProductionManager productionManager) {
        super(menu, productionManager, ConsolePrinter.GREEN);
    }

    @Override
    public String icon() {
        return ConsolePrinter.color(color, "$");
    }

    @Override
    public String label() {
        return "Comprar matéria-prima";
    }

    @Override
    public void show() {
        while (true) {
            printHeader();
            RawMaterial rm = productionManager.getRawMaterial();
            System.out.printf("   Item: " + ConsolePrinter.color(color, "%s") + "\n", rm.getName());
            System.out.printf("   Estoque atual: " + ConsolePrinter.color(color, "%.2f %s") + "\n", rm.getQuantity(), rm.getUnit());
            System.out.printf("   Custo unitário: " + ConsolePrinter.color(color, "R$ %.2f / %s") + "\n\n", rm.getPrice(), rm.getUnit());
            ConsolePrinter.printBackOption();

            menu.printFooter();
            int amount = menu.readInt("Quantos " + rm.getUnit() + " deseja " + ConsolePrinter.color(color, "COMPRAR") + "? ");

            if (amount == 0) break;

            if (amount < 0) {
                menu.setLastBuffer(ConsolePrinter.failText("Dessa vez não é! Quantidade inválida!"));
                continue;
            }

            float totalCost = amount * rm.getPrice();
            if (totalCost > productionManager.getBudget()) {
                menu.setLastBuffer(ConsolePrinter.failText("Dessa vez não é! Orçamento insuficiente: a compra custa R$ %.2f e o orçamento é R$ %.2f.",
                        totalCost, productionManager.getBudget()));
                continue;
            }

            System.out.printf("\nVerba projetada: R$ %.2f " + ConsolePrinter.color(ConsolePrinter.RED, "(▾ R$ -%.2f)") + "\n",
                    productionManager.getBudget() - totalCost, totalCost);
            int confirm = menu.readInt("Confirmar compra? [" + ConsolePrinter.color(ConsolePrinter.GREEN, "1") + "] Sim / ["
                    + ConsolePrinter.color(ConsolePrinter.RED, "0") + "] Não: ");

            if (confirm != 1) {
                menu.setLastBuffer(ConsolePrinter.color(color, "Compra cancelada."));
                continue;
            }

            try {
                float paid = productionManager.buyRawMaterial(amount);
                if (paid >= 150) {
                    menu.setLastBuffer(ConsolePrinter.okText("Dessa vez não é! Cliente comprou 150 reais e, sim, cliente ganhou um balão de presente!"));
                } else {
                    menu.setLastBuffer(ConsolePrinter.okText("Eitcha!!! Compra de matéria-prima realizada com sucesso!"));
                }
                break;
            } catch (InsufficientBudgetException e) {
                menu.setLastBuffer(ConsolePrinter.failText("%s", e.getMessage()));
            }
        }
    }
}
