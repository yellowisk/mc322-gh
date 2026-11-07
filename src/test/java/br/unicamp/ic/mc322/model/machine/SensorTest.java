package br.unicamp.ic.mc322.model.machine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes de Unidade - Sensor")
public class SensorTest {
    private Sensor sensor;

    @BeforeEach
    void setUp() {
        sensor = new Sensor(1);
    }

    @Nested
    @DisplayName("Construtor e Inicialização")
    class ConstrutorTestes {

        @Test
        @DisplayName("Deve inicializar o sensor com calibração máxima (1.0) e estado Novo")
        void deveInicializarComCalibracaoMaxima() {
            assertEquals(1.0, sensor.getCalibracao(), 0.0001);
            assertTrue(sensor.estaNovo());
            assertTrue(sensor.estaCalibrado());
        }
    }

    @Nested
    @DisplayName("Registro de Ciclos e Recalibração")
    class CicloERecalibracaoTestes {

        @Test
        @DisplayName("Deve registrar ciclos mantendo os limites e recalibrar com sucesso")
        void deveRegistrarCiclosERecalibrar() {
            // 1. desgaste
            for (int i = 0; i < 200; i++) {
                sensor.registrarCiclo();
            }
            assertTrue(sensor.getCalibracao() >= 0.0);
            assertTrue(sensor.getCalibracao() <= 1.0);
            assertFalse(sensor.estaNovo());

            // 2. recalibracao
            sensor.recalibrar();
            assertEquals(1.0, sensor.getCalibracao(), 0.0001);
            assertTrue(sensor.estaNovo());
            assertTrue(sensor.estaCalibrado());
        }
    }

    @Nested
    @DisplayName("Restauração de Estado")
    class RestauracaoTestes {

        @Test
        @DisplayName("Deve restaurar calibração válida e bloquear valores fora do intervalo")
        void deveRestaurarCalibracao() {
            // 1. sucesso
            sensor.restaurar(0.8);
            assertEquals(0.8, sensor.getCalibracao(), 0.0001);

            sensor.restaurar(0.0);
            assertEquals(0.0, sensor.getCalibracao(), 0.0001);

            // 2. falha
            assertThrows(IllegalArgumentException.class, () -> sensor.restaurar(-0.1));
            assertThrows(IllegalArgumentException.class, () -> sensor.restaurar(1.01));
            assertThrows(IllegalArgumentException.class, () -> sensor.restaurar(2.0));
        }
    }

    @Nested
    @DisplayName("Diagnóstico e Indicadores")
    class DiagnosticoEIndicadoresTestes {

        @Test
        @DisplayName("Deve verificar se está calibrado e gerar strings de diagnóstico")
        void deveGerarDiagnosticos() {
            // 1. Sensor calibrado
            sensor.restaurar(0.95);
            assertTrue(sensor.estaCalibrado());
            assertEquals("Sensor 1: OK (95%)", sensor.diagnostico());

            // 2. Sensor descalibrado
            sensor.restaurar(0.60);
            assertFalse(sensor.estaCalibrado());
            assertEquals("Sensor 1: Descalibrado (60%)", sensor.diagnostico());
        }
    }
}