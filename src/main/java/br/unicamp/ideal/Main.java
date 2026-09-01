package br.unicamp.ideal;

import br.unicamp.ideal.domain.entities.RawMaterial;
import br.unicamp.ideal.domain.entities.machines.conveyor.Conveyor;
import br.unicamp.ideal.domain.entities.machines.machine.Machine;
import br.unicamp.ideal.domain.entities.product.Product;
import br.unicamp.ideal.domain.entities.product.ProductStatus;

import java.util.Scanner;

public class Main {

    static void main() {
        RawMaterial vidro = new RawMaterial("Vidro", 10, "kg", 5);
        Product copoDeVidro = new Product(1, "Copo de Vidro", ProductStatus.UNPROCESSED, 3);
        Product tacaDeVidro = new Product(2, "Taça de Vidro", ProductStatus.UNPROCESSED, 2);
        Product copoJoaozinho = new Product(3, "Copo de Vidro do Joãozinho", ProductStatus.UNPROCESSED, 6);
        Conveyor esteiraIdealDeProcessamento = new Conveyor("Esteira Ideal de Processamento", 8);
        Machine maquinaIdeal = new Machine("Máquina Ideal", true, 8);

        // Turning the conveyor on...
        esteiraIdealDeProcessamento.turnOn();

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
            System.out.println("""
                    ========================================
                    MAIN MENU
                    ========================================
                    1 - Start production
                    2 - Look up stock
                    3 - Exit
                    Choose one: 
                    """);

            if(!scanner.hasNextInt()) {
                if(!scanner.hasNext()) break mainLoop;
                System.out.printf("[NÃO FOI DESSA VEZ...] %s ain't a number :b%n", scanner.next());
                continue;
            }

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.println("Select a product (1-3): \n");
                    if(!scanner.hasNextInt()) {
                        if(!scanner.hasNext()) break mainLoop;
                        System.out.printf("[NÃO FOI DESSA VEZ...] %s ain't a number :b%n", scanner.next());
                        continue;
                    }
                    int pInput = scanner.nextInt();

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

                    if (!esteiraIdealDeProcessamento.getIsOn()) {
                        System.out.printf("[NÃO FOI DESSA VEZ...] Conveyor %s ain't on!%n", esteiraIdealDeProcessamento.getName());
                        continue;
                    } else
                        System.out.printf("[EITCHA!] The conveyor %s is on!%n", esteiraIdealDeProcessamento.getName());

                    if (!maquinaIdeal.getIsOn()) {
                        System.out.printf("[NÃO FOI DESSA VEZ...] Machine %s ain't on!%n", maquinaIdeal.getName());
                        continue;
                    } else
                        System.out.printf("[EITCHA!] The machine %s is on!%n", maquinaIdeal.getName());

                    try {
                        esteiraIdealDeProcessamento.addRawMaterial(productChosen.getRawMaterialAmountNeeded());
                        System.out.printf("[EITCHA!] Raw Material %s added to the conveyor.%n", vidro.getName());
                        System.out.printf("[EITCHA!] %s was sent to the machine %s%n", vidro.getName(), maquinaIdeal.getName());

                        System.out.printf("[EITCHA!] %s is processing %d %s of %s...%n", maquinaIdeal.getName(), demandChosen, vidro.getUnit(), vidro.getName());
                        maquinaIdeal.process(vidro, demandChosen, productChosen);
                        System.out.printf("[EITCHA!] Product %d - %s created successfully.%n", productChosen.getId(), productChosen.getName());

                        esteiraIdealDeProcessamento.removeRawMaterial();
                        esteiraIdealDeProcessamento.addProduct(productChosen);
                        System.out.printf("[EITCHA!] Product %s taken to inspéction.%n", productChosen.getName());
                        esteiraIdealDeProcessamento.removeProduct();
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        System.out.printf("[NÃO FOI DESSA VEZ...] %s%n", e.getMessage());

                        // Here we be freeing the conveyor, so a failed run doesn't block the next one.
                        if (esteiraIdealDeProcessamento.getRawMaterial() > 0)
                            esteiraIdealDeProcessamento.removeRawMaterial();
                        if (esteiraIdealDeProcessamento.getProduct() != null)
                            esteiraIdealDeProcessamento.removeProduct();
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
                    System.out.println("[COMO TEM FORÇA!] Acabou ligeiro...");
                    esteiraIdealDeProcessamento.turnOff();
                    break mainLoop;
                default:
                    System.out.printf("[eitcha...] %d ain't an option on the menu :b%n", choice);
            }
        }

    }
}
