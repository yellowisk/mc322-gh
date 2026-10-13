package domain.entities.productionmanager;

import domain.entities.demand.Demanda;
import domain.entities.demand.StatusDemanda;
import domain.entities.conveyor.Esteira;
import domain.entities.machine.Maquina;
import domain.entities.product.Produto;
import domain.entities.product.StatusProduto;
import domain.entities.rawmaterial.MateriaPrima;
import domain.entities.report.Relatorio;
import domain.exceptions.OrcamentoInsuficienteException;
import domain.exceptions.MateriaPrimaInsuficienteException;
import domain.exceptions.DemandaInvalidaException;
import domain.exceptions.TransicaoDemandaInvalidaException;
import domain.exceptions.MaquinaPrecisaReparoException;
import domain.exceptions.MaquinaIndisponivelException;
import domain.exceptions.EstrategiaNaoSelecionadaException;
import domain.exceptions.NenhumaDemandaElegivelException;
import domain.exceptions.PlantaException;
import domain.interfaces.EstrategiaProducao;
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
public class GerenciadorProducao {
    /// ArrayList de demandas. Haverá uma demanda por produtosDisponiveisFabricacao
    private List<Demanda> demandas = new ArrayList<Demanda>();
    /// ArrayList de produtos fabricados com sucesso e com necessidade de reparo
    private List<Produto> produtosFabricados = new ArrayList<Produto>();
    /// Arraylist de produtos que servirão de moldes/exemplo para o menu e para a produção nas máquinas
    private List<Produto> produtosDisponiveisFabricacao = new ArrayList<Produto>();
    /// ArrayList de máquinas existentes na fábrica
    private List<Maquina> maquinas = new ArrayList<Maquina>();
    /// esteira da fábrica
    private Esteira esteira;
    /// produto selecionado atualmente
    private Produto produtoAtual;
    /// matéria-prima da fábrica
    private final MateriaPrima materiaPrima;
    /// verba atual da fábrica
    private float verba;
    /// estratégia selecionada atualmente
    private EstrategiaProducao estrategiaAtual;
    /// número de lotes produzidos
    private int contadorLote = 0;
    /// objeto da classe Relatorio para salvar dados para log
    private Relatorio relatorio;
    /// lista das etapas de produção instanciada para evitar laço 'for' otimizado direto no Enum.
    private static final EtapaProducao[] etapasProducao = EtapaProducao.values();

