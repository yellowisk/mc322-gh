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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes Máquina de Inspeção")
public class MaquinaInspecaoTest {

    private MaquinaInspecao maquina;
    private Produto produtoEmpacotado;

    @BeforeEach
    void setUp() {
        maquina = new MaquinaInspecao(
                "Máquina de Inspeção", 20, 0.01, 5.0, 1.0, 5, 2, 2
        );
        produtoEmpacotado = new CopoDeVidro("Copo", 0.4);
        produtoEmpacotado.setStatus(StatusProduto.EMPACOTADO);
    }

    @Nested
    @DisplayName("Construtor e Inicialização")
    class ConstrutorTestes {

        @Test
        @DisplayName("Deve inicializar a máquina de inspeção desligada e com câmeras limpas")
        void deveInicializarCorretamente() {
            assertEquals("Máquina de Inspeção", maquina.getNome());
            assertEquals("Inspeção", maquina.getTipo());
            assertFalse(maquina.estaLigada());
            assertFalse(maquina.precisaManutencao());
            assertEquals(List.of(false, false), maquina.getLentesSujas());
        }

        @Test
        @DisplayName("Não deve permitir criação com quantidade de câmeras inválida")
        void validaCamerasInvalidas() {
            // 1. falha
            assertThrows(IllegalArgumentException.class, () ->
                    new MaquinaInspecao("Inspeção", 20, 0.01, 5.0, 1.0, 5, 2, 0)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    new MaquinaInspecao("Inspeção", 20, 0.01, 5.0, 1.0, 5, 2, -1)
            );
        }
    }

    @Nested
    @DisplayName("Funcionamento e Processamento")
    class FuncionamentoTestes {

        @Test
        @DisplayName("Deve processar o produto e atualizar status, barrando uso desligada")
        void deveProcessarEInspecionar() {
            // 1. falha
            assertThrows(MaquinaIndisponivelException.class, () -> maquina.processar(produtoEmpacotado));

            // 2. sucesso
            maquina.ligar();
            Produto resultado = maquina.processar(produtoEmpacotado);

            assertNotNull(resultado.getStatus());
            assertTrue(
            resultado.getStatus() == StatusProduto.APROVADO ||
                    resultado.getStatus() == StatusProduto.REJEITADO ||
                    resultado.getStatus() == StatusProduto.PRECISA_MANUTENCAO
            );
        }
    }

    @Nested
    @DisplayName("Manutenção e Câmeras")
    class ManutencaoECamerasTestes {

        @Test
        @DisplayName("Deve exigir e realizar reparo quando as câmeras estiverem sujas")
        void deveExigirERepararCameras() {
            maquina.ligar();
            maquina.restaurarCameras(List.of(true, true));

            // 1. falha (precisa de reparo)
            assertTrue(maquina.precisaManutencao());
            assertThrows(MaquinaPrecisaReparoException.class, () -> maquina.processar(produtoEmpacotado));

            // 2. sucesso (após reparar)
            maquina.reparar();
            assertFalse(maquina.precisaManutencao());
            assertEquals(List.of(false, false), maquina.getLentesSujas());
        }
    }

    @Nested
    @DisplayName("Restauração de Estado")
    class RestauracaoTestes {

        @Test
        @DisplayName("Deve restaurar estado das lentes e bloquear listas incompatíveis")
        void deveRestaurarEstadoLentes() {
            // 1. sucesso
            maquina.restaurarCameras(List.of(true, false));
            assertEquals(List.of(true, false), maquina.getLentesSujas());

            // 2. falha
            assertThrows(IllegalArgumentException.class, () -> maquina.restaurarCameras(List.of(true))
            );
        }
    }
}