package br.unicamp.ic.mc322.service;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import br.unicamp.ic.mc322.exception.MaquinaPrecisaReparoException;
import br.unicamp.ic.mc322.exception.MateriaPrimaInsuficienteException;
import br.unicamp.ic.mc322.exception.OrcamentoInsuficienteException;
import br.unicamp.ic.mc322.exception.TransicaoDemandaInvalidaException;
import br.unicamp.ic.mc322.model.demand.Demanda;
import br.unicamp.ic.mc322.model.demand.StatusDemanda;
import br.unicamp.ic.mc322.model.machine.Maquina;
import br.unicamp.ic.mc322.model.machine.MaquinaEmpacotamento;
import br.unicamp.ic.mc322.model.machine.MaquinaInspecao;
import br.unicamp.ic.mc322.model.machine.MaquinaProcessamento;
import br.unicamp.ic.mc322.model.productionline.LinhaDeProducao;
import br.unicamp.ic.mc322.model.rawmaterial.MateriaPrima;
import br.unicamp.ic.mc322.model.scenario.ConfiguracaoCenario;
import br.unicamp.ic.mc322.strategy.CatalogoEstrategias;
import br.unicamp.ic.mc322.util.GeradorAleatorio;
import br.unicamp.ic.mc322.view.ConsolePrinter;
import br.unicamp.ic.mc322.view.VelocidadeProducao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Regras da produção:
 * <ul>
 *     <li>cenário Ideal: cada uso tira exatamente 1 de saúde do motor;</li>
 *     <li>máquinas c/ chance de falha base 0 e custos exatos em float (1 + 0,5 + 0,25 = R$ 1,75 por unidade);</li>
 *     <li>seed fixa e velocidade instantânea (sem as pausas da animação).</li>
 * </ul>
 * O que ainda é aleatório (aprovado/rejeitado na inspeção) só é conferido pelo total
 */
@DisplayName("GerenciadorProducao")
class GerenciadorProducaoTest {
    private static final double CUSTO_POR_UNIDADE = 1.75;
    private static final double VIDRO_POR_COPO = 0.5;

    private GerenciadorProducao gerenciador;
    private LinhaDeProducao linha;
    private Demanda copos;

    @BeforeEach
    void prepararAmbiente() {
        GeradorAleatorio.setSeed(42);
        ConsolePrinter.setVelocidadeProducao(VelocidadeProducao.INSTANTANEA);
    }

    /**
     * Fábrica c/ uma demanda de copos (as outras zeradas)
     *
     * @param qtdCopos copos pedidos
     * @param vidro estoque de vidro, c/ mínimo de 5 kg
     * @param verba verba, em R$
     */
    private void montarFabrica(int qtdCopos, double vidro, float verba) {
        ConfiguracaoCenario ideal = ConfiguracaoCenario.buscarPorTipo("Ideal").orElseThrow();
        linha = new LinhaDeProducao("Linha de teste");
        linha.adicionarMaquina(new MaquinaProcessamento("Processamento", 20, 0, 1, ideal.getMultiplicadorCenario(), ideal.getDesgasteMaximo(), 2, 3));
        linha.adicionarMaquina(new MaquinaEmpacotamento("Empacotamento", 20, 0, 0.5, ideal.getMultiplicadorCenario(), ideal.getDesgasteMaximo(), 2));
        linha.adicionarMaquina(new MaquinaInspecao("Inspeção", 20, 0, 0.25, ideal.getMultiplicadorCenario(), ideal.getDesgasteMaximo(), 2, 3));

        List<Demanda> demandas = List.of(
                Demanda.restaurar(FabricaPlanta.buscarNoCatalogo("Copo").orElseThrow(), qtdCopos, 0, StatusDemanda.PENDENTE, 0, 0),
                Demanda.restaurar(FabricaPlanta.buscarNoCatalogo("Pote").orElseThrow(), 0, 0, StatusDemanda.PENDENTE, 0, 1),
                Demanda.restaurar(FabricaPlanta.buscarNoCatalogo("Kit Copo").orElseThrow(), 0, 0, StatusDemanda.PENDENTE, 0, 2)
        );
        gerenciador = FabricaPlanta.montar(ideal, new MateriaPrima("Vidro", vidro, "kg", 5, 1), verba, linha,
                demandas, CatalogoEstrategias.padrao()).getGerenciador();
        copos = gerenciador.getDemandas().getFirst();
    }

