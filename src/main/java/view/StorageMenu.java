package view;

import domain.entities.product.Produto;
import domain.entities.productionmanager.ProductionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Submenu do armazém. Agrupa os produtos aprovados por lote e mostra cada lote c/ produto, qualidade
 * e total. Cada lote pode ser expandido p/ ver as unidades c/ o risco de rejeição e se precisam de manutenção
 *
 * @author Heitor Almeida
 * @author Glayson Oliveira
 */
public class StorageMenu extends Submenu {

    /**
     * Cria o submenu c/ a cor azul
     *
     * @param menu menu principal
     * @param gerenciadorProducao gerenciador da produção
     */
    public StorageMenu(Menu menu, ProductionManager gerenciadorProducao) {
        super(menu, gerenciadorProducao, ConsolePrinter.AZUL);
    }

    @Override
    public String icone() {
        return ConsolePrinter.colorir(cor, "≡");
    }

    @Override
    public String rotulo() {
        return "Ver armazém";
    }

    /**
     * Loop do submenu. O número de um lote expande ou recolhe ele, 99 expande ou recolhe todos.
     */
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
            System.out.println(" Lista de lotes: ");

            if (numerosLotes.isEmpty()) {
                System.out.println(ConsolePrinter.CINZA + " O armazém está vazio. Nenhum produto finalizado." + ConsolePrinter.RESET + "\n");
            } else {
                for (int i = 0; i < numerosLotes.size(); i++) {
                    int idLote = numerosLotes.get(i);
                    List<Produto> produtosDoLote = listasLotes.get(i);

                    // Linha principal do lote (não expansível)
                    System.out.printf(ConsolePrinter.CINZA + " %d." + ConsolePrinter.RESET + " Lote #%d: %s (★ %.2f) [Total: %d un]\n",
                            (i + 1), idLote,
                            produtosDoLote.getFirst().getNome(),
                            produtosDoLote.getFirst().getQualidade(),
                            produtosDoLote.size());

                    // Forma expandida: lista os produtos internos do lote
                    if (opcoesExpandidas[i] == 1) {
                        for (Produto p : produtosDoLote) {
                            String simboloArvore = (p == produtosDoLote.getLast()) ? "└─" : "├─";
                            System.out.printf("    " + ConsolePrinter.CINZA + " %s %s #%d, Risco %s, %s\n" + ConsolePrinter.RESET,
                                    simboloArvore,
                                    p.getNome(), p.getId(), String.format("%.0f%%", p.getRiscoRejeicao() * 100),
                                    p.precisaManutencao()
                                            ? ConsolePrinter.colorir(ConsolePrinter.VERMELHO, "✗ Alto risco")
                                            : ConsolePrinter.colorir(ConsolePrinter.VERDE, "✓ OK")
                                    );
                        }
                    }
                }
            }

            System.out.println();
            if (!numerosLotes.isEmpty()) {
                System.out.printf(ConsolePrinter.CINZA + " 99." + ConsolePrinter.RESET + " Expandir/Recolher todos\n\n");
            }

            ConsolePrinter.imprimirOpcaoVoltar();
            menu.printFooter();

            int opcao = menu.readInt("Escolha: ");

            if (opcao == 0) break;

            // Opção 99 para Expandir/Recolher tudo
            if (opcao == 99 && !numerosLotes.isEmpty()) {
                int novoEstado = expandirTodos ? 1 : 0;
                Arrays.fill(opcoesExpandidas, novoEstado);
                expandirTodos = !expandirTodos;
                continue;
            }

            try {
                opcoesExpandidas[opcao - 1] ^= 1; // Inverte a flag usando bitwise XOR
            } catch (IndexOutOfBoundsException e) {
                menu.setLastBuffer(ConsolePrinter.textoFalha("Dessa vez não é! Opção de lote inválida!"));
            }
        }
    }
}