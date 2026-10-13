package domain.entities.productionmanager;

import domain.entities.demand.Demanda;
import domain.entities.demand.StatusDemanda;
import domain.entities.conveyor.Esteira;
import domain.entities.machine.Machine;
import domain.entities.product.Produto;
import domain.entities.product.ProductStatus;
import domain.entities.rawmaterial.MateriaPrima;
import domain.entities.report.Relatorio;
import domain.exceptions.InsufficientBudgetException;
import domain.exceptions.InsufficientRawMaterialException;
import domain.exceptions.InvalidDemandException;
import domain.exceptions.InvalidDemandTransitionException;
import domain.exceptions.MachineNeedsRepairException;
import domain.exceptions.MachineUnavailableException;
import domain.exceptions.NoStrategySelectedException;
import domain.exceptions.NullDemandException;
import domain.exceptions.PlantaException;
import domain.interfaces.ProductionStrategy;
import view.ConsolePrinter;

import java.util.ArrayList;
import java.util.List;

/**
 * Essa classe é o administrador da produção. Ele é
 * responsável por guardar a lista de máquinas, verba atual,
 * lista de demandas, produtos produzidos, matéria-prima, etc.
 *
 * @author Heitor Almeida
 * @author Glayson Oliveira
 */
public class ProductionManager {
    /// ArrayList de demandas. Haverá uma demanda por produtosDisponiveisFabricacao
    private List<Demanda> demandas = new ArrayList<Demanda>();
    /// ArrayList de produtos fabricados com sucesso e com necessidade de reparo
    private List<Produto> produtosFabricados = new ArrayList<Produto>();
    /// Arraylist de produtos que servirão de moldes/exemplo para o menu e para a produção nas máquinas
    private List<Produto> produtosDisponiveisFabricacao = new ArrayList<Produto>();
    /// ArrayList de máquinas existentes na fábrica
    private List<Machine> maquinas = new ArrayList<Machine>();
    /// esteira da fábrica
    private Esteira esteira;
    /// produto selecionado atualmente
    private Produto currProduto;
    /// matéria-prima da fábrica
    private final MateriaPrima materiaPrima;
    /// verba atual da fábrica
    private float verba;
    /// estratégia selecionada atualmente
    private ProductionStrategy currEstrategia;
    /// número de lotes produzidos
    private int loteCount = 0;
    /// objeto da classe Relatorio para salvar dados para log
    private Relatorio relatorio;
    /// lista das etapas de produção instanciada para evitar laço 'for' otimizado direto no Enum.
    private static final EtapaProducao[] etapasProducao = EtapaProducao.values();

    /**
     * Cria o gerenciador sem máquinas, produtos, esteira nem estratégia, que são adicionados depois pelo menu
     *
     * @param rawMaterial matéria-prima da fábrica
     * @param budget verba inicial, em R$, que vem do cenário escolhido
     * @param rp relatório onde a auditoria vai acumulando os dados da produção
     */
    public ProductionManager(MateriaPrima rawMaterial, float budget, Relatorio rp) {
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
        this.demandas.add(new Demanda(p, num));
    }

    /**
     * Atualiza a demanda fornecida com uma nova quantidade de
     * produtos
     * @param d a demanda a ser alterada
     * @param num a nova quantidade
     */
    public void atualizarDemanda(Demanda d, int num) {
        Produto p = getProdutoPeloNome(d.getNomeProduto());
        d.atualizarQuantidade(p, num);
    }

