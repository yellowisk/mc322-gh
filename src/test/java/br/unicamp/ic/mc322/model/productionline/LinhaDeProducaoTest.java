package br.unicamp.ic.mc322.model.productionline;

import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import br.unicamp.ic.mc322.model.machine.MaquinaEmpacotamento;
import br.unicamp.ic.mc322.model.machine.MaquinaInspecao;
import br.unicamp.ic.mc322.model.machine.MaquinaProcessamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes Linha de Produção")
public class LinhaDeProducaoTest {

    private LinhaDeProducao linha;
    private MaquinaProcessamento maquinaProcessamento;
    private MaquinaEmpacotamento maquinaEmpacotamento;
    private MaquinaInspecao maquinaInspecao;

    @BeforeEach
    void setUp() {
        linha = new LinhaDeProducao("Linha Teste");

        maquinaProcessamento = new MaquinaProcessamento(
                "Processadora", 20, 0.05, 10.0, 1.0, 5, 2, 2
        );
        maquinaEmpacotamento = new MaquinaEmpacotamento(
                "Empacotadora", 20, 0.05, 8.0, 1.0, 5, 2
        );
        maquinaInspecao = new MaquinaInspecao(
                "Inspetora", 20, 0.05, 5.0, 1.0, 5, 2, 2
        );
    }

    @Nested
    @DisplayName("Construtor e Adição de Máquinas")
    class ConstrutorEAdicaoTestes {

        @Test
        @DisplayName("Deve inicializar a linha e barrar nomes inválidos")
        void deveInicializarLinha() {
            // 1. sucesso
            assertEquals("Linha Teste", linha.getNome());
            assertTrue(linha.getMaquinas().isEmpty());
            assertFalse(linha.precisaManutencao());

            // 2. falha
            assertThrows(IllegalArgumentException.class, () -> new LinhaDeProducao(null));
            assertThrows(IllegalArgumentException.class, () -> new LinhaDeProducao(""));
        }

        @Test
        @DisplayName("Deve adicionar máquinas com sucesso e rejeitar máquina nula")
        void deveAdicionarMaquinas() {
            // 1. sucesso
            linha.adicionarMaquina(maquinaProcessamento);
            linha.adicionarMaquina(maquinaEmpacotamento);

            assertEquals(2, linha.getMaquinas().size());

            assertThrows(UnsupportedOperationException.class, () -> linha.getMaquinas().add(maquinaInspecao));

            // 2. falha
            assertThrows(IllegalArgumentException.class, () -> linha.adicionarMaquina(null));
        }
    }

    @Nested
    @DisplayName("Acesso e Etapas da Produção")
    class AcessoEEtapasTestes {

        @BeforeEach
        void popularLinha() {
            linha.adicionarMaquina(maquinaProcessamento);
            linha.adicionarMaquina(maquinaEmpacotamento);
            linha.adicionarMaquina(maquinaInspecao);
        }

        @Test
        @DisplayName("Deve obter máquina por índice e etapa, barrando limites inválidos")
        void deveObterMaquina() {
            // 1. sucesso
            assertEquals(maquinaProcessamento, linha.getMaquina(0));
            assertEquals(maquinaEmpacotamento, linha.getMaquina(1));
            assertEquals(maquinaInspecao, linha.getMaquina(2));

            assertEquals(maquinaProcessamento, linha.getMaquinaDaEtapa(EtapaProducao.PROCESSAMENTO));
            assertEquals(maquinaEmpacotamento, linha.getMaquinaDaEtapa(EtapaProducao.EMPACOTAMENTO));
            assertEquals(maquinaInspecao, linha.getMaquinaDaEtapa(EtapaProducao.INSPECAO));

            // 2. falha
            assertThrows(IndexOutOfBoundsException.class, () -> linha.getMaquina(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> linha.getMaquina(3));
            assertThrows(IndexOutOfBoundsException.class, () -> linha.getMaquina(5));
        }
    }

    @Nested
    @DisplayName("Controle de Energia e Cálculo de Custos")
    class ControleDeEnergiaECustoTestes {

        @BeforeEach
        void popularLinha() {
            linha.adicionarMaquina(maquinaProcessamento);
            linha.adicionarMaquina(maquinaEmpacotamento);
            linha.adicionarMaquina(maquinaInspecao);
        }

