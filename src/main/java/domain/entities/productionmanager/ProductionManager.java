package domain.entities.productionmanager;

import domain.entities.demand.Demand;
import domain.entities.demand.DemandStatus;
import domain.entities.conveyor.Conveyor;
import domain.entities.machine.Machine;
import domain.entities.product.Produto;
import domain.entities.product.ProductStatus;
import domain.entities.rawmaterial.RawMaterial;
import domain.entities.report.Report;
import domain.exceptions.InsufficientBudgetException;
import domain.exceptions.MachineNeedsRepairException;
import domain.exceptions.NoStrategySelectedException;
import domain.exceptions.NullDemandException;
import domain.interfaces.ProductionStrategy;
import view.ConsolePrinter;

import java.util.ArrayList;
import java.util.List;

/**
 * Essa classe é o administrador da produção. Ele é
 * responsável por guardar a lista de máquinas, verba atual,
 * lista de demandas, produtos produzidos, matéria-prima, etc.
 */
public class ProductionManager {
    /// ArrayList de demandas. Haverá uma demanda por produtosDisponiveisFabricacao
    private List<Demand> demandas = new ArrayList<Demand>();
    /// ArrayList de produtos fabricados com sucesso e com necessidade de reparo
    private List<Produto> produtosFabricados = new ArrayList<Produto>();
    /// Arraylist de produtos que servirão de moldes/exemplo para o menu e para a produção nas máquinas
    private List<Produto> produtosDisponiveisFabricacao = new ArrayList<Produto>();
    /// ArrayList de máquinas existentes na fábrica
    private List<Machine> maquinas = new ArrayList<Machine>();
    /// esteira da fábrica
    private Conveyor esteira;
    /// produto selecionado atualmente
    private Produto currProduto;
    /// matéria-prima da fábrica
    private final RawMaterial materiaPrima;
    /// verba atual da fábrica
    private float verba;
    /// estratégia selecionada atualmente
    private ProductionStrategy currEstrategia;
    /// número de lotes produzidos
    private int loteCount = 0;
    /// booleano para se há máquina quebrada ou não. Essa flag impede a produção de
    /// continuar caso true
    private boolean temMaquinaQuebrada = false;
    /// objeto da classe Relatorio para salvar dados para log
    private Report relatorio;
    /// lista das etapas de produção instanciada para evitar laço 'for' otimizado direto no Enum.
    private static final ProductionStages[] etapasProducao = ProductionStages.values();

    public ProductionManager(RawMaterial rawMaterial, float budget, Report rp) {
        this.materiaPrima = rawMaterial;
        this.verba = budget;
        this.relatorio = rp;
    }
    
    /**
     * Adicina novo produto na lista de produtos.
     *
     * @param p o produto a ser adicionado
     */
    public void adicionarNovoProduto(Produto p) {
        this.produtosDisponiveisFabricacao.add(p);
        registrarDemanda(p, 0); // inicializa a demanda zerada
    }

    /* === DEMANDAS === */

    /**
     * Adiciona uma nova demanda na lista de demandas
     * @param p o produto associado à demanda
     * @param num a quantidade de produtos solicitados
     */
    public void registrarDemanda(Produto p, int num) {
        this.demandas.add(new Demand(p, num));
    }

    /**
     * Atualiza a demanda fornecida com uma nova quantidade de
     * produtos
     * @param d a demanda a ser alterada
     * @param num a nova quantidade
     */
    public void atualizarDemanda(Demand d, int num) {
        Produto p = getProdutoPeloNome(d.getProductName());
        d.atualizarQuantidade(p, num);
    }