    // === Produção normal ===

    @Test
    @DisplayName("Produzir uma demanda conclui ela e desconta vidro e verba de cada unidade")
    void produzirConcluiEDesconta() {
        montarFabrica(3, 20, 100);

        gerenciador.produzirDemanda(copos);

        assertEquals(StatusDemanda.CONCLUIDA, copos.getStatus());
        assertEquals(3, copos.getQuantidadeProduzida());
        assertEquals(20 - 3 * VIDRO_POR_COPO, gerenciador.getMateriaPrima().getQuantidade());
        assertEquals(100 - 3 * CUSTO_POR_UNIDADE, gerenciador.getVerba());
    }

    @Test
    @DisplayName("Toda unidade produzida entra no relatório, e só as não rejeitadas vão p/ o armazém")
    void produzirAtualizaRelatorioEArmazem() {
        montarFabrica(5, 20, 100);

        gerenciador.produzirDemanda(copos);

        var relatorio = gerenciador.getRelatorio();
        assertEquals(5, relatorio.getTotalProdutos());
        assertEquals(relatorio.getTotalProdutosAprovados() + relatorio.getTotalProdutosPrecisamManutencao(),
                gerenciador.getProdutosFabricados().size());
        assertEquals(5 * VIDRO_POR_COPO, relatorio.getMateriaPrimaGastaTotal());
    }

    @Test
    @DisplayName("Depois de produzir, as máquinas e a esteira ficam desligadas e a esteira vazia")
    void produzirDesligaTudo() {
        montarFabrica(2, 20, 100);

        gerenciador.produzirDemanda(copos);

        assertFalse(linha.getMaquinas().stream().anyMatch(Maquina::estaLigada));
        assertFalse(gerenciador.getEsteira().estaLigada());
        assertNull(gerenciador.getEsteira().getProduto());
    }

    @Test
    @DisplayName("Cada unidade desgasta o motor de cada máquina em 1 no cenário Ideal")
    void produzirDesgastaMotores() {
        montarFabrica(4, 20, 100);

        gerenciador.produzirDemanda(copos);

        for (Maquina m : linha.getMaquinas()) {
            assertEquals(96, m.getSaude(), m.getNome());
        }
    }

    // === Limites de estoque e verba ===

    @Test
    @DisplayName("Vidro que deixa o estoque exatamente no mínimo é suficiente")
    void vidroExatamenteNoMinimo() {
        montarFabrica(3, 5 + 3 * VIDRO_POR_COPO, 100);

        gerenciador.produzirDemanda(copos);

        assertEquals(StatusDemanda.CONCLUIDA, copos.getStatus());
        assertEquals(5, gerenciador.getMateriaPrima().getQuantidade());
    }

    @Test
    @DisplayName("Vidro insuficiente lança MateriaPrimaInsuficienteException antes de gastar qualquer coisa")
    void vidroInsuficienteNaoGastaNada() {
        montarFabrica(3, 5 + 3 * VIDRO_POR_COPO - 0.1, 100);

        assertThrows(MateriaPrimaInsuficienteException.class, () -> gerenciador.produzirDemanda(copos));

        assertEquals(StatusDemanda.PENDENTE, copos.getStatus());
        assertEquals(5 + 3 * VIDRO_POR_COPO - 0.1, gerenciador.getMateriaPrima().getQuantidade());
        assertEquals(100, gerenciador.getVerba());
    }

    @Test
    @DisplayName("Verba exatamente igual ao custo é suficiente e termina zerada")
    void verbaExatamenteIgualAoCusto() {
        montarFabrica(3, 20, (float) (3 * CUSTO_POR_UNIDADE));

        gerenciador.produzirDemanda(copos);

        assertEquals(StatusDemanda.CONCLUIDA, copos.getStatus());
        assertEquals(0, gerenciador.getVerba());
    }

