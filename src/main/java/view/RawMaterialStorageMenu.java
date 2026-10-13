package view;

import domain.entities.product.Product;
import domain.entities.productionmanager.ProductionManager;
import domain.entities.rawmaterial.RawMaterial;

import java.util.ArrayList;
import java.util.List;

public class RawMaterialStorageMenu extends Submenu {

    public RawMaterialStorageMenu(Menu menu, ProductionManager gerenciadorProducao) {
        super(menu, gerenciadorProducao, ConsolePrinter.AZUL);
    }

    @Override
    public String icone() {
        return ConsolePrinter.colorir(cor, "≡");
    }

    @Override
    public String rotulo() {
        return "Ver armazém de matéria prima";
    }

    @Override
    public void exibir() {
        List<Product> fabricatedProducts = gerenciadorProducao.getFabricatedProducts();

        // Agrupamento de produtos por listas
        List<Integer> batchNumbers = new ArrayList<>();
        List<List<Product>> batchLists = new ArrayList<>();
        for (Product p : fabricatedProducts) {
            int batchNum = p.getLote();
            int index = batchNumbers.indexOf(batchNum);

            if (index == -1) {
                // Se o lote ainda não existe nas listas, cria um novo
                batchNumbers.add(batchNum);
                List<Product> newList = new ArrayList<>();
                newList.add(p);
                batchLists.add(newList);
            } else {
                // Se já existe, adiciona o produto na lista correspondente ao índice
                batchLists.get(index).add(p);
            }
        }

        // Arrays de controle para expansão (1 ou 0)
        int[] expandedOptions = new int[batchNumbers.size()];
        boolean expandAll = true;

        while (true) {
            imprimirCabecalho();
            System.out.println(" Matérias-prima disponíveis: ");

            RawMaterial rm = gerenciadorProducao.getRawMaterial();
            String line = String.format(cor + "%s" + ConsolePrinter.RESET + ": %.2f %s", rm.getName(), rm.getQuantity(), rm.getUnit());
            ConsolePrinter.listaOpcoes(line, "Voltar");

            menu.printFooter();

            int option = menu.readInt("Escolha: ");

            if (option == 0) {
                break;
            } else {
                menu.setLastBuffer(ConsolePrinter.textoFalha("Dessa vez não é! Opção inválida!"));
            }
        }
    }
}