package view;

import domain.entities.product.Produto;
import domain.entities.productionmanager.GerenciadorProducao;
import domain.entities.rawmaterial.MateriaPrima;

import java.util.ArrayList;
import java.util.List;

/**
 * Submenu do armazém de matéria-prima. Mostra o estoque de matéria-prima, separado do armazém de
 * produtos acabados
 *
 * @author Glayson Oliveira
 */
public class MenuArmazemMateriaPrima extends Submenu {

    /**
     * Cria o submenu c/ a cor azul
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção, de onde vem a matéria-prima
     */
    public MenuArmazemMateriaPrima(Menu menu, GerenciadorProducao gerenciadorProducao) {
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
        List<Produto> produtosFabricados = gerenciadorProducao.getProdutosFabricados();

        // Agrupamento de produtos por listas
        List<Integer> numerosLotes = new ArrayList<>();
        List<List<Produto>> listasLotes = new ArrayList<>();
        for (Produto p : produtosFabricados) {
            int numeroLote = p.getLote();
            int indice = numerosLotes.indexOf(numeroLote);

            if (indice == -1) {
                // Se o lote ainda não existe nas listas, cria um novo
                numerosLotes.add(numeroLote);
                List<Produto> novaLista = new ArrayList<>();
                novaLista.add(p);
                listasLotes.add(novaLista);
            } else {
                // Se já existe, adiciona o produto na lista correspondente ao índice
                listasLotes.get(indice).add(p);
            }
        }

        // Arrays de controle para expansão (1 ou 0)
        int[] opcoesExpandidas = new int[numerosLotes.size()];
        boolean expandirTodos = true;

        while (true) {
            imprimirCabecalho();
            System.out.println(" Matérias-prima disponíveis: ");

            MateriaPrima rm = gerenciadorProducao.getMateriaPrima();
            String linha = String.format(cor + "%s" + ConsolePrinter.RESET + ": %.2f %s", rm.getNome(), rm.getQuantidade(), rm.getUnidade());
            ConsolePrinter.listaOpcoes(linha, "Voltar");

            menu.imprimirRodape();

            int opcao = menu.lerInteiro("Escolha: ");

            if (opcao == 0) {
                break;
            } else {
                menu.setUltimaMensagem(ConsolePrinter.textoFalha("Dessa vez não é! Opção inválida!"));
            }
        }
    }
}