    /**
     * Cria o gerenciador sem máquinas, produtos, esteira nem estratégia, que são adicionados depois pelo menu
     *
     * @param materiaPrima matéria-prima da fábrica
     * @param verba verba inicial, em R$, que vem do cenário escolhido
     * @param relatorio relatório onde a auditoria vai acumulando os dados da produção
     */
    public GerenciadorProducao(MateriaPrima materiaPrima, float verba, Relatorio relatorio) {
        this.materiaPrima = materiaPrima;
        this.verba = verba;
        this.relatorio = relatorio;
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
    public void adicionarMaquina(Maquina m) {
        this.maquinas.add(m);
    }
    
    /**
     * Troca a estratégia que escolhe a próxima demanda
     *
     * @param s nova estratégia
     */
    public void setEstrategiaAtual(EstrategiaProducao s) {
        this.estrategiaAtual = s;
    }
    
    /**
     * Estratégia que escolhe a próxima demanda
     *
     * @return estratégia atual, ou null se nenhuma tiver sido escolhida
     */
    public EstrategiaProducao getEstrategiaAtual() {
        return this.estrategiaAtual;
    }

    /**
     * Inicia a produção da próxima demanda. A demanda é escolhida pela estratégia atual.
     *
     * @throws EstrategiaNaoSelecionadaException se nenhuma estratégia tiver sido escolhida
     * @throws NenhumaDemandaElegivelException se a estratégia não encontrar nenhuma demanda elegível
     * @throws domain.exceptions.PlantaException qualquer erro de {@link #produzirDemanda(Demanda)}
     */
    public void iniciarProducao() {
        if (this.estrategiaAtual == null) {
            throw new EstrategiaNaoSelecionadaException("Nenhuma estratégia de produção selecionada.\n");
        }

        Demanda proximaDemanda = espiarProximaDemanda();

        if (proximaDemanda == null) {
            String mensagem = String.format("Dessa vez não é! %s não encontrou nenhuma demanda elegível.\n",
                    this.estrategiaAtual.getNomeEstrategia());
            throw new NenhumaDemandaElegivelException(mensagem);
        }

        System.out.printf("%s escolheu: %s (%d un)\n",
                this.estrategiaAtual.getNomeEstrategia(), proximaDemanda.getNomeProduto(), proximaDemanda.getQuantidade());
        produzirDemanda(proximaDemanda);
    }

    /**
     * Retorna a demanda que a estratégia escolheria, sem fabricá-la
     * @return próxima demanda
     */
    public Demanda espiarProximaDemanda() {
        if (this.estrategiaAtual == null) {
            return null;
        }
        // ehViavel() and getCustoEstimado() read cached cost, so it's gotta be fresh beforehand
        atualizarCustoEstimadoTodasDemandas();
        return this.estrategiaAtual.selecionarDemanda(this.demandas, this.verba);
    }

    /**
     * Simula o custo total de um produto passando por todas as máquinas da fábrica
     * @return custo total de operação
     */
    public double calcCustoTotalOperacao() {
        double soma = 0;
        for (Maquina m : this.maquinas) {
            soma += m.getCustoOperacao();
        }
        return soma;
    }

    /**
     * Atualiza o custo estimado de todas as demandas
     */
    public void atualizarCustoEstimadoTodasDemandas() {
        double custoOperacaoUnitario = calcCustoTotalOperacao();
        for (Demanda d : this.demandas) {
            d.atualizarCustoEstimado(custoOperacaoUnitario);
        }
    }

    /**
     * Quanto custaria fabricar todas as demandas pendentes. As concluídas e canceladas ficam fora da projeção.
     * Também atualiza o custo estimado de cada demanda
     *
     * @return custo total projetado, em R$
     */
    public double getCustoProjetado() {
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
     * @param demanda A demanda que será fabricada.
     * @throws MaquinaPrecisaReparoException se alguma máquina precisar de reparo
     * @throws DemandaInvalidaException se o produto da demanda não existir ou se ela não tiver unidades pedidas
     * @throws TransicaoDemandaInvalidaException se o status da demanda não permitir fabricar
     * @throws OrcamentoInsuficienteException se o orçamento não cobrir o custo estimado
     * @throws MateriaPrimaInsuficienteException se o estoque não cobrir a matéria-prima das unidades que faltam
     */
    public void produzirDemanda(Demanda demanda) {
        // VERIFICAÇÕES
        if (temMaquinaQuebrada()) {
            throw new MaquinaPrecisaReparoException("Não podemos iniciar a fabricação. Há máquinas precisando de reparo.");
        }

        this.produtoAtual = this.getProdutoPeloNome(demanda.getNomeProduto());

        if (produtoAtual == null) {
            throw new DemandaInvalidaException("Dessa vez não é! Produto sem demanda cadastrada.");
        }

        if (demanda.getQuantidade() <= 0) {
            throw new DemandaInvalidaException(String.format(
                    "Dessa vez não é! Nenhuma unidade de %s foi demandada. Atualize a demanda antes de fabricar.",
                    produtoAtual.getNome()));
        }

        if (!demanda.getStatus().podeSerFabricada()) {
            throw new TransicaoDemandaInvalidaException(String.format(
                    "Dessa vez não é! Demanda de %s está %s e não pode ser fabricada. Atualize a demanda para reativá-la.",
                    produtoAtual.getNome(), demanda.getStatus().getDescricao()));
        }

        atualizarCustoEstimadoTodasDemandas();
        if (!demanda.ehViavel(this.verba)) {
            throw new OrcamentoInsuficienteException(String.format(
                    "Dessa vez não é! Orçamento insuficiente para fabricar %s: custo R$ %.2f, orçamento R$ %.2f.",
                    produtoAtual.getNome(), demanda.getCustoEstimado(), this.verba));
        }

        // Confere a matéria-prima de todas as unidades que faltam, p/ não parar no meio c/ estoque e verba já gastos
        double materiaPrimaNecessaria = demanda.getQuantidadeRestante() * produtoAtual.getQuantidadeMateriaPrimaPorUnidade();
        if (!this.materiaPrima.estaDisponivel(materiaPrimaNecessaria)) {
            throw new MateriaPrimaInsuficienteException(String.format(
                    "Cadê a força? Estoque insuficiente para fabricar %d %s:\n precisa de %.2f %s, tem %.2f %s (mínimo %d %s). "
                            + "Compre matéria-prima e fabrique de novo.",
                    demanda.getQuantidadeRestante(), produtoAtual.getNome(),
                    materiaPrimaNecessaria, this.materiaPrima.getUnidade(),
                    this.materiaPrima.getQuantidade(), this.materiaPrima.getUnidade(),
                    this.materiaPrima.getQuantidadeMinima(), this.materiaPrima.getUnidade()));
        }

        int jaProduzidas = demanda.getQuantidadeProduzida();
        if (demanda.getStatus() == StatusDemanda.CANCELADA && jaProduzidas > 0) {
            ConsolePrinter.arvoreInfo(false, "Retomando demanda de %s de onde parou (%d/%d).",
                    produtoAtual.getNome(), jaProduzidas, demanda.getQuantidade());
        }


        demanda.iniciarProducao();
        int lote = ++this.contadorLote;

        this.getEsteira().ligar();
        for (Maquina m: this.getMaquinas()) {
            m.ligar();
        }

        System.out.printf("Eitcha!!! Iniciando produção de: %s (lote %d)\n", produtoAtual.getNome(), lote);
        ConsolePrinter.pausarProducao(1500);

        int quantidadeRestante = demanda.getQuantidadeRestante();
        int fabricadasNaRodada = 0;

        // Variáveis de auditoria
        int aprovadasNaRodada = 0;
        int precisamManutencaoNaRodada = 0;
        int rejeitadasNaRodada = 0;
        double tempoProducaoNaRodada = 0;
        double dinheiroGastoNaRodada = 0;

        Produto produtoNaLinha = null;

        while (fabricadasNaRodada < quantidadeRestante) {
            System.out.printf("(%d/%d) %s " + "─".repeat(24) + "\n", (jaProduzidas + fabricadasNaRodada + 1), demanda.getQuantidade(), produtoAtual.getNome());

            try {
                for (EtapaProducao etapaAtual : etapasProducao) {
                    Maquina maquinaAtual = this.maquinas.get(etapaAtual.getCodigo());

                    ConsolePrinter.cabecalhoEtapa(maquinaAtual, "%s", etapaAtual.getNome());

                    // Confere tudo antes de cobrar a etapa, p/ não pagar por uma operação que não vai acontecer
                    maquinaAtual.verificarDisponivel();
                    double materiaPrimaPorUnidade = produtoAtual.getQuantidadeMateriaPrimaPorUnidade();
                    if (etapaAtual == EtapaProducao.PROCESSAMENTO && !this.materiaPrima.estaDisponivel(materiaPrimaPorUnidade)) {
                        throw new MateriaPrimaInsuficienteException(String.format(
                                "Cadê a força? Estoque insuficiente para fabricar mais um %s: precisa de %.2f %s. "
                                        + "Compre matéria-prima e fabrique de novo para continuar de onde parou.",
                                produtoAtual.getNome(), materiaPrimaPorUnidade, this.materiaPrima.getUnidade()));
                    }

                    if (!descontarCustoOperacao(maquinaAtual)) {
                        String mensagem = String.format("Dessa vez não é! Orçamento insuficiente para operar a máquina de %s!", maquinaAtual.getTipo());
                        throw new OrcamentoInsuficienteException(mensagem);
                    }
                    dinheiroGastoNaRodada += maquinaAtual.getCustoOperacao(); // Auditoria

                    if (etapaAtual == EtapaProducao.PROCESSAMENTO) {
                        esteira.adicionarMateriaPrima(materiaPrimaPorUnidade);
                        ConsolePrinter.arvoreInfo(false, "%s carregado para a esteira.", this.materiaPrima.getNome());

                        this.materiaPrima.consumir(esteira.removerMateriaPrima());
                        relatorio.aumentarMateriaPrimaGasta(materiaPrimaPorUnidade); // Auditoria, só depois do consumo
                        ConsolePrinter.arvoreInfo(false, "%s transportado até a máquina de %s. E é ligeiro!", this.materiaPrima.getNome(), maquinaAtual.getTipo());

                        ConsolePrinter.arvoreInfo(false, "Máquina %s %.2f %s de %s...", etapaAtual.getGerundio(), this.produtoAtual.getQuantidadeMateriaPrimaPorUnidade(), this.materiaPrima.getUnidade(), this.materiaPrima.getNome());
                        produtoNaLinha = maquinaAtual.processar(this.produtoAtual);
                        produtoNaLinha.setLote(lote);

                        ConsolePrinter.arvoreOk(true, "Tu não acredita! Sabe o que é? \"%s\" #%d criado.", produtoNaLinha.getNome(), produtoNaLinha.getId());
                        this.esteira.adicionarProduto(produtoNaLinha);
                    } else {
                        ConsolePrinter.arvoreInfo(false, "%s #%d transportado até a máquina de %s. E é ligeiro!", produtoNaLinha.getNome(), produtoNaLinha.getId(), maquinaAtual.getTipo());

                        ConsolePrinter.arvoreInfo(false, "Máquina %s o produto %s #%d...", etapaAtual.getGerundio(), produtoNaLinha.getNome(), produtoNaLinha.getId());
                        produtoNaLinha = maquinaAtual.processar(this.esteira.removerProduto());

                        ConsolePrinter.arvoreOk(true, "Produto %s #%d foi %s com sucesso.", produtoNaLinha.getNome(), produtoNaLinha.getId(), etapaAtual.getVerboParticipio());
                        this.esteira.adicionarProduto(produtoNaLinha);
                    }
                }

                // Validação final
                if (produtoNaLinha.getStatus() == StatusProduto.APROVADO) {
                    this.produtosFabricados.add(this.esteira.removerProduto());
                    ConsolePrinter.arvoreOk(true, "Eitcha, como ele tem força! O produto %s #%d" + ConsolePrinter.VERDE + " foi aprovado" + ConsolePrinter.RESET + " e enviado ao armazém!", produtoNaLinha.getNome(), produtoNaLinha.getId());
                    aprovadasNaRodada++;
                    relatorio.aumentarTotalProdutosAprovados();
                } else if (produtoNaLinha.getStatus() == StatusProduto.PRECISA_MANUTENCAO) {
                    this.produtosFabricados.add(this.esteira.removerProduto());
                    ConsolePrinter.arvoreOk(true, "Cade a força? O produto %s #%d" + ConsolePrinter.AMARELO + " foi parcialmente aprovado" + ConsolePrinter.RESET + " e enviado ao armazém!", produtoNaLinha.getNome(), produtoNaLinha.getId());
                    precisamManutencaoNaRodada++;
                    relatorio.aumentarTotalProdutosPrecisamManutencao();
                } else {
                    this.esteira.removerProduto();
                    ConsolePrinter.arvoreFalha(true, "Cade a força? O produto %s #%d" + ConsolePrinter.VERMELHO + " foi rejeitado" + ConsolePrinter.RESET + " na inspeção e descartado.", produtoNaLinha.getNome(), produtoNaLinha.getId());
                    relatorio.aumentarTotalProdutosRejeitados();
                    rejeitadasNaRodada++;
                }

                tempoProducaoNaRodada += produtoAtual.calcularTempoProducao();
                fabricadasNaRodada++;

            } catch (PlantaException e) {
                // Máquina quebrou ou orçamento/matéria-prima acabaram no meio do lote: a demanda é cancelada c/ o progresso salvo
                ConsolePrinter.arvoreFalha(true, e.getMessage());
                descartarUnidadeIncompleta();
                break;
            }
        }

        relatorio.aumentarDinheiroGasto(dinheiroGastoNaRodada);
        relatorio.aumentarTempoTotalProducao(tempoProducaoNaRodada);
        relatorio.calcularTaxaSucesso();

        String textoInfo;

        if (fabricadasNaRodada == quantidadeRestante) {
            demanda.concluir(fabricadasNaRodada, tempoProducaoNaRodada);
            textoInfo = String.format(" Demanda de %s concluída em %.2f segundos de produção.",
                    demanda.getNomeProduto(), demanda.getTempoTotalProducao());
        } else {
            demanda.cancelar(fabricadasNaRodada, tempoProducaoNaRodada);
            textoInfo = String.format(" Demanda de %s cancelada (%d/%d). Fabrique de novo para continuar de onde parou.",
                    demanda.getNomeProduto(), demanda.getQuantidadeProduzida(), demanda.getQuantidade());
        }

        ConsolePrinter.cartao("%d produtos fabricados: %d aprovados, %d precisam de manutenção e %d rejeitados." + "\n" + textoInfo,
                fabricadasNaRodada, aprovadasNaRodada,
                precisamManutencaoNaRodada, rejeitadasNaRodada);

        // Desliga as máquinas
        this.getEsteira().desligar();
        for (Maquina m: this.getMaquinas()) {
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
     * @param maquina máquina da etapa
     * @return true se o custo foi descontado, false se a verba não cobre (e nada é descontado)
     */
    private boolean descontarCustoOperacao(Maquina maquina) {
        if (this.verba >= maquina.getCustoOperacao()) {
            this.verba -= (float) maquina.getCustoOperacao();
            return true;
        }
        return false;
    }

    /**
     * Compra matéria-prima e desconta o valor da verba. Se a verba não cobrir, nada é alterado
     *
     * @param quantidade quantidade a comprar, na unidade da matéria-prima
     * @return quanto a compra custou no total, em R$
     * @throws OrcamentoInsuficienteException se a verba não cobrir a compra
     */
    public float comprarMateriaPrima(int quantidade) {
        float custoTotal = quantidade * this.materiaPrima.getPreco();
        if (this.verba < custoTotal) {
            throw new OrcamentoInsuficienteException("Dessa vez não é! Orçamento insuficiente para comprar matéria-prima!");
        }
        verba -= custoTotal;
        this.materiaPrima.adicionarEstoque(quantidade);
        return custoTotal;
    }

    // === REPAIR ===

    /**
     * Função que repara a máquina escolhida.
     * @param m A máquina a ser reparada
     * @throws MaquinaIndisponivelException se a máquina estiver c/ a saúde cheia e não precisar de reparo
     */
    public void repararMaquina(Maquina m) {
        if (!(m.getSaude() < m.getSaudeMaxima())) {
            throw new MaquinaIndisponivelException("Não foi dessa vez! A " + m.getNome() + " não precisa de reparo.");
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
    public List<Maquina> getMaquinas() {
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
     * @param esteira esteira da fábrica
     */
    public void adicionarEsteira(Esteira esteira) {
        this.esteira = esteira;
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
        return this.maquinas.stream().anyMatch(Maquina::precisaManutencao);
    }
}
