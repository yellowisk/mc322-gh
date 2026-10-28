package br.unicamp.ic.mc322.model.rawmaterial;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import br.unicamp.ic.mc322.exception.MateriaPrimaInsuficienteException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes de Unidade - Matéria Prima")
public class MateriaPrimaTest {

    @Nested
    @DisplayName("Construtor e Inicialização")
    class ConstrutorTestes {

        @Test
        @DisplayName("Não deve permitir criação com quantidades iniciais ou mínimas negativas")
        void validaConstrutorComParametrosInvalidos() {
            // 1. falha
            assertThrows(IllegalArgumentException.class, () -> new MateriaPrima("Vidro", 10, "kg", -1, 1.0F));
            assertThrows(IllegalArgumentException.class, () -> new MateriaPrima("Vidro", -10, "kg", 1, 1.0F));
        }
    }

    @Nested
    @DisplayName("Regras de Negócio e Atualizações")
    class RegrasDeNegocioTestes {

        @Test
        @DisplayName("Deve consumir estoque com sucesso e atualizar a quantidade")
        void deveConsumirEstoque() {
            MateriaPrima material = new MateriaPrima("Vidro", 100, "kg", 10, 2.5F);

            // 1. sucesso
            material.consumir(50.0);
            assertEquals(50.0, material.getQuantidade());

            // 2. falha
            assertThrows(MateriaPrimaInsuficienteException.class, () -> material.consumir(100000.0));
            assertThrows(DemandaInvalidaException.class, () -> material.consumir(-1.0));
        }

        @Test
        @DisplayName("Deve adicionar estoque com sucesso e rejeitar quantidade inválida")
        void deveAdicionarEstoque() {
            MateriaPrima material = new MateriaPrima("Vidro", 20, "kg", 5, 2.5F);

            // 1. sucesso
            material.adicionarEstoque(10);
            assertEquals(30.0, material.getQuantidade(), 0.0001);

            // 2. falha
            assertThrows(IllegalArgumentException.class, () -> material.adicionarEstoque(0));
            assertThrows(IllegalArgumentException.class, () -> material.adicionarEstoque(-10));
        }

        @Test
        @DisplayName("Deve verificar disponibilidade corretamente")
        void deveVerificarDisponibilidade() {
            MateriaPrima material = new MateriaPrima("Vidro", 100, "kg", 10, 2.5F);

            // 1. sucesso
            assertTrue(material.estaDisponivel(50.0));

            // 2. falha
            assertFalse(material.estaDisponivel(100000.0));
        }
    }
}