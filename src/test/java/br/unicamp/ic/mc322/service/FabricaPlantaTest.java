package br.unicamp.ic.mc322.service;

import br.unicamp.ic.mc322.model.demand.Demanda;
import br.unicamp.ic.mc322.model.demand.StatusDemanda;
import br.unicamp.ic.mc322.model.machine.Maquina;
import br.unicamp.ic.mc322.model.machine.MaquinaEmpacotamento;
import br.unicamp.ic.mc322.model.machine.MaquinaInspecao;
import br.unicamp.ic.mc322.model.machine.MaquinaProcessamento;
import br.unicamp.ic.mc322.model.product.CopoDeVidro;
import br.unicamp.ic.mc322.model.productionline.LinhaDeProducao;
import br.unicamp.ic.mc322.model.rawmaterial.MateriaPrima;
import br.unicamp.ic.mc322.model.scenario.ConfiguracaoCenario;
import br.unicamp.ic.mc322.strategy.CatalogoEstrategias;
import br.unicamp.ic.mc322.strategy.EstrategiaProducao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FabricaPlanta e Planta")
class FabricaPlantaTest {
    private ConfiguracaoCenario intermediario;
    private Planta padrao;

    @BeforeEach
    void criarFabricaPadrao() {
        intermediario = ConfiguracaoCenario.buscarPorTipo("Intermediário").orElseThrow();
        padrao = FabricaPlanta.criarPadrao(intermediario);
    }

    @Test
    @DisplayName("Fábrica padrão começa c/ a verba do cenário")
    void padraoComecaComVerbaDoCenario() {
        assertEquals(intermediario.getVerba(), padrao.getGerenciador().getVerba());
        assertSame(intermediario, padrao.getCenario());
    }

    @Test
    @DisplayName("Fábrica padrão tem as 3 máquinas na ordem das etapas, já na linha do gerenciador")
    void padraoTemTresMaquinasNaOrdem() {
        List<Maquina> maquinas = padrao.getLinha().getMaquinas();

        assertEquals(3, maquinas.size());
        assertInstanceOf(MaquinaProcessamento.class, maquinas.get(0));
        assertInstanceOf(MaquinaEmpacotamento.class, maquinas.get(1));
        assertInstanceOf(MaquinaInspecao.class, maquinas.get(2));
        assertEquals(padrao.getLinha().getMaquinas(), padrao.getGerenciador().getMaquinas(),
                "gerenciador e planta tem que apontar p/ a mesma linha");
    }

    @Test
    @DisplayName("As máquinas usam o multiplicador de falha do cenário")
    void maquinasUsamMultiplicadorDoCenario() {
        for (Maquina m : padrao.getLinha().getMaquinas()) {
            assertEquals(intermediario.getMultiplicadorCenario(), m.getMultiplicadorCenario());
        }
    }

    @Test
    @DisplayName("Criar uma fábrica de outro cenário não muda o multiplicador das máquinas que já existem")
    void outraFabricaNaoMudaMultiplicadorDaAtual() {
        ConfiguracaoCenario apocaliptico = ConfiguracaoCenario.buscarPorTipo("Apocalíptico").orElseThrow();

        FabricaPlanta.criarPadrao(apocaliptico);

        Maquina daPadrao = padrao.getLinha().getMaquina(0);
        assertEquals(intermediario.getMultiplicadorCenario(), daPadrao.getMultiplicadorCenario());
    }

    @Test
    @DisplayName("Fábrica padrão tem uma demanda zerada e pendente por produto, e a estratégia padrão")
    void padraoTemUmaDemandaZeradaPorProduto() {
        List<Demanda> demandas = padrao.getGerenciador().getDemandas();

        assertEquals(List.of("Copo", "Pote", "Kit Copo"), demandas.stream().map(Demanda::getNomeProduto).toList());
        for (Demanda d : demandas) {
            assertEquals(0, d.getQuantidade());
            assertEquals(StatusDemanda.PENDENTE, d.getStatus());
        }
        assertSame(CatalogoEstrategias.padrao(), padrao.getGerenciador().getEstrategiaAtual());
        assertNotNull(padrao.getGerenciador().getEsteira());
    }

