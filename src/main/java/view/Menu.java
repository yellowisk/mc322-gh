package view;

import domain.utils.ScenarioConfiguration;
import domain.entities.conveyor.Conveyor;
import domain.entities.machine.InspectionMachine;
import domain.entities.machine.PackingMachine;
import domain.entities.machine.ProcessingMachine;
import domain.entities.product.CopoDeVidro;
import domain.entities.product.KitCopoDeVidro;
import domain.entities.product.PoteDeVidro;
import domain.entities.product.Product;
import domain.entities.productionmanager.ProductionManager;
import domain.entities.rawmaterial.RawMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Menu {
    private ScenarioConfiguration currScenario;
    private ProductionManager productionManager;

    private final Scanner scanner = new Scanner(System.in);
    private String lastBuffer; // last message, shows up in the next footer
    private StrategyMenu strategyMenu;
    private List<Submenu> submenus;


    public void start() {
        boolean running = true;

        printIntroScreen();
        scenarioChooserMenu();

        productionManager = new ProductionManager(
                new RawMaterial("Vidro", 50, "kg", 5, 1),
                currScenario.getBudget()
        );

        initProductionManager();

        // Menu: lista de submenus
        List<String> optionsList = new ArrayList<>(
                submenus.stream()
                    .map(submenu -> submenu.icon() + " " + submenu.label())
                    .toList()
        );
        optionsList.add("Sair");
        String[] options = optionsList.toArray(new String[0]);

        while (running) {
            ConsolePrinter.clearScreen();
            ConsolePrinter.card(ConsolePrinter.color(ConsolePrinter.GRAY, "⌂") + " FÁBRICA IDEAL");
            System.out.println();
            System.out.println(" Cenário atual: " + currScenario.getName() + "\n");

            ConsolePrinter.optionsList(options);

            printFooter();
            int option = readInt("Escolha: ");

            if (option == 0) break;

            Submenu chosen;
            try {
                chosen = submenus.get(option - 1);
            } catch (IndexOutOfBoundsException e) {
                setLastBuffer(ConsolePrinter.failText("Dessa vez não é! Opção inválida!"));
                continue;
            }
            chosen.show();
        }
    }

    private void initProductionManager() {
        // Produtos hardcoded
        productionManager.addNewProduct(new CopoDeVidro("Copo", 0.5));
        productionManager.addNewProduct(new PoteDeVidro("Pote", 1.2));
        productionManager.addNewProduct(new KitCopoDeVidro("Kit Copo", 3));
        Product.resetIdCounter();

        // Esteira hardcoded
        productionManager.addNewConveyor(new Conveyor("Esteira Ligeira", 20));

        // Máquinas hardcoded
        productionManager.addNewMachine(new ProcessingMachine(
                        "Máquina de Processamento", 20, 0.25,
                        0.78, currScenario.getScenarioMultiplier(),
                        currScenario.getWearDamage()
                )
        );
        productionManager.addNewMachine(new PackingMachine(
                        "Máquina de Empacotamento", 20, 0.15,
                        0.5, currScenario.getScenarioMultiplier(),
                        currScenario.getWearDamage()
                )
        );
        productionManager.addNewMachine(new InspectionMachine(
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
                new RawMaterialStorageMenu(this, productionManager)
        );

        productionManager.setStrategy(strategyMenu.defaultStrategy());
    }

    private void printIntroScreen() {
        ConsolePrinter.clearScreen();
        ConsolePrinter.line();
        System.out.println(ConsolePrinter.BOLD + ConsolePrinter.ORANGE
                + ConsolePrinter.centerString(65, "FÁBRICA IDEAL") + ConsolePrinter.RESET);
        System.out.println(ConsolePrinter.GRAY
                + ConsolePrinter.centerString(65, "\"E, sim, cliente ganhou um balão de presente! :D\"")
                + ConsolePrinter.RESET);
        ConsolePrinter.line();
        System.out.println();
        System.out.println("   Bem-vindos à " + ConsolePrinter.ORANGE + "Fábrica Ideal" + ConsolePrinter.RESET + "! Aqui cliente não pediu");
        System.out.println("   150 kg de vidro e, sim, ganha produtos ótimos de presente!");
        System.out.println("   Os balões de presente ficam no " + ConsolePrinter.BLUE + "@merc.adinhoideall" + ConsolePrinter.RESET + ", nosso");
        System.out.println("   fiel cliente!");
        System.out.println();
        System.out.println("   " + ConsolePrinter.GREEN + "Desenvolvido por:" + ConsolePrinter.RESET);
        System.out.println("     • Heitor Almeida    " + ConsolePrinter.GRAY + "RA: 245293" + ConsolePrinter.RESET);
        System.out.println("     • Glayson Oliveira  " + ConsolePrinter.GRAY + "RA: 281213" + ConsolePrinter.RESET);
        System.out.println();
        ConsolePrinter.line();
        System.out.println();
        System.out.println(ConsolePrinter.color(ConsolePrinter.YELLOW, "Pressione ENTER para começar..."));
        scanner.nextLine();
    }

    private void scenarioChooserMenu() {
        ScenarioConfiguration[] scenariosList = {
                new ScenarioConfiguration("Mercadinho Ideal", 1000, 0.6, 0),
                new ScenarioConfiguration("Mercadinho Razoável", 1000, 1, 2),
                new ScenarioConfiguration("Mercadinho Precário", 1000, 1.25, 2)
        };

        // Lista de strings para o ConsolePrinter.optionsList
        String[] scenariosStringList = {
                scenariosList[0].getName(),
                scenariosList[1].getName(),
                scenariosList[2].getName(),
                "Sair"
        };

        while (true) {
            ConsolePrinter.clearScreen();
            ConsolePrinter.card("SELECIONADOR DE CENÁRIO");

            ConsolePrinter.optionsList(scenariosStringList);

            int option = readInt("Escolha: ");

            if (option == 0) System.exit(0);

            try {
                currScenario = scenariosList[option - 1];
                break;
            } catch (IndexOutOfBoundsException e) {
                System.out.println("\n" + ConsolePrinter.failText("Dessa vez não é! Opção de cenário inválida!"));
                waitEnter();
            }
        }
    }

    // === Stuff the submenus use ===

    int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            }
            scanner.next();
            System.out.println(ConsolePrinter.failText("Dessa vez não é! Digite um número."));
        }
    }

    /** Waits for ENTER, but first eats the leftover line nextInt() leaves behind */
    void waitEnter() {
        System.out.println("\n" + ConsolePrinter.color(ConsolePrinter.YELLOW, "Pressione ENTER para voltar..."));
        scanner.nextLine();
        scanner.nextLine();
    }

    void setLastBuffer(String message) {
        this.lastBuffer = message;
    }

    void printFooter() {
        if (this.lastBuffer != null) {
            ConsolePrinter.card("%s", this.lastBuffer);
            this.lastBuffer = null;
        } else {
            ConsolePrinter.line();
        }
        ConsolePrinter.printOneLineStats(productionManager.getBudget(),
                productionManager.getRawMaterial().getQuantity(),
                productionManager.getFabricatedProducts().size());
        System.out.println();
    }
}
