package br.unicamp.ic.mc322.model.demand;

import br.unicamp.ic.mc322.exception.QuantidadeDemandaInvalidaException;
import br.unicamp.ic.mc322.exception.TransicaoDemandaInvalidaException;
import br.unicamp.ic.mc322.model.product.CopoDeVidro;
import br.unicamp.ic.mc322.model.product.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes de Unidade - Demanda e StatusDemanda")
public class DemandaTest {

    private Produto copo;

    @BeforeEach
    void setUp() {
        copo = new CopoDeVidro("Copo de Vidro", 0.5);
    }

    @Nested
    @DisplayName("Construtor e Inicialização")
    class ConstrutorTestes {

        @Test
        @DisplayName("Deve inicializar demanda com valores padrão e status PENDENTE")
        void deveInicializarDemandaCorretamente() {
            Demanda d = new Demanda(copo, 10);

            assertEquals("Copo de Vidro", d.getNomeProduto());
            assertEquals(10, d.getQuantidade());
            assertEquals(0, d.getQuantidadeProduzida());
            assertEquals(10, d.getQuantidadeRestante());
            assertEquals(StatusDemanda.PENDENTE, d.getStatus());
            assertEquals(5.0, d.getMateriaPrimaTotal(), 0.0001); // 10 * 0.5
            assertEquals(0.0, d.getTempoTotalProducao(), 0.0001);
        }

        @Test
        @DisplayName("Não deve permitir criação com quantidade nula ou negativa")
        void validaConstrutorComParametrosInvalidos() {
            assertThrows(IllegalArgumentException.class, () -> new Demanda(copo, 0));
            assertThrows(IllegalArgumentException.class, () -> new Demanda(copo, -1));
        }
    }

    @Nested
    @DisplayName("Regras de Negócio e Atualizações")
    class RegrasDeNegocioTestes {

        @Test
        @DisplayName("Deve atualizar quantidade, recalcular matéria-prima e resetar ordem de chegada")
        void deveAtualizarQuantidade() {
            // instanciacao
            Demanda d = new Demanda(copo, 10);
            long ordemAntiga = d.getOrdemChegada();

            // 1. sucesso
            d.atualizarQuantidade(copo, 20);
            assertEquals(20, d.getQuantidade());
            assertEquals(10.0, d.getMateriaPrimaTotal(), 0.0001);
            assertTrue(d.getOrdemChegada() > ordemAntiga);

            // 2. falha
            assertThrows(QuantidadeDemandaInvalidaException.class, () -> d.atualizarQuantidade(copo, -1));
        }

        @Test
        @DisplayName("Deve calcular custo estimado e verificar se é viável com o orçamento")
        void deveCalcularCustoEViabilidade() {
            Demanda d = new Demanda(copo, 10); // 10 unidades restantes
            d.atualizarCustoEstimado(2.5); // 10 * 2.5 = 25.0

            assertEquals(25.0, d.getCustoEstimado(), 0.0001);
            assertTrue(d.ehViavel(30.0));
            assertFalse(d.ehViavel(20.0));
        }
    }

    @Nested
    @DisplayName("Ciclo de Vida e Transições de Status")
    class CicloDeVidaTestes {

        @Test
        @DisplayName("Deve realizar transição completa: PENDENTE -> EM_PRODUCAO -> CONCLUIDA")
        void deveConcluirDemandaComSucesso() {
            Demanda d = new Demanda(copo, 10);

            d.iniciarProducao();
            assertEquals(StatusDemanda.EM_PRODUCAO, d.getStatus());
            // 1. falha
            assertThrows(IllegalArgumentException.class, () -> d.concluir(-1, 1.0));
            assertThrows(IllegalArgumentException.class, () -> d.concluir(1, -1.0));

            // 2. sucesso
            d.concluir(10, 120.0);
            assertEquals(StatusDemanda.CONCLUIDA, d.getStatus());
            assertEquals(10, d.getQuantidadeProduzida());
            assertEquals(0, d.getQuantidadeRestante());
            assertEquals(120.0, d.getTempoTotalProducao(), 0.0001);
        }