    @Test
    @DisplayName("Verba menor que o custo lança OrcamentoInsuficienteException antes de gastar qualquer coisa")
    void verbaInsuficienteNaoGastaNada() {
        montarFabrica(3, 20, 5);

        assertThrows(OrcamentoInsuficienteException.class, () -> gerenciador.produzirDemanda(copos));

        assertEquals(StatusDemanda.PENDENTE, copos.getStatus());
        assertEquals(5, gerenciador.getVerba());
        assertEquals(20, gerenciador.getMateriaPrima().getQuantidade());
    }

    // === Demandas que não podem ser fabricadas ===

    @Test
    @DisplayName("Demanda zerada lança DemandaInvalidaException")
    void demandaZeradaLanca() {
        montarFabrica(0, 20, 100);

        assertThrows(DemandaInvalidaException.class, () -> gerenciador.produzirDemanda(copos));
    }

    @Test
    @DisplayName("Fabricar de novo uma demanda concluída lança TransicaoDemandaInvalidaException")
    void demandaConcluidaLanca() {
        montarFabrica(2, 20, 100);
        gerenciador.produzirDemanda(copos);

        assertThrows(TransicaoDemandaInvalidaException.class, () -> gerenciador.produzirDemanda(copos));
    }

    // === Máquina quebrada ===

    @Test
    @DisplayName("Linha c/ máquina quebrada lança MaquinaPrecisaReparoException e não começa")
    void linhaQuebradaNaoComeca() {
        montarFabrica(3, 20, 100);
        linha.getMaquina(2).restaurarEstado(10, List.of(1.0, 1.0));

        assertThrows(MaquinaPrecisaReparoException.class, () -> gerenciador.produzirDemanda(copos));

        assertEquals(StatusDemanda.PENDENTE, copos.getStatus());
        assertEquals(100, gerenciador.getVerba());
    }

    @Test
    @DisplayName("Motor que quebra no meio do lote cancela a demanda e guarda o que já foi feito")
    void quebraNoMeioCancelaEGuardaProgresso() {
        montarFabrica(3, 20, 100);
        // Saúde 15 ainda funciona. depois da 1st unidade cai p 14 e quebra
        linha.getMaquina(0).restaurarEstado(15, List.of(1.0, 1.0));

        gerenciador.produzirDemanda(copos);

        assertEquals(StatusDemanda.CANCELADA, copos.getStatus());
        assertEquals(1, copos.getQuantidadeProduzida());
        assertEquals(20 - VIDRO_POR_COPO, gerenciador.getMateriaPrima().getQuantidade(), "só a 1ª unidade gastou vidro");
        assertEquals(100 - CUSTO_POR_UNIDADE, gerenciador.getVerba(), "a 2ª unidade não foi cobrada");
    }

    @Test
    @DisplayName("Depois de reparar, a demanda cancelada continua de onde parou")
    void retomarDepoisDoReparo() {
        montarFabrica(3, 20, 100);
        linha.getMaquina(0).restaurarEstado(15, List.of(1.0, 1.0));
        gerenciador.produzirDemanda(copos);

        linha.repararTodas();
        gerenciador.produzirDemanda(copos);

        assertEquals(StatusDemanda.CONCLUIDA, copos.getStatus());
        assertEquals(3, copos.getQuantidadeProduzida());
        assertEquals(20 - 3 * VIDRO_POR_COPO, gerenciador.getMateriaPrima().getQuantidade());
    }

    // === Compra de matéria-prima ===

    @Test
    @DisplayName("Comprar vidro soma no estoque e desconta o preço da verba")
    void comprarVidro() {
        montarFabrica(0, 20, 100);

        float custo = gerenciador.comprarMateriaPrima(10);

        assertEquals(10, custo);
        assertEquals(30, gerenciador.getMateriaPrima().getQuantidade());
        assertEquals(90, gerenciador.getVerba());
    }

    @Test
    @DisplayName("Comprar sem verba lança OrcamentoInsuficienteException e não muda nada")
    void comprarSemVerbaNaoMudaNada() {
        montarFabrica(0, 20, 5);

        assertThrows(OrcamentoInsuficienteException.class, () -> gerenciador.comprarMateriaPrima(6));

        assertEquals(20, gerenciador.getMateriaPrima().getQuantidade());
        assertEquals(5, gerenciador.getVerba());
    }
}
