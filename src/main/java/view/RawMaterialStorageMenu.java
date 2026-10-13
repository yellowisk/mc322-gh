package view;

import domain.entities.product.Produto;
import domain.entities.productionmanager.ProductionManager;
import domain.entities.rawmaterial.MateriaPrima;

import java.util.ArrayList;
import java.util.List;

/**
 * Submenu do armazém de matéria-prima. Mostra o estoque de matéria-prima, separado do armazém de
 * produtos acabados
 *
 * @author Glayson Oliveira
 */
public class RawMaterialStorageMenu extends Submenu {

    /**
     * Cria o submenu c/ a cor azul
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção, de onde vem a matéria-prima
     */
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
        List<Produto> fabricatedProducts = gerenciadorProducao.getProdutosFabricados();

        // Agrupamento de produtos por listas
        List<Integer> batchNumbers = new ArrayList<>();
        List<List<Produto>> batchLists = new ArrayList<>();
        for (Produto p : fabricatedProducts) {
            int batchNum = p.getLote();
            int index = batchNumbers.indexOf(batchNum);

            if (index == -1) {
                // Se o lote ainda não existe nas listas, cria um novo
                batchNumbers.add(batchNum);
                List<Produto> newList = new ArrayList<>();
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

            MateriaPrima rm = gerenciadorProducao.getMateriaPrima();
            String line = String.format(cor + "%s" + ConsolePrinter.RESET + ": %.2f %s", rm.getNome(), rm.getQuantidade(), rm.getUnidade());
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