        @Test
        @DisplayName("Deve cancelar demanda guardando o progresso parcial e permitir resetar")
        void deveCancelarEResetarDemanda() {
            Demanda d = new Demanda(copo, 10);

            d.iniciarProducao();

            // 1. falha
            assertThrows(IllegalArgumentException.class, () -> d.cancelar(-1, 1.0));
            assertThrows(IllegalArgumentException.class, () -> d.cancelar(1, -1.0));

            // 2. sucesso
            d.cancelar(4, 50.0);
            assertEquals(StatusDemanda.CANCELADA, d.getStatus());
            assertEquals(4, d.getQuantidadeProduzida());
            assertEquals(6, d.getQuantidadeRestante());

            d.resetar();
            assertEquals(StatusDemanda.PENDENTE, d.getStatus());
            assertEquals(0, d.getQuantidadeProduzida());
            assertEquals(0.0, d.getTempoTotalProducao(), 0.0001);
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar transição inválida (ex: PENDENTE -> CONCLUIDA direto)")
        void deveLancarExcecaoEmTransicaoInvalida() {
            Demanda d = new Demanda(copo, 10);
            assertThrows(TransicaoDemandaInvalidaException.class, () -> d.concluir(10, 100));
        }
    }

    @Nested
    @DisplayName("Testes enum StatusDemanda")
    class StatusDemandaTestes {

        @Test
        @DisplayName("Apenas PENDENTE deve ser selecionável pelas estratégias")
        void validaSelecao() {
            assertTrue(StatusDemanda.PENDENTE.ehSelecionavel());
            assertFalse(StatusDemanda.EM_PRODUCAO.ehSelecionavel());
            assertFalse(StatusDemanda.CONCLUIDA.ehSelecionavel());
            assertFalse(StatusDemanda.CANCELADA.ehSelecionavel());
        }

        @Test
        @DisplayName("Apenas PENDENTE e CANCELADA podem ser fabricadas")
        void validaFabricacao() {
            assertTrue(StatusDemanda.PENDENTE.podeSerFabricada());
            assertTrue(StatusDemanda.CANCELADA.podeSerFabricada());
            assertFalse(StatusDemanda.EM_PRODUCAO.podeSerFabricada());
            assertFalse(StatusDemanda.CONCLUIDA.podeSerFabricada());
        }

        @Test
        @DisplayName("Valida regras de transição do Enum StatusDemanda")
        void validaTransicoesDoEnum() {
            // PENDENTE -> EM_PRODUCAO (ou PENDENTE ao resetar/atualizar)
            assertTrue(StatusDemanda.PENDENTE.podeTransicionarPara(StatusDemanda.EM_PRODUCAO));
            assertFalse(StatusDemanda.PENDENTE.podeTransicionarPara(StatusDemanda.CONCLUIDA));

            // EM_PRODUCAO -> CONCLUIDA ou CANCELADA (não pode voltar pra PENDENTE direto)
            assertTrue(StatusDemanda.EM_PRODUCAO.podeTransicionarPara(StatusDemanda.CONCLUIDA));
            assertTrue(StatusDemanda.EM_PRODUCAO.podeTransicionarPara(StatusDemanda.CANCELADA));
            assertFalse(StatusDemanda.EM_PRODUCAO.podeTransicionarPara(StatusDemanda.PENDENTE));

            // CONCLUIDA não pode transicionar para nenhum outro
            assertFalse(StatusDemanda.CONCLUIDA.podeTransicionarPara(StatusDemanda.EM_PRODUCAO));
            assertFalse(StatusDemanda.CONCLUIDA.podeTransicionarPara(StatusDemanda.CANCELADA));
        }
    }
}