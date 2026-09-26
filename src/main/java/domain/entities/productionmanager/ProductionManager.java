package domain.entities.productionmanager;

import domain.entities.demand.Demand;
import domain.entities.demand.DemandStatus;
import domain.entities.conveyor.Conveyor;
import domain.entities.machine.Machine;
import domain.entities.product.Product;
import domain.entities.product.ProductStatus;
import domain.entities.rawmaterial.RawMaterial;
import domain.exceptions.InsufficientBudgetException;
import domain.exceptions.MachineNeedsRepairException;
import domain.interfaces.ProductionStrategy;
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
    private final RawMaterial rawMaterial;
    private float budget;
    private ProductionStrategy currentStrategy;
    private int batchCounter = 0;
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

    public void setStrategy(ProductionStrategy newStrategy) {
        this.currentStrategy = newStrategy;
    }

    public ProductionStrategy getCurrentStrategy() {
        return this.currentStrategy;
    }

    /**
     * Asks the currrent strategy which demand should be fabricated and does so.
     * Manager never knows the chosen strategy's concrete implementation
     */
    public void runNextProduction() {
        if (this.currentStrategy == null) {
            ConsolePrinter.fail("Nenhuma estratégia de produção selecionada.\n");
            return;
        }

        Demand nextDemand = peekNextDemand();

        if (nextDemand == null) {
            ConsolePrinter.fail("Dessa vez não é! %s não encontrou nenhuma demanda elegível.\n",
                    this.currentStrategy.getStrategyName());
            return;
        }

        System.out.printf("%s escolheu: %s (%d un)\n",
                this.currentStrategy.getStrategyName(), nextDemand.getProductName(), nextDemand.getAmount());
        fabricateDemand(nextDemand);
    }

    /**
     * Shows which demand the current strategy would pick rn, but without fabricating it.
     * Returns null when there's no strategy or no eligible demand.
     */
    public Demand peekNextDemand() {
        if (this.currentStrategy == null) {
            return null;
        }
        // isViable() and getEstimatedCost() read cached cost, so it's gotta be fresh beforehand
        refreshEstimatedCosts();
        return this.currentStrategy.selectDemand(this.demands, this.budget);
    }

    public double getUnitOperationCost() {
        double unitOperationCost = 0;
        for (Machine m : this.machines) {
            unitOperationCost += m.getOperationCost();
        }
        return unitOperationCost;
    }

    public void refreshEstimatedCosts() {
        double unitOperationCost = getUnitOperationCost();
        for (Demand d : this.demands) {
            d.updateEstimatedCost(unitOperationCost);
        }
    }

    /**
     * How much it'd cost to fabricate everything still in line (pending ones).
     * Completed and cancelled ones are outta the projection.
     */
    public double getProjectedCost() {
        refreshEstimatedCosts();
        double total = 0;
        for (Demand d : this.demands) {
            if (d.getStatus().isSelectable()) {
                total += d.getEstimatedCost();
            }
        }
        return total;
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

        if (!demand.getStatus().canBeFabricated()) {
            ConsolePrinter.fail("Demanda de %s está %s e não pode ser fabricada. Atualize a demanda para reativá-la.\n",
                    chosenProduct.getName(), demand.getStatus().getDescription());
            return;
        }

        refreshEstimatedCosts();
        if (!demand.isViable(this.budget)) {
            throw new InsufficientBudgetException(String.format(
                    "Dessa vez não é! Orçamento insuficiente para fabricar %s: custo R$ %.2f, orçamento R$ %.2f.",
                    chosenProduct.getName(), demand.getEstimatedCost(), this.budget));
        }

        int alreadyProduced = demand.getProducedAmount();
        if (demand.getStatus() == DemandStatus.CANCELLED && alreadyProduced > 0) {
            ConsolePrinter.treeInfo(false, "Retomando demanda de %s de onde parou (%d/%d).",
                    chosenProduct.getName(), alreadyProduced, demand.getAmount());
        }

        demand.startProduction();
        int batch = ++this.batchCounter;

        this.getConveyor().turnOn();
        for (Machine m: this.getMachines()) {
            m.turnOn();
        }

        System.out.printf("Eitcha!!! Iniciando produção de: %s (lote %d)\n", chosenProduct.getName(), batch);
        ConsolePrinter.pause(1500);

        int productsRemaining = demand.getRemainingAmount();
        int fabricatedAmount = 0;
        int approvedAmount = 0;
        double totalProductionTime = 0;

        Product currentProduct = null;

        while (fabricatedAmount < productsRemaining) {
            System.out.printf("(%d/%d) %s " + "─".repeat(24) + "\n", (alreadyProduced + fabricatedAmount + 1), demand.getAmount(), chosenProduct.getName());

            try {
                for (ProductionStages etapaAtual : etapasProducao) {
                    Machine currentMachine = this.machines.get(etapaAtual.getCode());

                    ConsolePrinter.stageHeader(currentMachine, "%s", etapaAtual.getNome());

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
                        currentProduct.setBatch(batch);

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

        String infoText;

        if (fabricatedAmount == productsRemaining) {
            demand.fulfill(fabricatedAmount, totalProductionTime);
            infoText = String.format(" Demanda de %s concluída em %.2f segundos de produção.",
                    demand.getProductName(), demand.getTotalProductionTime());
        } else {
            demand.cancel(fabricatedAmount, totalProductionTime);
            infoText = String.format(" Demanda de %s cancelada (%d/%d). Fabrique de novo para continuar de onde parou.",
                    demand.getProductName(), demand.getProducedAmount(), demand.getAmount());
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
            this.budget -= (float) machine.getOperationCost();
            return true;
        }
        return false;
    }

    /**
     * Buys raw material and takes it off the budget.
     *
     * @return how much the purchase cost in total
     * @throws InsufficientBudgetException when the budget can't cover it
     */
    public float buyRawMaterial(int amount) {
        float totalCost = amount * this.rawMaterial.getPrice();
        if (this.budget < totalCost) {
            throw new InsufficientBudgetException("Dessa vez não é! Orçamento insuficiente para comprar matéria-prima!");
        }
        budget -= totalCost;
        this.rawMaterial.addStock(amount);
        return totalCost;
    }

    // === REPAIR ===

    /**
     * Função que repara a máquina escolhida.
     * @param m A máquina a ser reparada
     */
    public void repairMachine(Machine m) {
        // TODO: personalizar log
        if (m == null) {
            throw new IllegalArgumentException("Máquina nula");
        }

        if (!m.needsMaintenance()) {
            throw new IllegalStateException("A " + m.getName() + " não precisa de reparo");
        }
        m.reparar();
        setTemMaquinaQuebrada(false);
    }

    // === GETTERS and SETTERS

    public List<Demand> getDemands() {
        return this.demands;
    }

    public List<Product> getFabricatedProducts() {
        return this.fabricatedProducts;
    }

    public List<Machine> getMachines() {
        return this.machines;
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

    public float getBudget() {
        return budget;
    }

    public boolean temMaquinaQuebrada() {
        return temMaquinaQuebrada;
    }

    public void setTemMaquinaQuebrada(boolean temMaquinaQuebrada) {
        this.temMaquinaQuebrada = temMaquinaQuebrada;
    }
}
