package domain.entities.productionmanager;

import domain.entities.demand.Demand;
import domain.entities.conveyor.Conveyor;
import domain.entities.machine.Machine;
import domain.entities.machine.StatusDeMaquina;
import domain.entities.product.Product;
import domain.entities.product.ProductStatus;
import domain.entities.rawmaterial.RawMaterial;
import domain.exceptions.InsufficientBudgetException;
import domain.exceptions.MachineNeedsRepairException;
import view.ConsolePrinter;

import java.util.ArrayList;
import java.util.List;

public class ProductionManager {
    private List<Demand> demands = new ArrayList<Demand>();
    private List<Product> fabricatedProducts = new ArrayList<Product>();
    private List<Product> availableProducts = new ArrayList<Product>();
    private List<Machine> machines = new ArrayList<Machine>();
    private Conveyor conveyor;
    private Product chosenProduct;
    private RawMaterial rawMaterial;
    private double budget;

    private boolean temMaquinaQuebrada = false;

    /**
     * Lista das etapas de produção instanciada para evitar laço 'for' otimizado
     * direto no Enum.
     */
    private static final ProductionStages[] etapasProducao = ProductionStages.values();

    public ProductionManager(RawMaterial rawMaterial, float budget) {
        this.rawMaterial = rawMaterial;
        this.budget = budget;
    }

    public void addNewProduct(Product newProduct) {
        this.availableProducts.add(newProduct);
        registerDemand(newProduct, 0); // init the product demand
    }

    public void registerDemand(Product product, int amount) {
        this.demands.add(new Demand(product, amount));
    }

    public void updateDemand(Demand demand, int newValue) {
        Product product = getProductByName(demand.getProductName());
        demand.updateAmount(product, newValue);
    }

    public Product getProductByName(String productName) {
        for (Product p: availableProducts) {
            if (p.getName().equalsIgnoreCase(productName)) {
                return p;
            }
        }
        return null;
    }

    public void addNewMachine(Machine machine) {
        this.machines.add(machine);
    }

    /**
     * Método principal da classe.
     * Realiza o processo de fabricação da demanda escolhida.
     *
     * @param demand A demanda que será fabricada.
     */
    public void fabricateDemand(Demand demand) {
        if (temMaquinaQuebrada()) {
            throw new MachineNeedsRepairException("Não podemos iniciar a fabricação. Há máquinas precisando de reparo.");
        }

        this.chosenProduct = this.getProductByName(demand.getProductName());

        if (chosenProduct == null) {
            ConsolePrinter.fail("Produto sem demanda cadastrada.\n");
            return;
        }

        if (demand.getAmount() <= 0) {
            ConsolePrinter.fail("Nenhuma unidade de %s foi demandada. Atualize a demanda antes de fabricar.\n", chosenProduct.getName());
            return;
        }

        this.getConveyor().turnOn();
        for (Machine m: this.getMachines()) {
            m.turnOn();
        }

        System.out.printf("Eitcha!!! Iniciando produção de: %s\n", chosenProduct.getName());

        int productsRemaining = demand.getAmount();
        int fabricatedAmount = 0;
        int approvedAmount = 0;
        double totalProductionTime = 0;
        boolean orcamentoSuficiente = true;

        Product currentProduct = null;

        while (fabricatedAmount < productsRemaining) {
            System.out.printf("(%d/%d) %s " + "─".repeat(24) + "\n", (fabricatedAmount + 1), productsRemaining, chosenProduct.getName());

            try {
                for (ProductionStages etapaAtual : etapasProducao) {
                    ConsolePrinter.stageHeader("%s", etapaAtual.getNome());
                    Machine currentMachine = this.machines.get(etapaAtual.getCode());

                    if (!calcProductionCost(currentMachine)) {
                        String mensagem = String.format("Dessa vez não é! Orçamento insuficiente para operar a máquina de %s!", currentMachine.getType());
                        throw new InsufficientBudgetException(mensagem);
                    }

                    if (etapaAtual == ProductionStages.PROCESSING) {
                        conveyor.addRawMaterial(chosenProduct.getRawMaterialPerUnit());
                        ConsolePrinter.treeInfo(false, "%s carregado para a esteira.", this.rawMaterial.getName());

                        this.rawMaterial.consume(conveyor.removeRawMaterial());
                        ConsolePrinter.treeInfo(false, "%s transportado até a máquina de %s. E é ligeiro!", this.rawMaterial.getName(), currentMachine.getType());

                        ConsolePrinter.treeInfo(false, "Máquina %s %.2f %s de %s...", etapaAtual.getGerundio(), this.chosenProduct.getRawMaterialPerUnit(), this.rawMaterial.getUnit(), this.rawMaterial.getName());
                        currentProduct = currentMachine.process(this.chosenProduct);

                        ConsolePrinter.treeOk(true, "Tu não acredita! Sabe o que é? \"%s\" #%d criado.", currentProduct.getName(), currentProduct.getId());
                        this.conveyor.addProduct(currentProduct);
                    } else {
                        ConsolePrinter.treeInfo(false, "%s #%d transportado até a máquina de %s. E é ligeiro!", currentProduct.getName(), currentProduct.getId(), currentMachine.getType());

                        ConsolePrinter.treeInfo(false, "Máquina %s o produto %s #%d...", etapaAtual.getGerundio(), currentProduct.getName(), currentProduct.getId());
                        currentProduct = currentMachine.process(this.conveyor.removeProduct());

                        // TODO: Adicionar verbo conjugado no particípio.
                        ConsolePrinter.treeOk(true, "Produto %s #%d passou pela etapa.", currentProduct.getName(), currentProduct.getId());
                        this.conveyor.addProduct(currentProduct);
                    }
                }

                // Validação final
                if (currentProduct != null && currentProduct.getStatus() == ProductStatus.APPROVED) {
                    this.fabricatedProducts.add(this.conveyor.removeProduct());
                    ConsolePrinter.treeOk(true, "Eitcha, como ele tem força! O produto %s #%d" + ConsolePrinter.GREEN + " foi aprovado" + ConsolePrinter.RESET + " e enviado ao armazém!", currentProduct.getName(), currentProduct.getId());
                    approvedAmount++;
                } else if (currentProduct != null) {
                    ConsolePrinter.treeFail(true, "Cade a força? O produto %s #%d" + ConsolePrinter.RED + " foi rejeitado" + ConsolePrinter.RESET + " na inspeção e descartado.", currentProduct.getName(), currentProduct.getId());
                    this.conveyor.removeProduct();
                }

                totalProductionTime += chosenProduct.countProductionTime();
                fabricatedAmount++;

            } catch (MachineNeedsRepairException e) {
                setTemMaquinaQuebrada(true);
                ConsolePrinter.treeFail(true, e.getMessage());
                break;
            } catch (Exception e) {
                ConsolePrinter.treeFail(true, e.getMessage());
                break;
            }
        }

        String infoText = "";

        if (fabricatedAmount == productsRemaining) {
            demand.fulfill(fabricatedAmount, totalProductionTime);
            infoText = String.format(" Demanda de %s concluída em %.2f segundos de produção.", demand.getProductName(), totalProductionTime);
        } else if (fabricatedAmount > 0) {
            demand.partiallyFulfill(fabricatedAmount, totalProductionTime);
            infoText = String.format(" Demanda de %s atendida parcialmente (%d/%d) em %.2f segundos de produção.",
                    demand.getProductName(), fabricatedAmount, productsRemaining, totalProductionTime);
        } else {
            demand.reset();
        }

        ConsolePrinter.card("%d produtos fabricados e %d aprovados." + (infoText.isEmpty() ? "\n" : "\n" + infoText), fabricatedAmount, approvedAmount);

        this.getConveyor().turnOff();
        for (Machine m: this.getMachines()) {
            m.turnOff();
        }
    }

