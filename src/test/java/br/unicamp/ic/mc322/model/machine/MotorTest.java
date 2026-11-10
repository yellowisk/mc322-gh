package br.unicamp.ic.mc322.model.machine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes Motor")
public class MotorTest {
    private Motor motor;

    @BeforeEach
    void setUp() {
        motor = new Motor(5);
    }

    @Nested
    @DisplayName("Construtor e Inicialização")
    class ConstrutorTestes {

        @Test
        @DisplayName("Deve inicializar com sucesso e barrar desgastes inválidos")
        void deveInicializarMotor() {
            // 1. sucesso
            assertEquals(100, motor.getSaude());
            assertEquals(100, motor.getSaudeMaxima());
            assertTrue(motor.estaNovo());
            assertFalse(motor.estaQuebrado());

            // 2. falha
            assertThrows(IllegalArgumentException.class, () -> new Motor(0));
            assertThrows(IllegalArgumentException.class, () -> new Motor(-5));
        }
    }

    @Nested
    @DisplayName("Desgaste e Reparo")
    class DesgasteEReparoTestes {

        @Test
        @DisplayName("Deve desgastar limitando a zero e recuperar saúde ao reparar")
        void deveDesgastarEReparar() {
            int saudeInicial = motor.getSaude();

            // 1. sucesso no desgaste
            motor.desgastar();
            assertTrue(motor.getSaude() < saudeInicial);
            assertTrue(motor.getSaude() >= 0);
            assertFalse(motor.estaNovo());

            for (int i = 0; i < 50; i++) {
                motor.desgastar();
            }
            assertEquals(0, motor.getSaude());

            // 2. sucesso no reparo
            motor.reparar();
            assertEquals(100, motor.getSaude());
            assertTrue(motor.estaNovo());
        }
    }

    @Nested
    @DisplayName("Diagnóstico e Indicadores")
    class DiagnosticoEIndicadoresTestes {

        @Test
        @DisplayName("Deve calcular fator de desgaste e diagnosticar quebras")
        void deveGerarDiagnosticos() {
            // 1. Motor novo
            assertEquals(1.0, motor.fatorDesgaste(), 0.0001);
            assertEquals("Motor: Novo (100/100)", motor.diagnostico());
            assertFalse(motor.estaQuebrado());

            // 2. Motor desgastado
            motor.restaurar(50);
            assertEquals(1.5, motor.fatorDesgaste(), 0.0001);
            assertEquals("Motor: Desgastado (50/100)", motor.diagnostico());

            // 3. Motor crítico/quebrado
            motor.restaurar(10);
            assertTrue(motor.estaQuebrado());
            assertEquals("Motor: QUEBRADO (10/100)", motor.diagnostico());
            // Fator de desgaste com saúde 10: 1 + (100-10)/100 = 1.9
            assertEquals(1.9, motor.fatorDesgaste(), 0.0001);
        }
    }
}