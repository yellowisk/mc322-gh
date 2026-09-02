package br.unicamp.ideal;

import br.unicamp.ideal.domain.entities.machines.inspectionStation.InspectionStation;
import br.unicamp.ideal.domain.entities.rawmaterial.RawMaterial;
import br.unicamp.ideal.domain.entities.machines.conveyor.Conveyor;
import br.unicamp.ideal.domain.entities.machines.machine.Machine;
import br.unicamp.ideal.domain.entities.product.Product;
import br.unicamp.ideal.domain.entities.product.ProductStatus;

import java.util.Scanner;

public class Main {

    static void main() {
        // >>> INSTANCES
        // Raw Materials
        RawMaterial vidro = new RawMaterial("Vidro", 50, "kg", 5);

        // Products
        Product copoDeVidro = new Product("Copo de Vidro", 3);
        Product tacaDeVidro = new Product( "Taça de Vidro", 2);
        Product copoJoaozinho = new Product( "Copo de Vidro do Joãozinho", 6);

        // Other
        Conveyor esteiraIdeal = new Conveyor("Esteira Ideal", 8);
        Machine maquinaIdeal = new Machine("Máquina Ideal", 8);
        InspectionStation estacaoDeInspecaoIdeal = new InspectionStation();

        // >>> MENU
        System.out.printf("""
                ========================================
                INDUSTRIAL PLANT
                ========================================
                
                Raw Material: %s - %s
                Quantity: %d
                Unity: %s
                
                """, vidro.getId().toString(), vidro.getName(), vidro.getQuantity(), vidro.getUnit());

        System.out.printf("""
                        Available Products:
                        1 - %s (demand: %d %s)
                        2 - %s (demand: %d %s)
                        3 - %s (demand: %d %s)
                        
                        %n""", copoDeVidro.getName(), copoDeVidro.getRawMaterialAmountNeeded(), vidro.getUnit(),
        tacaDeVidro.getName(), tacaDeVidro.getRawMaterialAmountNeeded(), vidro.getUnit(),
        copoJoaozinho.getName(), copoJoaozinho.getRawMaterialAmountNeeded(), vidro.getUnit());

        Scanner scanner = new Scanner(System.in);

        mainLoop:
        while(true) {
            // >>>> MENU CHOICES
            System.out.println("""
                    ========================================
                    MAIN MENU
                    ========================================
                    1 - Start production
                    2 - Look up stock
                    3 - Update stock
                    4 - Exit
                    Choose one:""");

            if (!scanner.hasNextInt()) {
                if(!scanner.hasNext()) break mainLoop;
                System.out.printf("[NÃO FOI DESSA VEZ...] %s ain't a number :b%n", scanner.next());
                continue;
            }

            int choice = scanner.nextInt();

            switch (choice) {
                case 1: // Start production

                    // validate input
                    System.out.println("Select a product (1-3):");
                    if (!scanner.hasNextInt()) {
                        if (!scanner.hasNext()) break mainLoop;
                        System.out.printf("[NÃO FOI DESSA VEZ...] %s ain't a number :b%n", scanner.next());
                        continue;
                    }
                    int pInput = scanner.nextInt();

                    // validate option
                    Product productChosen;
                    switch (pInput) {
                        case 1:
                            productChosen = copoDeVidro;
                            break;
                        case 2:
                            productChosen = tacaDeVidro;
                            break;
                        case 3:
                            productChosen = copoJoaozinho;
                            break;
                        default:
                            System.out.printf("[NÃO FOI DESSA VEZ...] %d ain't a product on the list :b%n", pInput);
                            continue;
                    }

                    if (productChosen.getStatus() == ProductStatus.PROCESSED) {
                        System.out.printf("[NÃO FOI DESSA VEZ...] %s has already been processed%n", productChosen.getName());
                        continue;
                    }

                    // check demand
                    System.out.printf("Please, type the raw material demand (%s): %n", vidro.getUnit());
                    if(!scanner.hasNextInt()) {
                        if(!scanner.hasNext()) break mainLoop;
                        System.out.printf("[NÃO FOI DESSA VEZ...] %s ain't a number :b%n", scanner.next());
                        continue;
                    }
                    int demandChosen = scanner.nextInt();

                    if (demandChosen < productChosen.getRawMaterialAmountNeeded()) {
                        System.out.printf("[NÃO FOI DESSA VEZ...] The demand of %d %s is below the %d %s needed to make one %s.%n",
                                demandChosen, vidro.getUnit(), productChosen.getRawMaterialAmountNeeded(), vidro.getUnit(), productChosen.getName());
                        continue;
                    }

                    // check availability
                    System.out.printf("Verifying the %s availability...%n", vidro.getName());

                    if (vidro.isAvailable(demandChosen)) {
                        System.out.printf("[EITCHA!] The demand of %d %s can be addressed!%n", demandChosen, vidro.getUnit());
                    } else if (demandChosen <= 0) {
                        System.out.printf("[NÃO FOI DESSA VEZ...] The demand of %d %s ain't a positive amount :b%n",
                                demandChosen, vidro.getUnit());
                        continue;
                    } else if (vidro.getQuantity() < demandChosen) {
                        System.out.printf("[NÃO FOI DESSA VEZ...] There's only %d %s of %s in stock, and the demand is %d %s.%n",
                                vidro.getQuantity(), vidro.getUnit(), vidro.getName(), demandChosen, vidro.getUnit());
                        continue;
                    } else {
                        System.out.printf("[NÃO FOI DESSA VEZ...] Consuming %d %s would leave only %d %s of %s in stock, below the %d %s minimum.%n",
                                demandChosen, vidro.getUnit(), vidro.getQuantity() - demandChosen, vidro.getUnit(),
                                vidro.getName(), vidro.getMinQuantity(), vidro.getUnit());
                        continue;
                    }

                    // >>> TURN ON EQUIPMENT
                    esteiraIdeal.turnOn();
                    System.out.printf("[EITCHA!] The %s is on!%n", esteiraIdeal.getName());
                    maquinaIdeal.turnOn();
                    System.out.printf("[EITCHA!] The %s is on!%n", maquinaIdeal.getName());
                    estacaoDeInspecaoIdeal.turnOn();
                    System.out.printf("[EITCHA!] The Inspection Station is on!%n");

                    // >>> PROCESSING
                    try {
                        // 1. put the raw material on the conveyor
                        esteiraIdeal.addRawMaterial(demandChosen);
                        System.out.printf("[EITCHA!] Raw material %s added to the conveyor.\n", vidro.getName());

                        // 2. carry the raw material to the machine and process it
                        System.out.println("[EITCHA!] Raw material carried to the machine.");
                        System.out.printf("[EITCHA!] Processing %d %s of %s...\n", demandChosen, vidro.getUnit(), vidro.getName());
                        maquinaIdeal.process(vidro, esteiraIdeal.removeRawMaterial(), productChosen);
                        System.out.printf("[EITCHA!] Product %s-%d created.\n", productChosen.getName(), productChosen.getId());

                        // 3. put the product on the conveyor
                        esteiraIdeal.addProduct(productChosen);
                        System.out.printf("[EITCHA!] Product %s %d added to the conveyor.\n", productChosen.getName(), productChosen.getId());

                        // 4. carry the raw material to the inspection station and inspect it
                        System.out.printf("[EITCHA!] Product %s %d carried to the inspection station.\n", productChosen.getName(), productChosen.getId());
                        System.out.printf("[EITCHA!] Inspecting product %s-%d...\n", productChosen.getName(), productChosen.getId());
                        estacaoDeInspecaoIdeal.inspect(esteiraIdeal.removeProduct());
                        System.out.printf("[EITCHA!] Product %s-%d approved on the inspection.\n", productChosen.getName(), productChosen.getId());

                    } catch (IllegalArgumentException | IllegalStateException e) {
                        System.out.printf("[NÃO FOI DESSA VEZ...] %s%n", e.getMessage());

                        // Here we be freeing the conveyor, so a failed run doesn't block the next one.
                        if (esteiraIdeal.getRawMaterial() > 0)
                            esteiraIdeal.removeRawMaterial();
                        if (esteiraIdeal.getProduct() != null)
                            esteiraIdeal.removeProduct();
                        continue;
                    }

                    System.out.printf("""
                            ========================================
                            PRODUCTION SUCCESFULLY FINISHED
                            ========================================
                            
                            Remaining stock of %s: %d
                            %n""", vidro.getName(), vidro.getQuantity());
                    break;
                case 2:
                    System.out.printf("""
                            ========================================
                            STOCK
                            ========================================
                            
                            %s: %d %s (minimum: %d %s)
                            
                            Products:
                            %d - %s: %s
                            %d - %s: %s
                            %d - %s: %s
                            %n""", vidro.getName(), vidro.getQuantity(), vidro.getUnit(), vidro.getMinQuantity(), vidro.getUnit(),
                    copoDeVidro.getId(), copoDeVidro.getName(), copoDeVidro.getStatus(),
                    tacaDeVidro.getId(), tacaDeVidro.getName(), tacaDeVidro.getStatus(),
                    copoJoaozinho.getId(), copoJoaozinho.getName(), copoJoaozinho.getStatus());
                    break;
                case 3:
                    System.out.printf("""
                            ========================================
                            UPDATE STOCK
                            ========================================
                            
                            Current stock of %s: %d %s (minimum: %d %s)
                            
                            %n""", vidro.getName(), vidro.getQuantity(), vidro.getUnit(), vidro.getMinQuantity(), vidro.getUnit());

                    System.out.printf("Please, type the amount of %s to add (%s): %n", vidro.getName(), vidro.getUnit());
                    if(!scanner.hasNextInt()) {
                        if(!scanner.hasNext()) break mainLoop;
                        System.out.printf("[NÃO FOI DESSA VEZ...] %s ain't a number :b%n", scanner.next());
                        continue;
                    }
                    int amountChosen = scanner.nextInt();

                    try {
                        vidro.addStock(amountChosen);
                        System.out.printf("[EITCHA!] %d %s of %s added to the stock.%n",
                                amountChosen, vidro.getUnit(), vidro.getName());
                    } catch (IllegalArgumentException e) {
                        System.out.printf("[NÃO FOI DESSA VEZ...] %s%n", e.getMessage());
                        continue;
                    }

                    System.out.printf("Current stock of %s: %d %s%n%n", vidro.getName(), vidro.getQuantity(), vidro.getUnit());
                    break;
                case 4:
                    System.out.println("[ACABOU LIGEIRO...] Shutting down the industry plant.");
                    esteiraIdeal.turnOff();
                    break mainLoop;
                default:
                    System.out.printf("[NÃO FOI DESSA VEZ...] %d ain't an option on the menu :b%n", choice);
            }
        }

    }
}
