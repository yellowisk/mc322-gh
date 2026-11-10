package br.unicamp.ic.mc322.model.machine;

import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import br.unicamp.ic.mc322.exception.MaquinaPrecisaReparoException;
import br.unicamp.ic.mc322.model.product.CopoDeVidro;
import br.unicamp.ic.mc322.model.product.Produto;
import br.unicamp.ic.mc322.model.product.StatusProduto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes Máquina de Processamento")
public class MaquinaProcessamentoTest {

    private MaquinaProcessamento maquina;
    private Produto modeloCopo;

    @BeforeEach
    void setUp() {
        maquina = new MaquinaProcessamento(
                "Máquina de Processamento", 10, 0.05, 12.5, 1.0, 5, 2, 2
        );
        modeloCopo = new CopoDeVidro("Copo", 0.4);
    }

    @Nested
    @DisplayName("Construtor e Inicialização")
    class ConstrutorTestes {

        @Test
        @DisplayName("Deve inicializar a máquina de processamento desligada e com estado novo")
        void deveInicializarCorretamente() {
            assertEquals("Máquina de Processamento", maquina.getNome());
            assertEquals("Processamento", maquina.getTipo());
            assertEquals(10, maquina.getCapacidadeMaxima());
            assertEquals(12.5, maquina.getCustoOperacao(), 0.0001);
            assertFalse(maquina.estaLigada());
            assertFalse(maquina.precisaManutencao());
            assertEquals(2, maquina.getQuantidadeSensores());
            assertEquals(2, maquina.contarSensoresCalibrados());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5})
        @DisplayName("Não deve permitir criação com quantidade de resistências ou sensores inválida")
        void validaConfiguracoesInvalidas(int qtdInvalida) {
            // 1. falha (resistências)
            assertThrows(IllegalArgumentException.class, () ->
                    new MaquinaProcessamento("M. Proc.", 10, 0.05, 10.0, 1.0, 5, 2, qtdInvalida)
            );
            // 2. falha (sensores)
            assertThrows(IllegalArgumentException.class, () ->
                    new MaquinaProcessamento("M. Proc.", 10, 0.05, 10.0, 1.0, 5, qtdInvalida, 2)
            );
        }
    }

    @Nested
    @DisplayName("Funcionamento e Processamento")
    class FuncionamentoTestes {

        @Test
        @DisplayName("Deve ligar e processar etapa, barrando uso e verificação desligada")
        void deveLigarEProcessar() {
            // 1. falha
            assertThrows(MaquinaIndisponivelException.class, () -> maquina.verificarDisponivel());
            assertThrows(MaquinaIndisponivelException.class, () -> maquina.processar(modeloCopo));

            // 2. sucesso
            maquina.ligar();
            assertTrue(maquina.estaLigada());

            Produto produtoProcessado = maquina.processar(modeloCopo);
            assertNotNull(produtoProcessado);
            assertEquals(StatusProduto.PROCESSADO, produtoProcessado.getStatus());

            // teste de alternância de estado
            maquina.desligar();
            assertFalse(maquina.estaLigada());
        }
    }

    @Nested
    @DisplayName("Manutenção, Desgaste e Reparo")
    class ManutencaoEReparoTestes {

        @Test
        @DisplayName("Deve exigir reparo geral por quebra do motor ou resistências queimadas")
        void deveExigirERepararComponentes() {
            // instanciação (motor quebrado e resistências queimadas)
            maquina.ligar();
            maquina.restaurarEstado(10, List.of(0.5, 0.5));
            maquina.restaurarResistencias(List.of(true, true));

            // 1. falha
            assertTrue(maquina.precisaManutencao());
            assertThrows(MaquinaPrecisaReparoException.class, () -> maquina.verificarDisponivel());
            assertThrows(MaquinaPrecisaReparoException.class, () -> maquina.processar(modeloCopo));

            // 2. sucesso (após reparo completo)
            maquina.reparar();
            assertFalse(maquina.precisaManutencao());
            assertEquals(100, maquina.getSaude());
            assertEquals(2, maquina.contarSensoresCalibrados());
            assertEquals(List.of(false, false), maquina.getResistenciasQueimadas());
        }
    }

    @Nested
    @DisplayName("Restauração de Estado")
    class RestauracaoTestes {

        @Test
        @DisplayName("Deve restaurar estado do motor e sensores, bloqueando listas incompatíveis")
        void deveRestaurarMotorESensores() {
            // 1. sucesso
            maquina.restaurarEstado(50, List.of(0.8, 0.9));
            assertEquals(50, maquina.getSaude());
            assertEquals(List.of(0.8, 0.9), maquina.getCalibracoesSensores());

            // 2. falha
            assertThrows(IllegalArgumentException.class, () -> maquina.restaurarEstado(100, List.of(1.0))
            );
        }

        @Test
        @DisplayName("Deve restaurar estado das resistências e bloquear tamanho incompatível")
        void deveRestaurarResistencias() {
            // 1. sucesso
            maquina.restaurarResistencias(List.of(true, false));
            assertEquals(List.of(true, false), maquina.getResistenciasQueimadas());

            // 2. falha
            assertThrows(IllegalArgumentException.class, () -> maquina.restaurarResistencias(List.of(true))
            );
        }
    }
}