    /**
     * Retorna o produto da lista de produtos disponíveis a partir
     * do nome fornecido
     * @param nome
     * @return Produto
     */
    public Produto getProdutoPeloNome(String nome) {
        for (Produto p: produtosDisponiveisFabricacao) {
            if (p.getName().equalsIgnoreCase(nome)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Adiciona uma máquina na lista de máquinas
     * @param m a máquina a ser adicionada
     */
    public void adicionarMaquina(Machine m) {
        this.maquinas.add(m);
    }
    
    public void setCurrEstrategia(ProductionStrategy s) {
        this.currEstrategia = s;
    }
    
    public ProductionStrategy getCurrEstrategia() {
        return this.currEstrategia;
    }

    /**
     * Inicia a produção da próxima demanda. A demanda é escolhida pela estratégia atual.
     */
    public void iniciarProducao() {
        if (this.currEstrategia == null) {
            throw new NoStrategySelectedException("Nenhuma estratégia de produção selecionada.\n");
        }

        Demand nextDemanda = espiarProximaDemanda();

        if (nextDemanda == null) {
            String buffer = String.format("Dessa vez não é! %s não encontrou nenhuma demanda elegível.\n",
                    this.currEstrategia.getStrategyName());
            throw new NullDemandException(buffer);
        }

        System.out.printf("%s escolheu: %s (%d un)\n",
                this.currEstrategia.getStrategyName(), nextDemanda.getProductName(), nextDemanda.getAmount());
        produzirDemanda(nextDemanda);
    }

    /**
     * Retorna a demanda que a estratégia escolheria, sem fabricá-la
     * @return próxima demanda
     */
    public Demand espiarProximaDemanda() {
        if (this.currEstrategia == null) {
            return null;
        }
        // isViable() and getEstimatedCost() read cached cost, so it's gotta be fresh beforehand
        atualizarCustoEstimadoTodasDemandas();
        return this.currEstrategia.escolherDemanda(this.demandas, this.verba);
    }

    /**
     * Simula o custo total de um produto passando por todas as máquinas da fábrica
     * @return custo total de operação
     */
    public double calcCustoTotalOperacao() {
        double sum = 0;
        for (Machine m : this.maquinas) {
            sum += m.getOperationCost();
        }
        return sum;
    }

    /**
     * Atualiza o custo estimado de todas as demandas
     */
    public void atualizarCustoEstimadoTodasDemandas() {
        double unitOperationCost = calcCustoTotalOperacao();
        for (Demand d : this.demandas) {
            d.atualizarCustoEstimado(unitOperationCost);
        }
    }

    /**
     * How much it'd cost to fabricate everything still in line (pending ones).
     * Completed and cancelled ones are outta the projection.
     */
    public double getProjectedCost() {
        atualizarCustoEstimadoTodasDemandas();
        double total = 0;
        for (Demand d : this.demandas) {
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
    public void produzirDemanda(Demand demand) {
        // VERIFICAÇÕES
        if (temMaquinaQuebrada()) {
            throw new MachineNeedsRepairException("Não podemos iniciar a fabricação. Há máquinas precisando de reparo.");
        }

        this.currProduto = this.getProdutoPeloNome(demand.getProductName());

        if (currProduto == null) {
            ConsolePrinter.fail("Produto sem demanda cadastrada.\n");
            return;
        }

        if (demand.getAmount() <= 0) {
            ConsolePrinter.fail("Nenhuma unidade de %s foi demandada. Atualize a demanda antes de fabricar.\n", currProduto.getName());
            return;
        }

        if (!demand.getStatus().canBeFabricated()) {
            ConsolePrinter.fail("Demanda de %s está %s e não pode ser fabricada. Atualize a demanda para reativá-la.\n",
                    currProduto.getName(), demand.getStatus().getDescription());
            return;
        }

        atualizarCustoEstimadoTodasDemandas();
        if (!demand.isViable(this.verba)) {
            throw new InsufficientBudgetException(String.format(
                    "Dessa vez não é! Orçamento insuficiente para fabricar %s: custo R$ %.2f, orçamento R$ %.2f.",
                    currProduto.getName(), demand.getEstimatedCost(), this.verba));
        }

        int alreadyProduced = demand.getProducedAmount();
        if (demand.getStatus() == DemandStatus.CANCELLED && alreadyProduced > 0) {
            ConsolePrinter.treeInfo(false, "Retomando demanda de %s de onde parou (%d/%d).",
                    currProduto.getName(), alreadyProduced, demand.getAmount());
        }


        demand.startProduction();
        int batch = ++this.loteCount;

        this.getEsteira().turnOn();
        for (Machine m: this.getMaquinas()) {
            m.turnOn();
        }

        System.out.printf("Eitcha!!! Iniciando produção de: %s (lote %d)\n", currProduto.getName(), batch);
        ConsolePrinter.productionPause(1500);

        int productsRemaining = demand.getRemainingAmount();
        int sectionFabricatedAmount = 0;

        // Variáveis de auditoria
        int sectionApprovedAmount = 0;
        int sectionNeedsMaintenanceAmount = 0;
        int sectionRejectedAmount = 0;
        double sectionProductionTime = 0;
        double sectionMoneySpent = 0;

        Produto currentProduct = null;

        while (sectionFabricatedAmount < productsRemaining) {
            System.out.printf("(%d/%d) %s " + "─".repeat(24) + "\n", (alreadyProduced + sectionFabricatedAmount + 1), demand.getAmount(), currProduto.getName());

            try {
                for (ProductionStages etapaAtual : etapasProducao) {
                    Machine currentMachine = this.maquinas.get(etapaAtual.getCode());

                    ConsolePrinter.stageHeader(currentMachine, "%s", etapaAtual.getNome());

                    if (!calcProductionCost(currentMachine)) {
                        String mensagem = String.format("Dessa vez não é! Orçamento insuficiente para operar a máquina de %s!", currentMachine.getType());
                        throw new InsufficientBudgetException(mensagem);
                    }
                    sectionMoneySpent += currentMachine.getOperationCost(); // Auditoria

                    if (etapaAtual == ProductionStages.PROCESSING) {
                        esteira.addRawMaterial(currProduto.getRawMaterialPerUnit());
                        relatorio.increaseRawMaterialSpent(currProduto.getRawMaterialPerUnit()); // Auditoria
                        ConsolePrinter.treeInfo(false, "%s carregado para a esteira.", this.materiaPrima.getName());

                        this.materiaPrima.consume(esteira.removeRawMaterial());
                        ConsolePrinter.treeInfo(false, "%s transportado até a máquina de %s. E é ligeiro!", this.materiaPrima.getName(), currentMachine.getType());

                        ConsolePrinter.treeInfo(false, "Máquina %s %.2f %s de %s...", etapaAtual.getGerundio(), this.currProduto.getRawMaterialPerUnit(), this.materiaPrima.getUnit(), this.materiaPrima.getName());
                        currentProduct = currentMachine.process(this.currProduto);
                        currentProduct.setBatch(batch);

                        ConsolePrinter.treeOk(true, "Tu não acredita! Sabe o que é? \"%s\" #%d criado.", currentProduct.getName(), currentProduct.getId());
                        this.esteira.addProduct(currentProduct);
                    } else {
                        ConsolePrinter.treeInfo(false, "%s #%d transportado até a máquina de %s. E é ligeiro!", currentProduct.getName(), currentProduct.getId(), currentMachine.getType());

                        ConsolePrinter.treeInfo(false, "Máquina %s o produto %s #%d...", etapaAtual.getGerundio(), currentProduct.getName(), currentProduct.getId());
                        currentProduct = currentMachine.process(this.esteira.removeProduct());

                        ConsolePrinter.treeOk(true, "Produto %s #%d foi %s com sucesso.", currentProduct.getName(), currentProduct.getId(), etapaAtual.getVerboParticipio());
                        this.esteira.addProduct(currentProduct);
                    }
                }

                // Validação final
                if (currentProduct.getStatus() == ProductStatus.APPROVED) {
                    this.produtosFabricados.add(this.esteira.removeProduct());
                    ConsolePrinter.treeOk(true, "Eitcha, como ele tem força! O produto %s #%d" + ConsolePrinter.GREEN + " foi aprovado" + ConsolePrinter.RESET + " e enviado ao armazém!", currentProduct.getName(), currentProduct.getId());
                    sectionApprovedAmount++;
                    relatorio.increaseTotalApprovedProducts();
                } else if (currentProduct.getStatus() == ProductStatus.TO_REPAIR) {
                    this.produtosFabricados.add(this.esteira.removeProduct());
                    ConsolePrinter.treeOk(true, "Cade a força? O produto %s #%d" + ConsolePrinter.YELLOW + " foi parcialmente aprovado" + ConsolePrinter.RESET + " e enviado ao armazém!", currentProduct.getName(), currentProduct.getId());
                    sectionNeedsMaintenanceAmount++;
                    relatorio.increaseTotalNeedsMaintenanceProducts();
                } else {
                    this.esteira.removeProduct();
                    ConsolePrinter.treeFail(true, "Cade a força? O produto %s #%d" + ConsolePrinter.RED + " foi rejeitado" + ConsolePrinter.RESET + " na inspeção e descartado.", currentProduct.getName(), currentProduct.getId());
                    relatorio.increaseTotalRejectedProducts();
                    sectionRejectedAmount++;
                }

                sectionProductionTime += currProduto.countProductionTime();
                sectionFabricatedAmount++;

            } catch (MachineNeedsRepairException e) {
                setTemMaquinaQuebrada(true);
                ConsolePrinter.treeFail(true, e.getMessage());
                break;
            } catch (Exception e) {
                ConsolePrinter.treeFail(true, e.getMessage());
                break;
            }
        }

        relatorio.increaseMoneySpent(sectionMoneySpent);
        relatorio.increaseTotalProductionTime(sectionProductionTime);
        relatorio.calcProdSuccessRate();

        String infoText;

        if (sectionFabricatedAmount == productsRemaining) {
            demand.fulfill(sectionFabricatedAmount, sectionProductionTime);
            infoText = String.format(" Demanda de %s concluída em %.2f segundos de produção.",
                    demand.getProductName(), demand.getTotalProductionTime());
        } else {
            demand.cancel(sectionFabricatedAmount, sectionProductionTime);
            infoText = String.format(" Demanda de %s cancelada (%d/%d). Fabrique de novo para continuar de onde parou.",
                    demand.getProductName(), demand.getProducedAmount(), demand.getAmount());
        }

        ConsolePrinter.card("%d produtos fabricados: %d aprovados, %d precisam de manutenção e %d rejeitados." + "\n" + infoText,
                sectionFabricatedAmount, sectionApprovedAmount,
                sectionNeedsMaintenanceAmount, sectionRejectedAmount);

        // Desliga as máquinas
        this.getEsteira().turnOff();
        for (Machine m: this.getMaquinas()) {
            m.turnOff();
        }
    }

    /**
     * Desconta o valor da produção
     * @param machine
     * @return
     */
    private boolean calcProductionCost(Machine machine) {
        // for those who just entered the stream: calc is short for _calculate_
        // yellowisk: lol, good one
        if (this.verba >= machine.getOperationCost()) {
            this.verba -= (float) machine.getOperationCost();
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
        float totalCost = amount * this.materiaPrima.getPrice();
        if (this.verba < totalCost) {
            throw new InsufficientBudgetException("Dessa vez não é! Orçamento insuficiente para comprar matéria-prima!");
        }
        verba -= totalCost;
        this.materiaPrima.addStock(amount);
        return totalCost;
    }

    // === REPAIR ===

    /**
     * Função que repara a máquina escolhida.
     * @param m A máquina a ser reparada
     */
    public void repairMachine(Machine m) {
        if (!(m.getSaude() < m.getSaudeMaxima())) {
            throw new IllegalStateException("Não foi dessa vez! A " + m.getName() + " não precisa de reparo");
        }
        m.reparar();
        setTemMaquinaQuebrada(false);
    }

    // === GETTERS and SETTERS

    public List<Demand> getDemandas() {
        return this.demandas;
    }

    public List<Produto> getProdutosFabricados() {
        return this.produtosFabricados;
    }

    public List<Machine> getMaquinas() {
        return this.maquinas;
    }

    public Report getRelatorio() {
        return this.relatorio;
    }

    public RawMaterial getMateriaPrima() {
        return this.materiaPrima;
    }

    public void addNewConveyor(Conveyor conveyor) {
        this.esteira = conveyor;
    }

    public Conveyor getEsteira() {
        return this.esteira;
    }

    public float getVerba() {
        return verba;
    }

    public boolean temMaquinaQuebrada() {
        return temMaquinaQuebrada;
    }

    public void setTemMaquinaQuebrada(boolean temMaquinaQuebrada) {
        this.temMaquinaQuebrada = temMaquinaQuebrada;
    }
}
