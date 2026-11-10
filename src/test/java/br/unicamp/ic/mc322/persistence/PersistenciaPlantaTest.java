package br.unicamp.ic.mc322.persistence;

import br.unicamp.ic.mc322.model.demand.Demanda;
import br.unicamp.ic.mc322.model.demand.StatusDemanda;
import br.unicamp.ic.mc322.model.machine.Maquina;
import br.unicamp.ic.mc322.model.machine.MaquinaEmpacotamento;
import br.unicamp.ic.mc322.model.machine.MaquinaInspecao;
import br.unicamp.ic.mc322.model.machine.MaquinaProcessamento;
import br.unicamp.ic.mc322.model.product.CopoDeVidro;
import br.unicamp.ic.mc322.service.FabricaPlanta;
import br.unicamp.ic.mc322.service.Planta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O que volta do arquivo tem que ser igual ao que foi salvo. Testes feitos em pasta temporária do JUnit
 */
@DisplayName("PersistenciaPlanta: Salvar e Carregar")
class PersistenciaPlantaTest {
    @TempDir
    Path pasta;

    private Path arquivo;
    private PersistenciaPlanta persistencia;

    @BeforeEach
    void apontarParaPastaTemporaria() {
        arquivo = pasta.resolve("planta.xml");
        persistencia = new PersistenciaPlanta(arquivo);
    }

    @Test
    @DisplayName("Sem save, existeSave é falso e não há data de último save")
    void semSave() {
        assertFalse(persistencia.existeSave());
        assertTrue(persistencia.dataUltimoSave().isEmpty());
    }

    @Test
    @DisplayName("Salvar cria o arquivo, inclusive as pastas que faltam")
    void salvarCriaArquivoEPastas() throws Exception {
        PersistenciaPlanta emSubpasta = new PersistenciaPlanta(pasta.resolve("a/b/planta.xml"));

        emSubpasta.salvar(PlantasDeTeste.padrao());

        assertTrue(emSubpasta.existeSave());
        assertTrue(emSubpasta.dataUltimoSave().isPresent());
        String xml = Files.readString(emSubpasta.getArquivo());
        assertTrue(xml.startsWith("<?xml"), xml);
        assertTrue(xml.contains("<planta") && xml.contains("versao=\"1\""), xml);
    }

    @Test
    @DisplayName("Salvar não deixa arquivo temporário sobrando na pasta")
    void salvarNaoDeixaTemporario() throws Exception {
        persistencia.salvar(PlantasDeTeste.padrao());

        try (var arquivos = Files.list(pasta)) {
            assertEquals(List.of(arquivo), arquivos.toList());
        }
    }

    @Test
    @DisplayName("Cenário, verba, estratégia e matéria-prima voltam iguais")
    void idaEVoltaDosDadosGerais() {
        Planta original = PlantasDeTeste.alterada();

        persistencia.salvar(original);
        Planta carregada = persistencia.carregar();

        assertAll(
                () -> assertSame(original.getCenario(), carregada.getCenario()),
                () -> assertEquals(412.35f, carregada.getGerenciador().getVerba()),
                () -> assertSame(original.getGerenciador().getEstrategiaAtual(), carregada.getGerenciador().getEstrategiaAtual()),
                () -> assertEquals("Vidro", carregada.getGerenciador().getMateriaPrima().getNome()),
                () -> assertEquals("kg", carregada.getGerenciador().getMateriaPrima().getUnidade()),
                () -> assertEquals(37.5, carregada.getGerenciador().getMateriaPrima().getQuantidade()),
                () -> assertEquals(5, carregada.getGerenciador().getMateriaPrima().getQuantidadeMinima()),
                () -> assertEquals(1.25f, carregada.getGerenciador().getMateriaPrima().getPreco())
        );
    }

    @Test
    @DisplayName("Configuração e estado de cada máquina e de cada parte voltam iguais")
    void idaEVoltaDasMaquinas() {
        Planta original = PlantasDeTeste.alterada();

        persistencia.salvar(original);
        Planta carregada = persistencia.carregar();

        List<Maquina> antes = original.getLinha().getMaquinas();
        List<Maquina> depois = carregada.getLinha().getMaquinas();
        assertEquals("Linha Teste", carregada.getLinha().getNome());
        assertEquals(antes.size(), depois.size());
        for (int i = 0; i < antes.size(); i++) {
            Maquina a = antes.get(i);
            Maquina d = depois.get(i);
            assertAll("máquina " + i,
                    () -> assertEquals(a.getClass(), d.getClass()),
                    () -> assertEquals(a.getNome(), d.getNome()),
                    () -> assertEquals(a.getCapacidadeMaxima(), d.getCapacidadeMaxima()),
                    () -> assertEquals(a.getChanceFalhaBase(), d.getChanceFalhaBase()),
                    () -> assertEquals(a.getCustoOperacao(), d.getCustoOperacao()),
                    () -> assertEquals(a.getMultiplicadorCenario(), d.getMultiplicadorCenario()),
                    () -> assertEquals(a.getSaude(), d.getSaude()),
                    () -> assertEquals(a.getCalibracoesSensores(), d.getCalibracoesSensores()),
                    () -> assertEquals(a.getChanceFalha(), d.getChanceFalha()),
                    () -> assertEquals(a.precisaManutencao(), d.precisaManutencao())
            );
        }
        MaquinaProcessamento processamento = assertInstanceOf(MaquinaProcessamento.class, depois.get(0));
        MaquinaEmpacotamento empacotamento = assertInstanceOf(MaquinaEmpacotamento.class, depois.get(1));
        MaquinaInspecao inspecao = assertInstanceOf(MaquinaInspecao.class, depois.get(2));
        assertAll("partes específicas",
                () -> assertEquals(List.of(false, true, false, true), processamento.getResistenciasQueimadas()),
                () -> assertEquals(0.3, empacotamento.getDesgasteLamina()),
                () -> assertEquals(List.of(true, false), inspecao.getLentesSujas()),
                () -> assertTrue(inspecao.precisaManutencao(), "o motor da inspeção foi salvo quebrado (12)")
        );
    }