    @Test
    @DisplayName("Depois de ativar, a próxima demanda chega depois de todas as da fábrica")
    void ativarAjustaOrdemDeChegada() {
        long ultima = padrao.getGerenciador().getDemandas().stream()
                .mapToLong(Demanda::getOrdemChegada).max().orElseThrow();

        padrao.ativar();
        Demanda nova = new Demanda(new CopoDeVidro("Copo", 0.5), 1);

        assertEquals(ultima + 1, nova.getOrdemChegada());
    }

    @Test
    @DisplayName("Montar usa as demandas e a estratégia recebidas")
    void montarUsaDemandasEEstrategiaRecebidas() {
        List<Demanda> demandas = demandasDoCatalogo();
        EstrategiaProducao ferrari = CatalogoEstrategias.buscarPorNome("Ferrari").orElseThrow();

        Planta montada = FabricaPlanta.montar(intermediario, vidro(), 123, linhaVazia(), demandas, ferrari);

        assertEquals(demandas, montada.getGerenciador().getDemandas());
        assertSame(ferrari, montada.getGerenciador().getEstrategiaAtual());
        assertEquals(123, montada.getGerenciador().getVerba());
    }

    @Test
    @DisplayName("Montar c/ verba zero é permitido")
    void montarComVerbaZero() {
        Planta montada = FabricaPlanta.montar(intermediario, vidro(), 0, linhaVazia(), demandasDoCatalogo(),
                CatalogoEstrategias.padrao());

        assertEquals(0, montada.getGerenciador().getVerba());
    }

    @Test
    @DisplayName("Montar c/ verba negativa lança IllegalArgumentException")
    void montarComVerbaNegativaLanca() {
        assertThrows(IllegalArgumentException.class, () -> FabricaPlanta.montar(intermediario, vidro(), -0.01f,
                linhaVazia(), demandasDoCatalogo(), CatalogoEstrategias.padrao()));
    }

    @Test
    @DisplayName("Montar c/ demanda de produto fora do catálogo lança IllegalArgumentException")
    void montarComProdutoForaDoCatalogoLanca() {
        List<Demanda> demandas = new ArrayList<>(demandasDoCatalogo());
        demandas.set(0, new Demanda(new CopoDeVidro("Taça", 0.5), 1));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> FabricaPlanta.montar(
                intermediario, vidro(), 10, linhaVazia(), demandas, CatalogoEstrategias.padrao()));
        assertTrue(e.getMessage().contains("Taça"), e.getMessage());
    }

    @Test
    @DisplayName("Montar c/ duas demandas do mesmo produto lança IllegalArgumentException")
    void montarComProdutoRepetidoLanca() {
        List<Demanda> demandas = new ArrayList<>(demandasDoCatalogo());
        demandas.set(1, new Demanda(new CopoDeVidro("Copo", 0.5), 1));

        assertThrows(IllegalArgumentException.class, () -> FabricaPlanta.montar(
                intermediario, vidro(), 10, linhaVazia(), demandas, CatalogoEstrategias.padrao()));
    }

    @Test
    @DisplayName("Montar faltando a demanda de um produto lança IllegalArgumentException")
    void montarFaltandoDemandaLanca() {
        List<Demanda> demandas = demandasDoCatalogo().subList(0, 2);

        assertThrows(IllegalArgumentException.class, () -> FabricaPlanta.montar(
                intermediario, vidro(), 10, linhaVazia(), demandas, CatalogoEstrategias.padrao()));
    }

    // === Auxiliares ===

    private static MateriaPrima vidro() {
        return new MateriaPrima("Vidro", 10, "kg", 5, 1);
    }

    private static LinhaDeProducao linhaVazia() {
        return new LinhaDeProducao("Linha de teste");
    }

    /** Uma demanda nova p/ cada produto do catálogo, na ordem do catálogo */
    private static List<Demanda> demandasDoCatalogo() {
        return FabricaPlanta.criarCatalogo().stream().map(p -> new Demanda(p, 2)).toList();
    }
}