    private boolean calcProductionCost(Machine machine) {
        // for those who just entered the stream: calc is short for _calculate_
        // yellowisk: lol, good one
        if (this.budget >= machine.getOperationCost()) {
            this.budget -= machine.getOperationCost();
            return true;
        }
        return false;
    }

    public void buyRawMaterial(int amount) {
        float totalCost = amount * this.rawMaterial.getPrice();
        if (this.budget < totalCost) {
            ConsolePrinter.fail("Dessa vez não é! Orçamento insuficiente para comprar matéria-prima!\n");
            return;
        }
        budget -= totalCost;
        this.rawMaterial.addStock(amount);

        if (totalCost >= 150) {
            ConsolePrinter.card(" [" + ConsolePrinter.GREEN + "OK" + ConsolePrinter.RESET
                    + "] Dessa vez não é! Cliente comprou 150 reais e, sim, cliente ganhou um balão de presente!");
        } else {
            ConsolePrinter.card(" [" + ConsolePrinter.GREEN + "OK" + ConsolePrinter.RESET + "] Eitcha!!! Compra de matéria-prima realizada com sucesso!");
        }
    }

    public void setChosenProduct(Product chosenProduct) {
        this.chosenProduct = chosenProduct;
    }

    public List<Demand> getDemands() {
        return this.demands;
    }

    public List<Product> getFabricatedProducts() {
        return this.fabricatedProducts;
    }

    public List<Machine> getMachines() {
        return this.machines;
    }

    public Product getChosenProduct() {
        return this.chosenProduct;
    }

    public RawMaterial getRawMaterial() {
        return this.rawMaterial;
    }

    public void addNewConveyor(Conveyor conveyor) {
        this.conveyor = conveyor;
    }

    public Conveyor getConveyor() {
        return this.conveyor;
    }

    public double getBudget() {
        return budget;
    }

    public void displayBudget() {
        ConsolePrinter.printOneLineStats(this.budget, this.rawMaterial.getQuantity(), this.fabricatedProducts.size());
    }

    public void displayStorage() {
        System.out.printf("   Estoque de Matéria-Prima: " + ConsolePrinter.GREEN + "%.2f %s\n\n" + ConsolePrinter.RESET,
                this.rawMaterial.getQuantity(), this.rawMaterial.getUnit());
        ConsolePrinter.listStorage(this.demands, this.fabricatedProducts);
    }

    public boolean temMaquinaQuebrada() {
        return temMaquinaQuebrada;
    }

    public void setTemMaquinaQuebrada(boolean temMaquinaQuebrada) {
        this.temMaquinaQuebrada = temMaquinaQuebrada;
    }

}
