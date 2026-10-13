package view;

import domain.entities.report.Relatorio;
import domain.exceptions.InvalidOptionException;
import domain.exceptions.PlantaException;
import domain.utils.ConfiguracaoCenario;
import domain.entities.conveyor.Esteira;
import domain.entities.machine.MaquinaInspecao;
import domain.entities.machine.MaquinaEmpacotamento;
import domain.entities.machine.MaquinaProcessamento;
import domain.entities.product.CopoDeVidro;
import domain.entities.product.KitCopoDeVidro;
import domain.entities.product.PoteDeVidro;
import domain.entities.product.Produto;
import domain.entities.productionmanager.GerenciadorProducao;
import domain.entities.rawmaterial.MateriaPrima;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Classe principal para os menus. Aqui fica a lógica principal
 * das telas da fábrica e é a partir daqui que os submenus são chamados
 */
public class Menu {
    private ConfiguracaoCenario cenarioAtual;
    private GerenciadorProducao gerenciadorProducao;
    private Relatorio relatorio = new Relatorio();

    private final Scanner leitor = new Scanner(System.in);
    private String ultimaMensagem; // last mensagem, shows up in the next footer
    private MenuEstrategia menuEstrategia;
    private List<Submenu> submenus;


    /**
     * Método de inicialização do menu, chamado na main
     */
    public void iniciar() {
        boolean rodando = true;

        imprimirTelaInicial();
        escolherCenario();

        gerenciadorProducao = new GerenciadorProducao(
                new MateriaPrima("Vidro", 50, "kg", 5, 1),
                cenarioAtual.getVerba(),
                relatorio
        );

        iniciarGerenciadorProducao();

        // Menu: lista de submenus
        List<String> listaOpcoes = new ArrayList<>(
                submenus.stream()
                    .map(submenu -> submenu.icone() + " " + submenu.rotulo())
                    .toList()
        );
        listaOpcoes.add("Sair");
        String[] opcoes = listaOpcoes.toArray(new String[0]);

        /**
         * Lppp principal
         */
        while (rodando) {
            ConsolePrinter.limparTela();
            ConsolePrinter.cartao(ConsolePrinter.colorir(ConsolePrinter.CINZA, "⌂") + " FÁBRICA IDEAL");
            System.out.println();
            System.out.println(" Cenário atual:    " + cenarioAtual.getNomeExibicao());
            System.out.println(" Estratégia atual: " + gerenciadorProducao.getEstrategiaAtual().getNomeEstrategia() + "\n");

            ConsolePrinter.listaOpcoes(opcoes);

            imprimirRodape();
            int opcao = lerInteiro("Escolha: ");

            if (opcao == 0) break;

            try {
                validarOpcao(opcao, submenus.size(), "Dessa vez não é! Opção inválida!");
                submenus.get(opcao - 1).exibir();
            } catch (PlantaException e) {
                // Qualquer erro de domínio que um submenu não tratou volta pro menu principal
                setUltimaMensagem(ConsolePrinter.textoFalha("%s", e.getMessage()));
            }
        }
    }

    /**
     * Método para iniciar o gerenciadorProducao. Rotina transformada em método
     * para simplificar o loop principal do menu
     */
    private void iniciarGerenciadorProducao() {
        // Produtos hardcoded
        gerenciadorProducao.adicionarNovoProduto(new CopoDeVidro("Copo", 0.5));
        gerenciadorProducao.adicionarNovoProduto(new PoteDeVidro("Pote", 1.2));
        gerenciadorProducao.adicionarNovoProduto(new KitCopoDeVidro("Kit Copo", 3));
        Produto.resetarContadorId();

        // Esteira hardcoded
        gerenciadorProducao.adicionarEsteira(new Esteira("Esteira Ligeira", 20));

        // Máquinas hardcoded
        gerenciadorProducao.adicionarMaquina(new MaquinaProcessamento(
                        "Máquina de Processamento", 20, 0.25,
                        0.78, cenarioAtual.getMultiplicadorCenario(),
                        cenarioAtual.getDesgasteMaximo()
                )
        );
        gerenciadorProducao.adicionarMaquina(new MaquinaEmpacotamento(
                        "Máquina de Empacotamento", 20, 0.15,
                        0.5, cenarioAtual.getMultiplicadorCenario(),
                        cenarioAtual.getDesgasteMaximo()
                )
        );
        gerenciadorProducao.adicionarMaquina(new MaquinaInspecao(
                        "Máquina de Inspeção", 20, 0.10,
                        0.43, cenarioAtual.getMultiplicadorCenario(),
                        cenarioAtual.getDesgasteMaximo()
                )
        );

        menuEstrategia = new MenuEstrategia(this, gerenciadorProducao);

        // List order = opcao number on the main menu
        submenus = List.of(
                new MenuAtualizarDemandas(this, gerenciadorProducao),
                new MenuFabricarDemandas(this, gerenciadorProducao),
                new MenuArmazem(this, gerenciadorProducao),
                new MenuComprarMateriaPrima(this, gerenciadorProducao),
                menuEstrategia,
                new MenuReparo(this, gerenciadorProducao),
                new MenuAuditoria(this, gerenciadorProducao),
                new MenuArmazemMateriaPrima(this, gerenciadorProducao),
                new MenuVelocidade(this, gerenciadorProducao)
        );

        gerenciadorProducao.setEstrategiaAtual(menuEstrategia.estrategiaPadrao());
    }

