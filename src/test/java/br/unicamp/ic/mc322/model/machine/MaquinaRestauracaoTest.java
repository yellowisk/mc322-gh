package br.unicamp.ic.mc322.model.machine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Restauração do estado das máquinas a partir de valores salvos: os valores voltam iguais, e os inválidos
 * são recusados c/ as mesmas regras que as partes respeitam no uso normal
 */
@DisplayName("Restauração do estado das máquinas")
class MaquinaRestauracaoTest {

    private static MaquinaProcessamento processamento() {
        return new MaquinaProcessamento("Processamento", 20, 0.1, 1, 1, 1, 2, 3);
    }

    private static MaquinaEmpacotamento empacotamento() {
        return new MaquinaEmpacotamento("Empacotamento", 20, 0.1, 1, 1, 1, 2);
    }

    private static MaquinaInspecao inspecao() {
        return new MaquinaInspecao("Inspeção", 20, 0.1, 1, 1, 1, 2, 3);
    }

    @Test
    @DisplayName("Restaurar motor e sensores devolve os mesmos valores")
    void restaurarMotorESensores() {
        MaquinaProcessamento maquina = processamento();

        maquina.restaurarEstado(72, List.of(1.0, 0.55));

        assertEquals(72, maquina.getSaude());
        assertEquals(List.of(1.0, 0.55), maquina.getCalibracoesSensores());
        assertEquals(1, maquina.contarSensoresCalibrados());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 100})
    @DisplayName("Saúde do motor nos limites (0 e 100) é aceita")
    void saudeNosLimitesEhAceita(int saude) {
        MaquinaProcessamento maquina = processamento();

        maquina.restaurarEstado(saude, List.of(1.0, 1.0));

        assertEquals(saude, maquina.getSaude());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101})
    @DisplayName("Saúde do motor fora de 0 a 100 lança IllegalArgumentException")
    void saudeForaDoIntervaloLanca(int saude) {
        MaquinaProcessamento maquina = processamento();

        assertThrows(IllegalArgumentException.class, () -> maquina.restaurarEstado(saude, List.of(1.0, 1.0)));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, 1.01, Double.NaN})
    @DisplayName("Calibração fora de 0 a 1 (ou NaN) lança IllegalArgumentException")
    void calibracaoForaDoIntervaloLanca(double calibracao) {
        MaquinaProcessamento maquina = processamento();

        assertThrows(IllegalArgumentException.class, () -> maquina.restaurarEstado(100, List.of(1.0, calibracao)));
    }

    @Test
    @DisplayName("Quantidade de calibrações diferente da de sensores lança IllegalArgumentException")
    void quantidadeDeSensoresErradaLanca() {
        MaquinaProcessamento maquina = processamento();

        assertThrows(IllegalArgumentException.class, () -> maquina.restaurarEstado(100, List.of(1.0)));
    }

    @Test
    @DisplayName("Motor restaurado abaixo de 15 deixa a máquina precisando de manutenção")
    void motorRestauradoQuebradoPrecisaManutencao() {
        MaquinaProcessamento maquina = processamento();

        maquina.restaurarEstado(14, List.of(1.0, 1.0));

        assertTrue(maquina.precisaManutencao());
    }

    @Test
    @DisplayName("Restaurar resistências devolve quais estão queimadas")
    void restaurarResistencias() {
        MaquinaProcessamento maquina = processamento();

        maquina.restaurarResistencias(List.of(false, true, false));

        assertEquals(List.of(false, true, false), maquina.getResistenciasQueimadas());
        assertFalse(maquina.precisaManutencao(), "uma resistência queimada não para o forno");
    }

    @Test
    @DisplayName("Todas as resistências restauradas como queimadas deixam a máquina precisando de manutenção")
    void todasResistenciasQueimadasPrecisaManutencao() {
        MaquinaProcessamento maquina = processamento();

        maquina.restaurarResistencias(List.of(true, true, true));

        assertTrue(maquina.precisaManutencao());
    }

    @Test
    @DisplayName("Quantidade de resistências diferente da do forno lança IllegalArgumentException")
    void quantidadeDeResistenciasErradaLanca() {
        MaquinaProcessamento maquina = processamento();

        assertThrows(IllegalArgumentException.class, () -> maquina.restaurarResistencias(List.of(true, true)));
    }

    @Test
    @DisplayName("Restaurar a seladora devolve o desgaste da lâmina")
    void restaurarSeladora() {
        MaquinaEmpacotamento maquina = empacotamento();

        maquina.restaurarSeladora(0.3);

        assertEquals(0.3, maquina.getDesgasteLamina());
        assertFalse(maquina.precisaManutencao());
    }

    @Test
    @DisplayName("Lâmina restaurada 100% gasta deixa a máquina precisando de manutenção")
    void laminaTotalmenteGastaPrecisaManutencao() {
        MaquinaEmpacotamento maquina = empacotamento();

        maquina.restaurarSeladora(1.0);

        assertTrue(maquina.precisaManutencao());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 1.1, Double.NaN})
    @DisplayName("Desgaste da lâmina fora de 0 a 1 (ou NaN) lança IllegalArgumentException")
    void desgasteDaLaminaForaDoIntervaloLanca(double desgaste) {
        MaquinaEmpacotamento maquina = empacotamento();

        assertThrows(IllegalArgumentException.class, () -> maquina.restaurarSeladora(desgaste));
    }

    @Test
    @DisplayName("Restaurar câmeras devolve quais estão c/ a lente suja")
    void restaurarCameras() {
        MaquinaInspecao maquina = inspecao();

        maquina.restaurarCameras(List.of(true, false, false));

        assertEquals(List.of(true, false, false), maquina.getLentesSujas());
        assertFalse(maquina.precisaManutencao(), "uma câmera suja não para a inspeção");
    }

    @Test
    @DisplayName("Quantidade de câmeras diferente da da máquina lança IllegalArgumentException")
    void quantidadeDeCamerasErradaLanca() {
        MaquinaInspecao maquina = inspecao();

        assertThrows(IllegalArgumentException.class, () -> maquina.restaurarCameras(List.of(true, false, false, true)));
    }

    @Test
    @DisplayName("Reparar depois de restaurar deixa todas as partes novas de novo")
    void repararDepoisDeRestaurar() {
        MaquinaProcessamento maquina = processamento();
        maquina.restaurarEstado(20, List.of(0.5, 0.5));
        maquina.restaurarResistencias(List.of(true, true, true));

        maquina.reparar();

        assertEquals(100, maquina.getSaude());
        assertEquals(List.of(1.0, 1.0), maquina.getCalibracoesSensores());
        assertEquals(List.of(false, false, false), maquina.getResistenciasQueimadas());
        assertFalse(maquina.temPartesDesgastadas());
    }
}