    /**
     * Retorna o produto da lista de produtos disponíveis a partir
     * do nome fornecido
     * @param nome nome do produto, ex.: "Copo"
     * @return o modelo do catálogo, ou null se nenhum produto tiver esse nome
     */
    public Produto getProdutoPeloNome(String nome) {
        for (Produto p: produtosDisponiveisFabricacao) {
            if (p.getNome().equalsIgnoreCase(nome)) {
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
    
    /**
     * Troca a estratégia que escolhe a próxima demanda
     *
     * @param s nova estratégia
     */
    public void setCurrEstrategia(ProductionStrategy s) {
        this.currEstrategia = s;
    }
    
    /**
     * Estratégia que escolhe a próxima demanda
     *
     * @return estratégia atual, ou null se nenhuma tiver sido escolhida
     */
    public ProductionStrategy getCurrEstrategia() {
        return this.currEstrategia;
    }

    /**
     * Inicia a produção da próxima demanda. A demanda é escolhida pela estratégia atual.
     *
     * @throws NoStrategySelectedException se nenhuma estratégia tiver sido escolhida
     * @throws NullDemandException se a estratégia não encontrar nenhuma demanda elegível
     * @throws domain.exceptions.PlantaException qualquer erro de {@link #produzirDemanda(Demanda)}
     */
    public void iniciarProducao() {
        if (this.currEstrategia == null) {
            throw new NoStrategySelectedException("Nenhuma estratégia de produção selecionada.\n");
        }

        Demanda nextDemanda = espiarProximaDemanda();

        if (nextDemanda == null) {
            String buffer = String.format("Dessa vez não é! %s não encontrou nenhuma demanda elegível.\n",
                    this.currEstrategia.getNomeEstrategia());
            throw new NullDemandException(buffer);
        }

        System.out.printf("%s escolheu: %s (%d un)\n",
                this.currEstrategia.getNomeEstrategia(), nextDemanda.getNomeProduto(), nextDemanda.getQuantidade());
        produzirDemanda(nextDemanda);
    }

    /**
     * Retorna a demanda que a estratégia escolheria, sem fabricá-la
     * @return próxima demanda
     */
    public Demanda espiarProximaDemanda() {
        if (this.currEstrategia == null) {
            return null;
        }
        // ehViavel() and getCustoEstimado() read cached cost, so it's gotta be fresh beforehand
        atualizarCustoEstimadoTodasDemandas();
        return this.currEstrategia.selecionarDemanda(this.demandas, this.verba);
    }

    /**
     * Simula o custo total de um produto passando por todas as máquinas da fábrica
     * @return custo total de operação
     */
    public double calcCustoTotalOperacao() {
        double sum = 0;
        for (Machine m : this.maquinas) {
            sum += m.getCustoOperacao();
        }
        return sum;
    }

    /**
     * Atualiza o custo estimado de todas as demandas
     */
    public void atualizarCustoEstimadoTodasDemandas() {
        double unitOperationCost = calcCustoTotalOperacao();
        for (Demanda d : this.demandas) {
            d.atualizarCustoEstimado(unitOperationCost);
        }
    }

    /**
     * Quanto custaria fabricar todas as demandas pendentes. As concluídas e canceladas ficam fora da projeção.
     * Também atualiza o custo estimado de cada demanda
     *
     * @return custo total projetado, em R$
     */
    public double getProjectedCost() {
        atualizarCustoEstimadoTodasDemandas();
        double total = 0;
        for (Demanda d : this.demandas) {
            if (d.getStatus().ehSelecionavel()) {
                total += d.getCustoEstimado();
            }
        }
        return total;
    }

    /**
     * Método principal da classe.
     * Realiza o processo de fabricação da demanda escolhida.
     *
     * Todas as verificações rodam antes de mexer em qualquer estado, então se uma falhar
     * nada é alterado (estoque, verba e status da demanda continuam iguais).
     *
     * @param demand A demanda que será fabricada.
     * @throws MachineNeedsRepairException se alguma máquina precisar de reparo
     * @throws InvalidDemandException se o produto da demanda não existir ou se ela não tiver unidades pedidas
     * @throws InvalidDemandTransitionException se o status da demanda não permitir fabricar
     * @throws InsufficientBudgetException se o orçamento não cobrir o custo estimado
     */
    public void produzirDemanda(Demanda demand) {
        // VERIFICAÇÕES
        if (temMaquinaQuebrada()) {
            throw new MachineNeedsRepairException("Não podemos iniciar a fabricação. Há máquinas precisando de reparo.");
        }

        this.currProduto = this.getProdutoPeloNome(demand.getNomeProduto());

        if (currProduto == null) {
            throw new InvalidDemandException("Dessa vez não é! Produto sem demanda cadastrada.");
        }

        if (demand.getQuantidade() <= 0) {
            throw new InvalidDemandException(String.format(
                    "Dessa vez não é! Nenhuma unidade de %s foi demandada. Atualize a demanda antes de fabricar.",
                    currProduto.getNome()));
        }

        if (!demand.getStatus().podeSerFabricada()) {
            throw new InvalidDemandTransitionException(String.format(
                    "Dessa vez não é! Demanda de %s está %s e não pode ser fabricada. Atualize a demanda para reativá-la.",
                    currProduto.getNome(), demand.getStatus().getDescricao()));
        }

        atualizarCustoEstimadoTodasDemandas();
        if (!demand.ehViavel(this.verba)) {
            throw new InsufficientBudgetException(String.format(
                    "Dessa vez não é! Orçamento insuficiente para fabricar %s: custo R$ %.2f, orçamento R$ %.2f.",
                    currProduto.getNome(), demand.getCustoEstimado(), this.verba));
        }

        int alreadyProduced = demand.getQuantidadeProduzida();
        if (demand.getStatus() == StatusDemanda.CANCELADA && alreadyProduced > 0) {
            ConsolePrinter.arvoreInfo(false, "Retomando demanda de %s de onde parou (%d/%d).",
                    currProduto.getNome(), alreadyProduced, demand.getQuantidade());
        }


        demand.iniciarProducao();
        int batch = ++this.loteCount;

        this.getEsteira().ligar();
        for (Machine m: this.getMaquinas()) {
            m.ligar();
        }

        System.out.printf("Eitcha!!! Iniciando produção de: %s (lote %d)\n", currProduto.getNome(), batch);
        ConsolePrinter.pausarProducao(1500);

        int productsRemaining = demand.getQuantidadeRestante();
        int sectionFabricatedAmount = 0;

        // Variáveis de auditoria
        int sectionApprovedAmount = 0;
        int sectionNeedsMaintenanceAmount = 0;
        int sectionRejectedAmount = 0;
        double sectionProductionTime = 0;
        double sectionMoneySpent = 0;

        Produto currentProduct = null;

        while (sectionFabricatedAmount < productsRemaining) {
            System.out.printf("(%d/%d) %s " + "─".repeat(24) + "\n", (alreadyProduced + sectionFabricatedAmount + 1), demand.getQuantidade(), currProduto.getNome());

            try {
                for (EtapaProducao etapaAtual : etapasProducao) {
                    Machine currentMachine = this.maquinas.get(etapaAtual.getCodigo());

                    ConsolePrinter.cabecalhoEtapa(currentMachine, "%s", etapaAtual.getNome());

                    // Confere tudo antes de cobrar a etapa, p/ não pagar por uma operação que não vai acontecer
                    currentMachine.verificarDisponivel();
                    double materiaPrimaPorUnidade = currProduto.getQuantidadeMateriaPrimaPorUnidade();
                    if (etapaAtual == EtapaProducao.PROCESSAMENTO && !this.materiaPrima.estaDisponivel(materiaPrimaPorUnidade)) {
                        throw new InsufficientRawMaterialException(String.format(
                                "Cadê a força? Estoque insuficiente para fabricar mais um %s: precisa de %.2f %s. "
                                        + "Compre matéria-prima e fabrique de novo para continuar de onde parou.",
                                currProduto.getNome(), materiaPrimaPorUnidade, this.materiaPrima.getUnidade()));
                    }

                    if (!calcProductionCost(currentMachine)) {
                        String mensagem = String.format("Dessa vez não é! Orçamento insuficiente para operar a máquina de %s!", currentMachine.getTipo());
                        throw new InsufficientBudgetException(mensagem);
                    }
                    sectionMoneySpent += currentMachine.getCustoOperacao(); // Auditoria

                    if (etapaAtual == EtapaProducao.PROCESSAMENTO) {
                        esteira.adicionarMateriaPrima(materiaPrimaPorUnidade);
                        ConsolePrinter.arvoreInfo(false, "%s carregado para a esteira.", this.materiaPrima.getNome());

                        this.materiaPrima.consumir(esteira.removerMateriaPrima());
                        relatorio.aumentarMateriaPrimaGasta(materiaPrimaPorUnidade); // Auditoria, só depois do consumo
                        ConsolePrinter.arvoreInfo(false, "%s transportado até a máquina de %s. E é ligeiro!", this.materiaPrima.getNome(), currentMachine.getTipo());

                        ConsolePrinter.arvoreInfo(false, "Máquina %s %.2f %s de %s...", etapaAtual.getGerundio(), this.currProduto.getQuantidadeMateriaPrimaPorUnidade(), this.materiaPrima.getUnidade(), this.materiaPrima.getNome());
                        currentProduct = currentMachine.processar(this.currProduto);
                        currentProduct.setLote(batch);

                        ConsolePrinter.arvoreOk(true, "Tu não acredita! Sabe o que é? \"%s\" #%d criado.", currentProduct.getNome(), currentProduct.getId());
                        this.esteira.adicionarProduto(currentProduct);
                    } else {
                        ConsolePrinter.arvoreInfo(false, "%s #%d transportado até a máquina de %s. E é ligeiro!", currentProduct.getNome(), currentProduct.getId(), currentMachine.getTipo());

                        ConsolePrinter.arvoreInfo(false, "Máquina %s o produto %s #%d...", etapaAtual.getGerundio(), currentProduct.getNome(), currentProduct.getId());
                        currentProduct = currentMachine.processar(this.esteira.removerProduto());

                        ConsolePrinter.arvoreOk(true, "Produto %s #%d foi %s com sucesso.", currentProduct.getNome(), currentProduct.getId(), etapaAtual.getVerboParticipio());
                        this.esteira.adicionarProduto(currentProduct);
                    }
                }

                // Validação final
                if (currentProduct.getStatus() == ProductStatus.APPROVED) {
                    this.produtosFabricados.add(this.esteira.removerProduto());
                    ConsolePrinter.arvoreOk(true, "Eitcha, como ele tem força! O produto %s #%d" + ConsolePrinter.VERDE + " foi aprovado" + ConsolePrinter.RESET + " e enviado ao armazém!", currentProduct.getNome(), currentProduct.getId());
                    sectionApprovedAmount++;
                    relatorio.aumentarTotalProdutosAprovados();
                } else if (currentProduct.getStatus() == ProductStatus.TO_REPAIR) {
                    this.produtosFabricados.add(this.esteira.removerProduto());
                    ConsolePrinter.arvoreOk(true, "Cade a força? O produto %s #%d" + ConsolePrinter.AMARELO + " foi parcialmente aprovado" + ConsolePrinter.RESET + " e enviado ao armazém!", currentProduct.getNome(), currentProduct.getId());
                    sectionNeedsMaintenanceAmount++;
                    relatorio.aumentarTotalProdutosPrecisamManutencao();
                } else {
                    this.esteira.removerProduto();
                    ConsolePrinter.arvoreFalha(true, "Cade a força? O produto %s #%d" + ConsolePrinter.VERMELHO + " foi rejeitado" + ConsolePrinter.RESET + " na inspeção e descartado.", currentProduct.getNome(), currentProduct.getId());
                    relatorio.aumentarTotalProdutosRejeitados();
                    sectionRejectedAmount++;
                }

                sectionProductionTime += currProduto.calcularTempoProducao();
                sectionFabricatedAmount++;

            } catch (PlantaException e) {
                // Máquina quebrou ou orçamento/matéria-prima acabaram no meio do lote: a demanda é cancelada c/ o progresso salvo
                ConsolePrinter.arvoreFalha(true, e.getMessage());
                descartarUnidadeIncompleta();
                break;
            }
        }

        relatorio.aumentarDinheiroGasto(sectionMoneySpent);
        relatorio.aumentarTempoTotalProducao(sectionProductionTime);
        relatorio.calcularTaxaSucesso();

        String infoText;

        if (sectionFabricatedAmount == productsRemaining) {
            demand.concluir(sectionFabricatedAmount, sectionProductionTime);
            infoText = String.format(" Demanda de %s concluída em %.2f segundos de produção.",
                    demand.getNomeProduto(), demand.getTempoTotalProducao());
        } else {
            demand.cancelar(sectionFabricatedAmount, sectionProductionTime);
            infoText = String.format(" Demanda de %s cancelada (%d/%d). Fabrique de novo para continuar de onde parou.",
                    demand.getNomeProduto(), demand.getQuantidadeProduzida(), demand.getQuantidade());
        }

        ConsolePrinter.cartao("%d produtos fabricados: %d aprovados, %d precisam de manutenção e %d rejeitados." + "\n" + infoText,
                sectionFabricatedAmount, sectionApprovedAmount,
                sectionNeedsMaintenanceAmount, sectionRejectedAmount);

        // Desliga as máquinas
        this.getEsteira().desligar();
        for (Machine m: this.getMaquinas()) {
            m.desligar();
        }
    }

    /**
     * Tira da esteira a unidade que parou no meio da linha (ex.: a máquina de empacotamento quebrou c/ ela
     * esperando), p/ a próxima produção começar c/ a esteira vazia. A matéria-prima e o custo das etapas que ela
     * já passou continuam contados, porque foram gastos de verdade
     */
    private void descartarUnidadeIncompleta() {
        if (this.esteira.getProduto() != null) {
            Produto incompleto = this.esteira.removerProduto();
            ConsolePrinter.arvoreInfo(true, "%s #%d ficou incompleto e foi descartado.", incompleto.getNome(), incompleto.getId());
        }
    }

    /**
     * Desconta da verba o custo de operar a máquina numa etapa, se a verba cobrir
     * @param machine máquina da etapa
     * @return true se o custo foi descontado, false se a verba não cobre (e nada é descontado)
     */
    private boolean calcProductionCost(Machine machine) {
        // for those who just entered the stream: calc is short for _calculate_
        // yellowisk: lol, good one
        if (this.verba >= machine.getCustoOperacao()) {
            this.verba -= (float) machine.getCustoOperacao();
            return true;
        }
        return false;
    }

    /**
     * Compra matéria-prima e desconta o valor da verba. Se a verba não cobrir, nada é alterado
     *
     * @param amount quantidade a comprar, na unidade da matéria-prima
     * @return quanto a compra custou no total, em R$
     * @throws InsufficientBudgetException se a verba não cobrir a compra
     */
    public float buyRawMaterial(int amount) {
        float totalCost = amount * this.materiaPrima.getPreco();
        if (this.verba < totalCost) {
            throw new InsufficientBudgetException("Dessa vez não é! Orçamento insuficiente para comprar matéria-prima!");
        }
        verba -= totalCost;
        this.materiaPrima.adicionarEstoque(amount);
        return totalCost;
    }

    // === REPAIR ===

    /**
     * Função que repara a máquina escolhida.
     * @param m A máquina a ser reparada
     * @throws MachineUnavailableException se a máquina estiver c/ a saúde cheia e não precisar de reparo
     */
    public void repairMachine(Machine m) {
        if (!(m.getSaude() < m.getSaudeMaxima())) {
            throw new MachineUnavailableException("Não foi dessa vez! A " + m.getNome() + " não precisa de reparo.");
        }
        m.reparar();
    }

    // === GETTERS and SETTERS

    /**
     * Demandas cadastradas, uma por produto do catálogo
     *
     * @return lista de demandas
     */
    public List<Demanda> getDemandas() {
        return this.demandas;
    }

    /**
     * Produtos que passaram na inspeção e estão no armazém, incluindo os que precisam de manutenção
     *
     * @return lista de produtos fabricados
     */
    public List<Produto> getProdutosFabricados() {
        return this.produtosFabricados;
    }

    /**
     * Máquinas da fábrica, na ordem das etapas da produção
     *
     * @return lista de máquinas
     */
    public List<Machine> getMaquinas() {
        return this.maquinas;
    }

    /**
     * Relatório c/ os dados acumulados da produção, usado pela auditoria
     *
     * @return relatório
     */
    public Relatorio getRelatorio() {
        return this.relatorio;
    }

    /**
     * Matéria-prima da fábrica, c/ o estoque atual
     *
     * @return matéria-prima
     */
    public MateriaPrima getMateriaPrima() {
        return this.materiaPrima;
    }

    /**
     * Define a esteira que leva a matéria-prima e os produtos entre as máquinas
     *
     * @param conveyor esteira da fábrica
     */
    public void addNewConveyor(Esteira conveyor) {
        this.esteira = conveyor;
    }

    /**
     * Esteira da fábrica
     *
     * @return esteira
     */
    public Esteira getEsteira() {
        return this.esteira;
    }

    /**
     * Verba disponível
     *
     * @return verba atual, em R$
     */
    public float getVerba() {
        return verba;
    }

    /**
     * Diz se alguma máquina está quebrada (saúde abaixo do limiar crítico). Enquanto for true, nenhuma
     * fabricação começa. É calculado a partir das máquinas, então só volta a ser false quando todas
     * as quebradas forem reparadas
     *
     * @return true se tiver máquina esperando reparo
     */
    public boolean temMaquinaQuebrada() {
        return this.maquinas.stream().anyMatch(Machine::precisaManutencao);
    }
}