    @Test
    @DisplayName("Demandas voltam c/ quantidade, progresso, status, tempo e ordem de chegada")
    void idaEVoltaDasDemandas() {
        Planta original = PlantasDeTeste.alterada();

        persistencia.salvar(original);
        List<Demanda> antes = original.getGerenciador().getDemandas();
        List<Demanda> depois = persistencia.carregar().getGerenciador().getDemandas();

        assertEquals(antes.size(), depois.size());
        for (int i = 0; i < antes.size(); i++) {
            Demanda a = antes.get(i);
            Demanda d = depois.get(i);
            assertAll("demanda de " + a.getNomeProduto(),
                    () -> assertEquals(a.getNomeProduto(), d.getNomeProduto()),
                    () -> assertEquals(a.getQuantidade(), d.getQuantidade()),
                    () -> assertEquals(a.getQuantidadeProduzida(), d.getQuantidadeProduzida()),
                    () -> assertEquals(a.getStatus(), d.getStatus()),
                    () -> assertEquals(a.getTempoTotalProducao(), d.getTempoTotalProducao()),
                    () -> assertEquals(a.getOrdemChegada(), d.getOrdemChegada()),
                    () -> assertEquals(a.getMateriaPrimaTotal(), d.getMateriaPrimaTotal()),
                    () -> assertEquals(a.getCustoEstimado(), d.getCustoEstimado())
            );
        }
        assertEquals(StatusDemanda.CANCELADA, depois.getFirst().getStatus());
    }

    @Test
    @DisplayName("Fábrica padrão recém-criada também vai e volta")
    void idaEVoltaDaFabricaPadrao() {
        Planta original = PlantasDeTeste.padrao();

        persistencia.salvar(original);
        Planta carregada = persistencia.carregar();

        assertEquals(original.getGerenciador().getVerba(), carregada.getGerenciador().getVerba());
        assertEquals(3, carregada.getLinha().getMaquinas().size());
        assertTrue(carregada.getLinha().getMaquinas().stream().noneMatch(Maquina::temPartesDesgastadas));
    }

    @Test
    @DisplayName("Salvar de novo substitui o save anterior")
    void salvarDeNovoAtualiza() {
        persistencia.salvar(PlantasDeTeste.padrao());

        persistencia.salvar(PlantasDeTeste.alterada());
        Planta carregada = persistencia.carregar();

        assertEquals("Apocalíptico", carregada.getCenario().getTipo());
        assertEquals(412.35f, carregada.getGerenciador().getVerba());
    }

    @Test
    @DisplayName("Carregar devolve objetos novos: mexer na carregada não muda a original")
    void carregarDevolveObjetosNovos() {
        Planta original = PlantasDeTeste.alterada();
        persistencia.salvar(original);

        Planta carregada = persistencia.carregar();
        carregada.getLinha().getMaquina(0).reparar();
        carregada.getGerenciador().getMateriaPrima().consumir(10);

        assertNotSame(original.getLinha(), carregada.getLinha());
        assertNotSame(original.getLinha().getMaquina(0), carregada.getLinha().getMaquina(0));
        assertEquals(72, original.getLinha().getMaquina(0).getSaude());
        assertEquals(37.5, original.getGerenciador().getMateriaPrima().getQuantidade());
    }

    @Test
    @DisplayName("Carregar duas vezes não duplica máquinas nem demandas")
    void carregarDuasVezesNaoDuplica() {
        persistencia.salvar(PlantasDeTeste.alterada());

        persistencia.carregar();
        Planta segunda = persistencia.carregar();

        assertEquals(3, segunda.getLinha().getMaquinas().size());
        assertEquals(3, segunda.getGerenciador().getDemandas().size());
    }

    @Test
    @DisplayName("Depois de ativar a fábrica carregada, uma demanda nova chega depois das salvas")
    void ativarDepoisDeCarregarAjustaChegada() {
        persistencia.salvar(PlantasDeTeste.alterada()); // aqui a maior ordem de chegada salva é 9

        persistencia.carregar().ativar();
        Demanda nova = new Demanda(new CopoDeVidro("Copo", 0.5), 1);

        assertEquals(10, nova.getOrdemChegada());
    }

    @Test
    @DisplayName("Os produtos das demandas carregadas são os modelos do catálogo")
    void demandasApontamParaOCatalogo() {
        persistencia.salvar(PlantasDeTeste.alterada());

        Planta carregada = persistencia.carregar();

        for (Demanda d : carregada.getGerenciador().getDemandas()) {
            assertSame(FabricaPlanta.buscarNoCatalogo(d.getNomeProduto()).orElseThrow(),
                    carregada.getGerenciador().getProdutoPeloNome(d.getNomeProduto()));
        }
    }
}