        @Test
        @DisplayName("Deve ligar e desligar todas as máquinas da linha conjuntamente")
        void deveLigarEDesligarLinha() {
            // 1. ligar
            linha.ligar();
            assertTrue(linha.getMaquina(0).estaLigada());
            assertTrue(linha.getMaquina(1).estaLigada());
            assertTrue(linha.getMaquina(2).estaLigada());

            // 2. desligar
            linha.desligar();
            assertFalse(linha.getMaquina(0).estaLigada());
            assertFalse(linha.getMaquina(1).estaLigada());
            assertFalse(linha.getMaquina(2).estaLigada());
        }

        @Test
        @DisplayName("Deve somar corretamente o custo de operação por unidade de todas as máquinas")
        void deveCalcularCustoOperacaoPorUnidade() {
            assertEquals(23.0, linha.calcCustoOperacaoPorUnidade(), 0.0001); // 10.0 + 8.0 + 5.0 = 23.0
        }
    }

    @Nested
    @DisplayName("Manutenção e Reparos")
    class ManutencaoEReparoTestes {

        @BeforeEach
        void popularLinha() {
            linha.adicionarMaquina(maquinaProcessamento);
            linha.adicionarMaquina(maquinaEmpacotamento);
            linha.adicionarMaquina(maquinaInspecao);
        }

        @Test
        @DisplayName("Deve indicar necessidade de manutenção quando houver máquina quebrada")
        void deveIdentificarPrecisaManutencao() {
            // instanciacao (linha nova)
            assertFalse(linha.precisaManutencao());

            // 1. sucesso (identificou quebra na empacotadora)
            maquinaEmpacotamento.restaurarEstado(10, List.of(1.0, 1.0));
            assertTrue(linha.precisaManutencao());
        }

        @Test
        @DisplayName("Deve reparar máquina com desgaste e barrar índices ou máquinas sem desgaste")
        void deveRepararMaquinaEspecifica() {
            // Insere desgaste na primeira máquina
            maquinaProcessamento.restaurarEstado(50, List.of(0.8, 0.8));
            assertTrue(maquinaProcessamento.temPartesDesgastadas());

            // 1. sucesso
            linha.repararMaquina(0);
            assertFalse(maquinaProcessamento.temPartesDesgastadas());

            // 2. falha (tentar reparar a mesma que já está nova)
            assertThrows(MaquinaIndisponivelException.class, () -> linha.repararMaquina(0));

            // falhas de índice
            assertThrows(IllegalArgumentException.class, () -> linha.repararMaquina(-1));
            assertThrows(IllegalArgumentException.class, () -> linha.repararMaquina(3));
            assertThrows(IllegalArgumentException.class, () -> linha.repararMaquina(10));
        }

        @Test
        @DisplayName("Deve reparar todas as máquinas com desgaste, barrando se todas estiverem novas")
        void deveRepararTodasAsMaquinasComDesgaste() {
            // 1. falha (todas novas)
            assertThrows(MaquinaIndisponivelException.class, () -> linha.repararTodas());

            // instanciacao de desgastes
            maquinaProcessamento.restaurarEstado(60, List.of(1.0, 1.0));
            maquinaInspecao.restaurarCameras(List.of(true, false));

            // 2. sucesso
            int reparadas = linha.repararTodas();
            assertEquals(2, reparadas);
            assertFalse(linha.precisaManutencao());
        }
    }

    @Nested
    @DisplayName("Relatório e Resumos da Auditoria")
    class RelatorioEDiagnosticoTestes {

        @BeforeEach
        void popularLinha() {
            linha.adicionarMaquina(maquinaProcessamento);
            linha.adicionarMaquina(maquinaEmpacotamento);
        }

        @Test
        @DisplayName("Deve gerar diagnóstico e resumo do problema formatados sem lançar exceções")
        void deveGerarRelatorios() {
            // 1. sucesso
            String relatorioCompleto = linha.gerarRelatorioDiagnostico();
            String relatorioFiltrado = linha.gerarRelatorioDiagnostico(i -> i == 0);
            String resumoProblema = linha.gerarResumoProblema();

            assertNotNull(relatorioCompleto);
            assertTrue(relatorioCompleto.contains("Linha Teste"));
            assertTrue(relatorioCompleto.contains("Processadora"));

            assertNotNull(relatorioFiltrado);
            assertNotNull(resumoProblema);
            assertTrue(resumoProblema.contains("Processadora"));
        }
    }
}