    /**
     * Tela inicial
     */
    private void imprimirTelaInicial() {
        ConsolePrinter.limparTela();
        ConsolePrinter.linha();
        System.out.println(ConsolePrinter.NEGRITO + ConsolePrinter.LARANJA
                + ConsolePrinter.centralizar(65, "FÁBRICA IDEAL") + ConsolePrinter.RESET);
        System.out.println(ConsolePrinter.CINZA
                + ConsolePrinter.centralizar(65, "\"E, sim, cliente ganhou um balão de presente! :D\"")
                + ConsolePrinter.RESET);
        ConsolePrinter.linha();
        System.out.println();
        System.out.println("   Bem-vindos à " + ConsolePrinter.LARANJA + "Fábrica Ideal" + ConsolePrinter.RESET + "! Aqui cliente não pediu");
        System.out.println("   150 kg de vidro e, sim, ganha produtos ótimos de presente!");
        System.out.println("   Os balões de presente ficam no " + ConsolePrinter.AZUL + "@merc.adinhoideall" + ConsolePrinter.RESET + ", nosso");
        System.out.println("   fiel cliente!");
        System.out.println();
        System.out.println("   " + ConsolePrinter.VERDE + "Desenvolvido por:" + ConsolePrinter.RESET);
        System.out.println("     • Heitor Almeida    " + ConsolePrinter.CINZA + "RA: 245293" + ConsolePrinter.RESET);
        System.out.println("     • Glayson Oliveira  " + ConsolePrinter.CINZA + "RA: 281213" + ConsolePrinter.RESET);
        System.out.println();
        ConsolePrinter.linha();
        System.out.println();
        System.out.println(ConsolePrinter.colorir(ConsolePrinter.AMARELO, "Pressione ENTER para começar..."));
        leitor.nextLine();
    }

    /**
     * Tela de seleção de cenário
     */
    private void escolherCenario() {

        ConfiguracaoCenario[] cenarios = {
                new ConfiguracaoCenario("Mercadinho Ideal", "Ideal", 1000, 0.5, 1),
                new ConfiguracaoCenario("Mercadinho Razoável", "Intermediário", 500, 1, 2),
                new ConfiguracaoCenario("Mercadinho Precário", "Apocalíptico", 250, 1.5, 4)
        };

        // Lista de strings para o ConsolePrinter.listaOpcoes
        String[] nomesCenarios = {
                cenarios[0].getNomeExibicao(),
                cenarios[1].getNomeExibicao(),
                cenarios[2].getNomeExibicao(),
                "Sair"
        };

        while (true) {
            ConsolePrinter.limparTela();
            ConsolePrinter.cartao("SELECIONADOR DE CENÁRIO");

            ConsolePrinter.listaOpcoes(nomesCenarios);

            int opcao = lerInteiro("Escolha: ");

            if (opcao == 0) System.exit(0);

            try {
                validarOpcao(opcao, cenarios.length, "Dessa vez não é! Opção de cenário inválida!");
                cenarioAtual = cenarios[opcao - 1];
                break;
            } catch (InvalidOptionException e) {
                System.out.println("\n" + ConsolePrinter.textoFalha("%s", e.getMessage()));
                esperarEnter();
            }
        }
    }

    // === Stuff the submenus use ===

    /**
     * Confere se a opção escolhida existe numa lista numerada de 1 até {@code quantidade}
     *
     * @param opcao opção digitada pelo usuário
     * @param quantidade quantas opções a lista tem
     * @param mensagem mensagem de erro p/ mostrar se a opção não existir
     * @throws InvalidOptionException se a opção estiver fora da lista
     */
    static void validarOpcao(int opcao, int quantidade, String mensagem) {
        if (opcao < 1 || opcao > quantidade) {
            throw new InvalidOptionException(mensagem);
        }
    }

    /**
     * Lê um inteiro do usuário. Se ele digitar algo que não é número, avisa e pergunta de novo
     *
     * @param texto texto mostrado antes da entrada
     * @return o número digitado
     */
    int lerInteiro(String texto) {
        while (true) {
            System.out.print(texto);
            if (leitor.hasNextInt()) {
                return leitor.nextInt();
            }
            leitor.next();
            System.out.println(ConsolePrinter.textoFalha("Dessa vez não é! Digite um número."));
        }
    }

    /** Espera o ENTER, mas antes consome a linha que sobra depois do nextInt() */
    void esperarEnter() {
        System.out.println("\n" + ConsolePrinter.colorir(ConsolePrinter.AMARELO, "Pressione ENTER para voltar..."));
        leitor.nextLine();
        leitor.nextLine();
    }

    /**
     * Guarda uma mensagem p/ aparecer no próximo rodapé, ex.: o resultado de uma operação
     *
     * @param mensagem mensagem já formatada, normalmente vinda de {@link ConsolePrinter#textoOk} ou {@link ConsolePrinter#textoFalha}
     */
    void setUltimaMensagem(String mensagem) {
        this.ultimaMensagem = mensagem;
    }

    /**
     * Imprime o rodapé dos menus: a última mensagem guardada (se tiver, e depois apaga ela) e o resumo
     * c/ verba, matéria-prima e produtos no armazém
     */
    void imprimirRodape() {
        if (this.ultimaMensagem != null) {
            ConsolePrinter.cartao("%s", this.ultimaMensagem);
            this.ultimaMensagem = null;
        } else {
            ConsolePrinter.linha();
        }
        ConsolePrinter.imprimirResumo(gerenciadorProducao.getVerba(),
                gerenciadorProducao.getMateriaPrima().getQuantidade(),
                gerenciadorProducao.getProdutosFabricados().size());
        System.out.println();
    }
}
