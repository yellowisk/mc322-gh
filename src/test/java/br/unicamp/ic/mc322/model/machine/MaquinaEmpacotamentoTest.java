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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes Máquina de Empacotamento")
public class MaquinaEmpacotamentoTest {

    private MaquinaEmpacotamento maquina;
    private Produto produtoProcessado;

    @BeforeEach
    void setUp() {
        maquina = new MaquinaEmpacotamento(
                "Máquina de Empacotamento", 15, 0.02, 8.0, 1.0, 4, 2
        );
        produtoProcessado = new CopoDeVidro("Copo", 0.4);
        produtoProcessado.setStatus(StatusProduto.PROCESSADO);
    }

    @Nested
    @DisplayName("Construtor e Inicialização")
    class ConstrutorTestes {

        @Test
        @DisplayName("Deve inicializar a máquina de empacotamento desligada e com seladora nova")
        void deveInicializarCorretamente() {
            // 1. sucesso
            assertEquals("Máquina de Empacotamento", maquina.getNome());
            assertEquals("Empacotamento", maquina.getTipo());
            assertFalse(maquina.estaLigada());
            assertFalse(maquina.precisaManutencao());
            assertEquals(0.0, maquina.getDesgasteLamina(), 0.0001);

            // 2. falha
            assertThrows(IllegalArgumentException.class, () ->
                    new MaquinaEmpacotamento("Empacotadora", 15, 0.02, 8.0, 1.0, 4, 0)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    new MaquinaEmpacotamento("Empacotadora", 15, 0.02, 8.0, 1.0, 4, -1)
            );
        }
    }

    @Nested
    @DisplayName("Regras de Negócio e Atualizações")
    class FuncionamentoTestes {

        @Test
        @DisplayName("Deve selar o produto e atualizar o status para EMPACOTADO")
        void deveEmpacotarComSucesso() {
            // 1. falha (processar desligada)
            assertThrows(MaquinaIndisponivelException.class, () -> maquina.processar(produtoProcessado));

            // 2. sucesso
            maquina.ligar();
            Produto resultado = maquina.processar(produtoProcessado);
            assertEquals(StatusProduto.EMPACOTADO, resultado.getStatus());
        }
    }

    @Nested
    @DisplayName("Ciclo de Vida e Transições de Status")
    class ManutencaoESeladoraTestes {

        @Test
        @DisplayName("Deve exigir reparo quando a lâmina estiver gasta e permitir conserto")
        void deveExigirReparoERepararSeladora() {
            maquina.ligar();
            maquina.restaurarSeladora(1.0);

            // 1. falha
            assertTrue(maquina.precisaManutencao());
            assertThrows(MaquinaPrecisaReparoException.class, () -> maquina.processar(produtoProcessado));

            // 2. sucesso
            maquina.reparar();
            assertFalse(maquina.precisaManutencao());
            assertEquals(0.0, maquina.getDesgasteLamina(), 0.0001);
            assertEquals(100, maquina.getSaude());
        }
    }

    @Nested
    @DisplayName("Restauração de Estado")
    class RestauracaoTestes {

        @Test
        @DisplayName("Deve restaurar o desgaste da lâmina para valores válidos")
        void deveRestaurarDesgasteValido() {
            // 1. sucesso
            maquina.restaurarSeladora(0.6);
            assertEquals(0.6, maquina.getDesgasteLamina(), 0.0001);

            // 2. falha
            assertThrows(IllegalArgumentException.class, () -> maquina.restaurarSeladora(-0.1));
            assertThrows(IllegalArgumentException.class, () -> maquina.restaurarSeladora(1.01));
        }
    }
}