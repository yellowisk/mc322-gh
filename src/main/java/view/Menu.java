package view;

import domain.entities.report.Report;
import domain.exceptions.InvalidOptionException;
import domain.exceptions.PlantaException;
import domain.utils.ScenarioConfig;
import domain.entities.conveyor.Conveyor;
import domain.entities.machine.InspectionMachine;
import domain.entities.machine.PackingMachine;
import domain.entities.machine.ProcessingMachine;
import domain.entities.product.CopoDeVidro;
import domain.entities.product.KitCopoDeVidro;
import domain.entities.product.PoteDeVidro;
import domain.entities.product.Produto;
import domain.entities.productionmanager.ProductionManager;
import domain.entities.rawmaterial.RawMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Classe principal para os menus. Aqui fica a lógica principal
 * das telas da fábrica e é a partir daqui que os submenus são chamados
 */
public class Menu {
    private ScenarioConfig currScenario;
    private ProductionManager productionManager;
    private Report report = new Report();

    private final Scanner scanner = new Scanner(System.in);
    private String lastBuffer; // last message, shows up in the next footer
    private StrategyMenu strategyMenu;
    private List<Submenu> submenus;


    /**
     * Método de inicialização do menu, chamado na main
     */
    public void start() {
        boolean running = true;

        printIntroScreen();
        scenarioChooserMenu();

        productionManager = new ProductionManager(
                new RawMaterial("Vidro", 50, "kg", 5, 1),
                currScenario.getBudget(),
                report
        );

        initProductionManager();

        // Menu: lista de submenus
        List<String> optionsList = new ArrayList<>(
                submenus.stream()
                    .map(submenu -> submenu.icone() + " " + submenu.rotulo())
                    .toList()
        );
        optionsList.add("Sair");
        String[] options = optionsList.toArray(new String[0]);

        /**
         * Lppp principal
         */
        while (running) {
            ConsolePrinter.limparTela();
            ConsolePrinter.cartao(ConsolePrinter.colorir(ConsolePrinter.CINZA, "⌂") + " FÁBRICA IDEAL");
            System.out.println();
            System.out.println(" Cenário atual:    " + currScenario.getDisplayName());
            System.out.println(" Estratégia atual: " + productionManager.getCurrEstrategia().getNomeEstrategia() + "\n");

            ConsolePrinter.listaOpcoes(options);

            printFooter();
            int option = readInt("Escolha: ");

            if (option == 0) break;

            try {
                validarOpcao(option, submenus.size(), "Dessa vez não é! Opção inválida!");
                submenus.get(option - 1).exibir();
            } catch (PlantaException e) {
                // Qualquer erro de domínio que um submenu não tratou volta pro menu principal
                setLastBuffer(ConsolePrinter.textoFalha("%s", e.getMessage()));
            }
        }
    }

    /**
     * Método para iniciar o productionManager. Rotina transformada em método
     * para simplificar o loop principal do menu
     */
    private void initProductionManager() {
        // Produtos hardcoded
        productionManager.adicionarNovoProduto(new CopoDeVidro("Copo", 0.5));
        productionManager.adicionarNovoProduto(new PoteDeVidro("Pote", 1.2));
        productionManager.adicionarNovoProduto(new KitCopoDeVidro("Kit Copo", 3));
        Produto.resetarContadorId();

        // Esteira hardcoded
        productionManager.addNewConveyor(new Conveyor("Esteira Ligeira", 20));

        // Máquinas hardcoded
        productionManager.adicionarMaquina(new ProcessingMachine(
                        "Máquina de Processamento", 20, 0.25,
                        0.78, currScenario.getScenarioMultiplier(),
                        currScenario.getWearDamage()
                )
        );
        productionManager.adicionarMaquina(new PackingMachine(
                        "Máquina de Empacotamento", 20, 0.15,
                        0.5, currScenario.getScenarioMultiplier(),
                        currScenario.getWearDamage()
                )
        );
        productionManager.adicionarMaquina(new InspectionMachine(
                        "Máquina de Inspeção", 20, 0.10,
                        0.43, currScenario.getScenarioMultiplier(),
                        currScenario.getWearDamage()
                )
        );

        strategyMenu = new StrategyMenu(this, productionManager);

        // List order = option number on the main menu
        submenus = List.of(
                new UpdateDemandMenu(this, productionManager),
                new FabricateDemandMenu(this, productionManager),
                new StorageMenu(this, productionManager),
                new BuyRawMaterialMenu(this, productionManager),
                strategyMenu,
                new RepairMenu(this, productionManager),
                new AuditMenu(this, productionManager),
                new RawMaterialStorageMenu(this, productionManager),
                new SpeedMenu(this, productionManager)
        );

        productionManager.setCurrEstrategia(strategyMenu.estrategiaPadrao());
    }

    /**
     * Tela inicial
     */
    private void printIntroScreen() {
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
        scanner.nextLine();
    }

    /**
     * Tela de seleção de cenário
     */
    private void scenarioChooserMenu() {

        ScenarioConfig[] scenariosList = {
                new ScenarioConfig("Mercadinho Ideal", "Ideal", 1000, 0.5, 1),
                new ScenarioConfig("Mercadinho Razoável", "Intermediário", 500, 1, 2),
                new ScenarioConfig("Mercadinho Precário", "Apocalíptico", 250, 1.5, 4)
        };

        // Lista de strings para o ConsolePrinter.listaOpcoes
        String[] scenariosStringList = {
                scenariosList[0].getDisplayName(),
                scenariosList[1].getDisplayName(),
                scenariosList[2].getDisplayName(),
                "Sair"
        };

        while (true) {
            ConsolePrinter.limparTela();
            ConsolePrinter.cartao("SELECIONADOR DE CENÁRIO");

            ConsolePrinter.listaOpcoes(scenariosStringList);

            int option = readInt("Escolha: ");

            if (option == 0) System.exit(0);

            try {
                validarOpcao(option, scenariosList.length, "Dessa vez não é! Opção de cenário inválida!");
                currScenario = scenariosList[option - 1];
                break;
            } catch (InvalidOptionException e) {
                System.out.println("\n" + ConsolePrinter.textoFalha("%s", e.getMessage()));
                waitEnter();
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

    int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            }
            scanner.next();
            System.out.println(ConsolePrinter.textoFalha("Dessa vez não é! Digite um número."));
        }
    }

    /** Waits for ENTER, but first eats the leftover line nextInt() leaves behind */
    void waitEnter() {
        System.out.println("\n" + ConsolePrinter.colorir(ConsolePrinter.AMARELO, "Pressione ENTER para voltar..."));
        scanner.nextLine();
        scanner.nextLine();
    }

    void setLastBuffer(String message) {
        this.lastBuffer = message;
    }

    void printFooter() {
        if (this.lastBuffer != null) {
            ConsolePrinter.cartao("%s", this.lastBuffer);
            this.lastBuffer = null;
        } else {
            ConsolePrinter.linha();
        }
        ConsolePrinter.imprimirResumo(productionManager.getVerba(),
                productionManager.getMateriaPrima().getQuantity(),
                productionManager.getProdutosFabricados().size());
        System.out.println();
    }
}
